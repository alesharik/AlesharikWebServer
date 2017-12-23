/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.api.collections;

import com.alesharik.webserver.api.functions.TripleConsumer;
import com.alesharik.webserver.api.functions.TripleFunction;
import com.alesharik.webserver.api.ticking.Tickable;
import com.alesharik.webserver.api.ticking.TickingPool;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.alesharik.webserver.api.collections.CachedHashMap.DEFAULT_CAPACITY;
import static com.alesharik.webserver.api.collections.CollectionsMathUtils.*;
import static com.alesharik.webserver.api.collections.ConcurrentLiveArrayList.DEFAULT_DELAY;
import static com.alesharik.webserver.api.collections.ConcurrentLiveArrayList.DEFAULT_LIFE_TIME;

/**
 * This is a concurrent hash map. Every key in this map has an expiry period. If life time of key is 0 then key removed.
 * Map updates every set delay in {@link TickingPool}(default or not) => concurrently.
 * <p>
 * LifeTime check is happening only in timer
 */
@EqualsAndHashCode(exclude = {"nodesLock", "entrySet", "removeQueue"}, callSuper = false)
@ToString(exclude = {"nodesLock", "entrySet", "removeQueue"})
public class ConcurrentLiveHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Tickable, Stoppable, Cloneable {
    private final StampedLock nodesLock = new StampedLock();
    private final AtomicBoolean started;
    private final AtomicInteger size;
    private final Queue<K> removeQueue = new LinkedTransferQueue<>();
    private volatile AtomicReferenceArray<LockedNode<K, V>> nodes;
    private volatile int sizeLimit;
    @Setter
    @Getter
    private volatile long defaultPeriod = DEFAULT_LIFE_TIME;
    private volatile TickingPool tickingPool = ConcurrentLiveArrayList.DEFAULT_POOL;
    @Getter
    private volatile long delay;
    private transient Set<Entry<K, V>> entrySet;

    public ConcurrentLiveHashMap() {
        this(DEFAULT_CAPACITY);
    }

    public ConcurrentLiveHashMap(@Nonnegative int count) {
        this(DEFAULT_DELAY, count);
    }

    public ConcurrentLiveHashMap(@Nonnegative long delay) {
        this(delay, DEFAULT_CAPACITY);
    }

    public ConcurrentLiveHashMap(@Nonnegative long delay, @Nonnegative int count) {
        this(delay, count, true);
    }

    public ConcurrentLiveHashMap(@Nonnegative long delay, @Nonnegative int count, boolean autoStart) {
        this.nodes = new AtomicReferenceArray<>(count);
        this.delay = delay;
        this.sizeLimit = count;
        this.size = new AtomicInteger(0);
        this.started = new AtomicBoolean(false);
        if(autoStart)
            start();
    }

    static boolean eq(Object o1, Object o2) {
        if(o1 == null)
            return o2 == null || o2.equals(o1);
        else
            return o1.equals(o2);
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    @Contract("null -> false")
    public boolean containsKey(Object key) {
        if(key == null)
            return false;

        int hash = hash(key);
        long nodesLock = this.nodesLock.readLock();
        try {
            int bucket = getBucket(hash, nodes.length());
            LockedNode<K, V> node = nodes.get(bucket);
            if(node == null)
                return false;
            long lock = node.lock.readLock();
            try {
                if(node.key.equals(key))
                    return true;
                Node<K, V> node1 = node.next;
                if(node1 != null) {
                    do {
                        if(node1.key.equals(key))
                            return true;
                    } while((node1 = node1.next) != null);
                }
            } finally {
                node.lock.unlockRead(lock);
            }
        } finally {
            this.nodesLock.unlockRead(nodesLock);
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        long nodesLock = this.nodesLock.readLock();
        try {
            for(int i = 0; i < nodes.length(); i++) {
                LockedNode<K, V> node = nodes.get(i);
                if(node != null) {
                    long lock = node.lock.readLock();
                    try {
                        if(eq(node.value, value))
                            return true;
                        Node<K, V> node1 = node.next;
                        if(node1 != null) {
                            do {
                                if(eq(node1.value, value))
                                    return true;
                            } while((node1 = node1.next) != null);
                        }
                    } finally {
                        node.lock.unlockRead(lock);
                    }
                }
            }
        } finally {
            this.nodesLock.unlockRead(nodesLock);
        }
        return false;
    }

    /**
     * @param consumer execute in read
     */
    protected <R> R processNode(Object key, boolean write, Predicate<Node<K, V>> isSuitable, Function<Node<K, V>, R> consumer) {
        if(key == null)
            return null;

        int hash = hash(key);
        long nodesLock = this.nodesLock.readLock();
        try {
            int bucket = getBucket(hash, nodes.length());
            LockedNode<K, V> node = nodes.get(bucket);
            if(node == null)
                return null;
            long lock = write ? node.lock.writeLock() : node.lock.readLock();
            try {
                if(node.key.equals(key) && isSuitable.test(node))
                    return consumer.apply(node);
                Node<K, V> node1 = node.next;
                if(node1 != null) {
                    do {
                        if(node1.key.equals(key) && isSuitable.test(node1))
                            return consumer.apply(node1);
                    } while((node1 = node1.next) != null);
                }
            } finally {
                node.lock.unlock(lock);
            }
        } finally {
            this.nodesLock.unlockRead(nodesLock);
        }
        return null;
    }

    @Override
    @Contract("null -> null")
    public V get(Object key) {
        if(key == null)
            return null;

        int hash = hash(key);
        long nodesLock = this.nodesLock.readLock();
        try {
            int bucket = getBucket(hash, nodes.length());
            LockedNode<K, V> node = nodes.get(bucket);
            if(node == null)
                return null;
            long lock = node.lock.readLock();
            try {
                if(node.key.equals(key))
                    return node.value;
                Node<K, V> node1 = node.next;
                if(node1 != null) {
                    do {
                        if(node1.key.equals(key))
                            return node.value;
                    } while((node1 = node1.next) != null);
                }
            } finally {
                node.lock.unlockRead(lock);
            }
        } finally {
            this.nodesLock.unlockRead(nodesLock);
        }
        return null;
    }

    @Override
    public V put(@Nonnull K key, V value) {
        return put(key, value, defaultPeriod);
    }

    public V put(@Nonnull K key, V value, long period) {
        int hash = hash(key);
        long nodesLock = this.nodesLock.readLock();
        V old = null;
        try {
            int bucket = getBucket(hash, nodes.length());
            LockedNode<K, V> node = nodes.get(bucket);
            if(node == null) {
                System.out.println("Before switch to write");
                long _temp = nodesLock;
                this.nodesLock.unlockRead(nodesLock);
                nodesLock = this.nodesLock.writeLock();
                System.out.println("Switch to write: from: " + _temp + " to " + nodesLock);
                bucket = getBucket(hash, nodes.length());
                node = nodes.get(bucket);
                if(node == null) {
                    LockedNode<K, V> newNode = new LockedNode<>(key, value, period);
                    nodes.set(bucket, newNode);
                    size.incrementAndGet();
                    System.out.println("Before checkSize");
                    long temp_old = nodesLock;
                    nodesLock = checkSize(0, nodesLock, true);
                    System.out.println("Before checkSize:" + temp_old + ", after: " + nodesLock);
                    return null;
                }
            }
            long lock = node.lock.writeLock();

            try {
                if(node.key.equals(key)) {
                    System.out.println("Node equality: first: " + node.key + ", second: " + key);
                    old = node.value;
                    node.value = value;
                    node.reset();
                    node.period = period;
                } else {
                    Node<K, V> newNode = new Node<>(key, value, period);
                    if(node.next == null) {
                        node.next = newNode;
                        size.incrementAndGet();
                        nodesLock = checkSize(0, nodesLock, false);
                    } else {
                        Node<K, V> n = node.next;
                        if(n.key.equals(key)) {
                            System.out.println("Node equality: first: " + node.key + ", second: " + key);
                            old = n.value;
                            n.value = value;
                            n.reset();
                            n.period = period;
                        } else {
                            boolean setNext = true;
                            if(n.next != null)
                                while((n = n.next).next != null) {
                                    if(n.key.equals(key)) {
                                        old = n.value;
                                        n.value = value;
                                        n.reset();
                                        n.period = period;
                                        setNext = false;
                                    }
                                }
                            if(setNext) {
                                n.next = newNode;
                                size.incrementAndGet();
                                System.out.println("Before checkSize on setNext");
                                long temp_old = nodesLock;
                                nodesLock = checkSize(0, nodesLock, false);
                                System.out.println("Before checkSize:" + temp_old + ", after: " + nodesLock);
                            }
                        }
                    }
                }
            } finally {
                node.lock.unlockWrite(lock);
            }


        } finally {
            System.out.println("Write: " + this.nodesLock.isWriteLocked() + ", read: " + this.nodesLock.isReadLocked() + ", lock: " + nodesLock);
            this.nodesLock.unlock(nodesLock);
        }
        return old;
    }

    private long checkSize(int required, final long lock, boolean write) {
        long l = lock;
        if(!write) {
            System.out.println("checkSize: try write lock OK!");
            l = nodesLock.tryConvertToWriteLock(lock);
            System.out.println("checkSize: try write lock ended: l: " + l);
            if(l == 0) {
                System.out.println("checkSize: try acquire failure!");
                nodesLock.unlockRead(lock);
                l = nodesLock.writeLock();
                System.out.println("checkSize: write lock acquired!");
            }
        }
        try {
            if(size.get() + required > sizeLimit) {
                int minCapacity = sizeLimit << 1;
                int newCapacity = powerOfTwoFor(minCapacity);
                sizeLimit = newCapacity << 1;
                size.set(0);
                AtomicReferenceArray<LockedNode<K, V>> old = nodes;
                nodes = new AtomicReferenceArray<>(newCapacity);

                for(int i = 0; i < old.length(); i++) {
                    LockedNode<K, V> node = old.get(i);
                    if(node != null) {
                        addFast(node);
                        Node<K, V> next = node.next;
                        if(next != null) {
                            addFast(next);
                            while(next.next != null) {
                                next = next.next;
                                if(next != null)
                                    addFast(next);
                            }
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("checkSize: Before return: lock: " + lock + ", return lock: " + l);
        return l;
    }

    /**
     * REMOVE {@link CachedHashMap.Entry#next}. DO NOT CHECK SIZE
     */
    private void addFast(Map.Entry<K, V> entry) {
        int bucket = getBucket(hash(entry.getKey()), nodes.length());
        LockedNode<K, V> head = nodes.get(bucket);
        if(head == null)
            nodes.set(bucket, new LockedNode<>(entry.getKey(), entry.getValue(), ((Periodic) entry).getPeriod()));
        else {
            Node<K, V> next = head.next;
            if(next != null) {
                while(next.next != null)
                    next = next.next;
                next.next = new Node<>(entry.getKey(), entry.getValue(), ((Periodic) entry).getPeriod());
            } else
                head.next = new Node<>(entry.getKey(), entry.getValue(), ((Periodic) entry).getPeriod());
        }
        size.incrementAndGet();
    }

    @Override
    public V remove(Object key) {
        int hash = hash(key);
        long nodesLock = this.nodesLock.writeLock();
        try {
            return removeActual(key, hash);
        } finally {
            this.nodesLock.unlock(nodesLock);
        }
    }

    private V removeActual(Object key, int hash) {
        int bucket = getBucket(hash, nodes.length());
        LockedNode<K, V> node = nodes.get(bucket);
        if(node == null)
            return null;
        long lock = node.lock.writeLock();
        try {
            if(node.key.equals(key)) {
                if(node.next != null) {
                    LockedNode<K, V> node1 = new LockedNode<>(node.next.key, node.next.value, node.next.period);
                    node1.startTime = node.next.startTime;
                    node1.next = node.next.next;
                    nodes.set(bucket, node1);
                    size.decrementAndGet();
                    return node.value;
                } else {
                    nodes.set(bucket, null);
                    size.decrementAndGet();
                    return node.value;
                }
            }
            Node<K, V> node1 = node.next;
            Node<K, V> last = null;
            if(node1 != null) {
                do {
                    if(node1.key.equals(key))
                        break;
                    last = node1;
                } while((node1 = node1.next) != null);
                if(node1 == null)
                    return null;
                if(last != null)
                    last.next = node1.next;
                else
                    node.next = node1.next;
                size.decrementAndGet();
                return node1.value;
            }
        } finally {
            node.lock.unlock(lock);
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    public void putAll(@NotNull Map<? extends K, ? extends V> m, long period) {
        m.forEach((o, o2) -> put(o, o2, period));
    }

    @Override
    public void clear() {
        long lock = nodesLock.writeLock();
        try {
            nodes = new AtomicReferenceArray<>(sizeLimit);
            size.set(0);
        } finally {
            nodesLock.unlockWrite(lock);
        }
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = entrySet;
        if(entries == null)
            entrySet = entries = new EntrySetImpl();
        return entries;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        long lock = nodesLock.readLock();
        try {
            return containsKey(key) ? get(key) : defaultValue;//Can do read lock inside read lock, because StampedLock is non-fair
        } finally {
            nodesLock.unlockRead(lock);
        }
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        long lock = nodesLock.readLock();
        try {
            for(int i = 0; i < nodes.length(); i++) {
                LockedNode<K, V> node = nodes.get(i);
                if(node == null)
                    continue;
                long l = node.lock.readLock();
                try {
                    action.accept(node.key, node.value);
                    Node<K, V> next = node.next;
                    if(next != null)
                        do {
                            action.accept(next.key, next.value);
                        } while((next = next.next) != null);
                } finally {
                    node.lock.unlock(l);
                }
            }
        } finally {
            nodesLock.unlockRead(lock);
        }
    }

    public void forEach(TripleConsumer<? super K, ? super V, Long> action) {
        long lock = nodesLock.readLock();
        try {
            for(int i = 0; i < nodes.length(); i++) {
                LockedNode<K, V> node = nodes.get(i);
                if(node == null)
                    continue;
                long l = node.lock.readLock();
                try {
                    action.accept(node.key, node.value, node.period);
                    Node<K, V> next = node.next;
                    if(next != null)
                        do {
                            action.accept(next.key, next.value, next.period);
                        } while((next = next.next) != null);
                } finally {
                    node.lock.unlock(l);
                }
            }
        } finally {
            nodesLock.unlockRead(lock);
        }
    }

    @Override
    public boolean remove(@NotNull Object key, Object value) {
        int hash = hash(key);
        long nodesLock = this.nodesLock.writeLock();
        try {
            int bucket = getBucket(hash, nodes.length());
            LockedNode<K, V> node = nodes.get(bucket);
            if(node == null)
                return false;
            long lock = node.lock.writeLock();
            try {
                if(node.key.equals(key) && eq(node.value, value)) {
                    if(node.next != null) {
                        LockedNode<K, V> node1 = new LockedNode<>(node.next.key, node.next.value, node.next.period);
                        node1.startTime = node.next.startTime;
                        node1.next = node.next.next;
                        nodes.set(bucket, node1);
                        size.decrementAndGet();
                        return true;
                    } else {
                        nodes.set(bucket, null);
                        size.decrementAndGet();
                        return true;
                    }
                }
                Node<K, V> node1 = node.next;
                Node<K, V> last = null;
                if(node1 != null) {
                    do {
                        if(node1.key.equals(key) && eq(node.value, value))
                            break;
                        last = node1;
                    } while((node1 = node1.next) != null);
                    if(node1 == null)
                        return false;
                    if(last != null)
                        last.next = node1.next;
                    else
                        node.next = node1.next;
                    size.decrementAndGet();
                    return true;
                }
            } finally {
                node.lock.unlock(lock);
            }
        } finally {
            this.nodesLock.unlock(nodesLock);
        }
        return false;
    }

    @Override
    public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
        Boolean ok = processNode(key, true, kvNode -> eq(kvNode.value, oldValue), kvNode -> {
            kvNode.value = newValue;
            return true;
        });
        return ok != null && ok;
    }

    @Override
    public V replace(@NotNull K key, @NotNull V value) {
        return processNode(key, true, kvNode -> true, kvNode -> {
            V o = kvNode.value;
            kvNode.value = value;
            return o;
        });
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        long lock = nodesLock.readLock();
        try {
            for(int i = 0; i < nodes.length(); i++) {
                LockedNode<K, V> node = nodes.get(i);
                if(node == null)
                    continue;
                long l = node.lock.writeLock();
                try {
                    node.value = function.apply(node.key, node.value);
                    node.reset();
                    Node<K, V> next = node.next;
                    if(next != null)
                        do {
                            next.value = function.apply(next.key, next.value);
                            node.reset();
                        } while((next = next.next) != null);
                } finally {
                    node.lock.unlockWrite(l);
                }
            }
        } finally {
            nodesLock.unlockRead(lock);
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function, TripleFunction<? super K, ? super V, Long, Long> timeRemap) {
        long lock = nodesLock.readLock();
        try {
            for(int i = 0; i < nodes.length(); i++) {
                LockedNode<K, V> node = nodes.get(i);
                if(node == null)
                    continue;
                long l = node.lock.writeLock();
                try {
                    node.value = function.apply(node.key, node.value);
                    node.period = timeRemap.apply(node.key, node.value, node.period);
                    node.reset();
                    Node<K, V> next = node.next;
                    if(next != null)
                        do {
                            next.value = function.apply(next.key, next.value);
                            node.period = timeRemap.apply(node.key, node.value, node.period);
                            node.reset();
                        } while((next = next.next) != null);
                } finally {
                    node.lock.unlockWrite(l);
                }
            }
        } finally {
            nodesLock.unlockRead(lock);
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if(!containsKey(key)) {
            V apply = mappingFunction.apply(key);
            put(key, apply);
            return apply;
        }
        return get(key);
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, BiFunction<? super K, ? super V, Long> timeMapping) {
        if(!containsKey(key)) {
            V value = mappingFunction.apply(key);
            long period = timeMapping.apply(key, value);
            put(key, value, period);
            return value;
        }
        return get(key);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return processNode(key, true, kvNode -> true, kvNode -> {
            kvNode.reset();
            kvNode.value = remappingFunction.apply(key, kvNode.value);
            return kvNode.value;
        });
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, TripleFunction<? super K, ? super V, Long, Long> timeRemap) {
        return processNode(key, true, kvNode -> true, kvNode -> {
            kvNode.value = remappingFunction.apply(key, kvNode.value);
            kvNode.period = timeRemap.apply(key, kvNode.value, kvNode.period);
            kvNode.reset();
            return kvNode.value;
        });
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if(!containsKey(key)) {
            V n = remappingFunction.apply(key, null);
            put(key, n);
            return n;
        } else
            return processNode(key, true, kvNode -> true, kvNode -> {
                kvNode.value = remappingFunction.apply(key, kvNode.value);
                kvNode.reset();
                return kvNode.value;
            });
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, TripleFunction<? super K, ? super V, Long, Long> timeRemap) {
        if(!containsKey(key)) {
            V n = remappingFunction.apply(key, null);
            long period = timeRemap.apply(key, n, -1L);
            put(key, n, period);
            return n;
        } else
            return processNode(key, true, kvNode -> true, kvNode -> {
                kvNode.value = remappingFunction.apply(key, kvNode.value);
                kvNode.period = timeRemap.apply(key, kvNode.value, kvNode.period);
                kvNode.reset();
                return kvNode.value;
            });
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value :
                remappingFunction.apply(oldValue, value);
        if(newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

    public V merge(K key, V value, long period, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value :
                remappingFunction.apply(oldValue, value);
        if(newValue == null) {
            remove(key);
        } else {
            put(key, newValue, period);
        }
        return newValue;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if(!containsKey(key))
            return put(key, value);
        return get(key);
    }

    public V putIfAbsent(K key, V value, long period) {
        if(!containsKey(key))
            return put(key, value, period);
        return get(key);
    }

    public long getLiveTime(K key) {
        Long aLong = processNode(key, false, kvNode -> true, Node::getLiveTime);
        return aLong == null ? -1 : aLong;
    }

    public long getPeriod(K key) {
        Long aLong = processNode(key, false, kvNode -> true, Node::getPeriod);
        return aLong == null ? -1 : aLong;
    }

    public boolean resetTime(K key) {
        return processNode(key, true, kvNode -> true, kvNode -> {
            kvNode.reset();
            return true;
        }) != null;
    }

    /**
     * Resets start time
     */
    public boolean setPeriod(K key, long period) {
        return processNode(key, true, kvNode -> true, kvNode -> {
            kvNode.period = period;
            kvNode.reset();
            return true;
        }) != null;
    }

    @Override
    public void start() {
        if(started.get())
            return;
        tickingPool.startTicking(this, delay, TimeUnit.MILLISECONDS);
        started.set(true);
    }

    @Override
    public void stop() {
        if(!started.get())
            return;
        tickingPool.stopTicking(this);
        started.set(false);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    public void setDelay(long delay) {
        if(started.get()) {
            stop();
            this.delay = delay;
            start();
        } else
            this.delay = delay;
    }

    public void setTickingPool(TickingPool tickingPool) {
        if(!isRunning())
            this.tickingPool = tickingPool;
        else {
            stop();
            this.tickingPool = tickingPool;
            start();
        }
    }

    @Override
    public void tick() {
        if(!started.get())
            return;
        long current = System.currentTimeMillis();
        long lock = nodesLock.readLock();
        try {
            for(int i = 0; i < nodes.length(); i++) {
                LockedNode<K, V> node = nodes.get(i);
                if(node == null)
                    continue;
                if(node.isExpired(current))
                    removeQueue.add(node.key);
                long l = node.lock.readLock();
                try {
                    Node<K, V> next = node.next;
                    if(next != null)
                        do {
                            if(next.isExpired(current))
                                removeQueue.add(next.key);
                        } while((next = next.next) != null);
                } finally {
                    node.lock.unlock(l);
                }
            }
        } finally {
            nodesLock.unlockRead(lock);
        }
        long write = nodesLock.writeLock();
        try {
            K key;
            while((key = removeQueue.poll()) != null)
                removeActual(key, hash(key));
        } finally {
            nodesLock.unlockWrite(write);
        }
    }

    @Override
    protected ConcurrentLiveHashMap<K, V> clone() {
        ConcurrentLiveHashMap<K, V> map = new ConcurrentLiveHashMap<>(sizeLimit);
        map.started.set(started.get());
        map.size.set(size.get());
        map.sizeLimit = sizeLimit;
        map.defaultPeriod = defaultPeriod;
        map.tickingPool = tickingPool;
        map.delay = delay;

        for(int i = 0; i < nodes.length(); i++) {
            map.nodes.set(i, nodes.get(i) == null ? null : nodes.get(i).clone());
        }
        return map;
    }

    private interface Periodic {
        long getPeriod();
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static class Node<K, V> implements Map.Entry<K, V>, Periodic, Cloneable {
        protected final K key;
        protected volatile V value;
        protected volatile Node<K, V> next;

        protected volatile long startTime;
        protected volatile long period;

        public Node(K key, V value, long period) {
            this.key = key;
            this.value = value;
            this.period = period;
            this.startTime = System.currentTimeMillis();
        }


        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

        public void reset() {
            this.startTime = System.currentTimeMillis();
        }

        public boolean isExpired(long current) {
            return (current - startTime) >= period;
        }

        public long getLiveTime() {
            return getLiveTime(System.currentTimeMillis());
        }

        public long getLiveTime(long current) {
            return current - startTime;
        }

        @Override
        public Node<K, V> clone() {
            return new Node<>(key, value, next == null ? null : next.clone(), startTime, period);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof Node)) {
                if(o instanceof Map.Entry)
                    return eq(((Entry) o).getKey(), getKey()) && eq(((Entry) o).getValue(), getValue());
            }
            if(!super.equals(o)) return false;

            Node<?, ?> node = (Node<?, ?>) o;

            if(getStartTime() != node.getStartTime()) return false;
            if(getPeriod() != node.getPeriod()) return false;
            if(getKey() != null ? !getKey().equals(node.getKey()) : node.getKey() != null) return false;
            if(getValue() != null ? !getValue().equals(node.getValue()) : node.getValue() != null) return false;
            return getNext() != null ? getNext().equals(node.getNext()) : node.getNext() == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
            result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
            result = 31 * result + (getNext() != null ? getNext().hashCode() : 0);
            result = 31 * result + (int) (getStartTime() ^ (getStartTime() >>> 32));
            result = 31 * result + (int) (getPeriod() ^ (getPeriod() >>> 32));
            return result;
        }
    }

    @Getter
    @Setter
    private static final class LockedNode<K, V> extends Node<K, V> implements Map.Entry<K, V>, Periodic {
        private final transient StampedLock lock = new StampedLock();

        public LockedNode(K key, V value, long period) {
            super(key, value, period);
        }

        @Override
        public LockedNode<K, V> clone() {
            LockedNode<K, V> node = new LockedNode<>(key, value, period);
            node.startTime = startTime;
            node.next = next == null ? null : next.clone();
            return node;
        }
    }

    private final class EntrySetImpl extends AbstractSet<Entry<K, V>> {

        @Override
        public int size() {
            return ConcurrentLiveHashMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ConcurrentLiveHashMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return ConcurrentLiveHashMap.this.containsKey(((Entry<K, V>) o).getKey());
        }

        @NotNull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new IteratorImpl();
        }

        @Override
        public boolean add(Entry<K, V> kvEntry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            Entry<K, V> obj = (Entry<K, V>) o;
            return ConcurrentLiveHashMap.this.remove(obj.getKey(), obj.getValue());
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return c.stream()
                    .map(o -> (Entry<K, V>) o)
                    .map(Entry::getKey)
                    .map(ConcurrentLiveHashMap.this::containsKey)
                    .reduce(Boolean::logicalAnd)
                    .orElse(false);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return removeIf(kvEntry -> !c.contains(kvEntry));
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return removeIf(c::contains);
        }

        @Override
        public void clear() {
            ConcurrentLiveHashMap.this.clear();
        }

        private final class IteratorImpl implements Iterator<Entry<K, V>> {
            private volatile int lastIndex;
            private volatile Node<K, V> lastNode;
            private volatile LockedNode<K, V> root;
            private volatile K current;

            @Override
            public boolean hasNext() {
                long lock = nodesLock.readLock();
                try {
                    if(lastNode == null || lastNode.next == null) {
                        for(int i = lastIndex; i < nodes.length(); i++) {
                            if(nodes.get(i) != null)
                                return true;
                        }
                        return false;
                    }
                    return true;
                } finally {
                    nodesLock.unlockRead(lock);
                }
            }

            @Override
            public Entry<K, V> next() {
                long lock = nodesLock.readLock();
                try {
                    if(root == null) {
                        LockedNode<K, V> nextRoot = findNextRoot();
                        current = nextRoot.key;
                        return nextRoot;
                    }
                    StampedLock lock1 = root.lock;
                    long readLock = lock1.readLock();
                    try {
                        if(lastNode == null)
                            lastNode = root.next;
                        if(lastNode == null) {
                            lock1.unlockRead(readLock);
                            readLock = 0;
                            root = null;
                            Entry<K, V> next = next();
                            current = next.getKey();
                            return next;
                        } else {
                            Node<K, V> ret = lastNode;
                            if(lastNode.next != null)
                                lastNode = lastNode.next;
                            else {
                                lastNode = null;
                                findNextRoot();
                            }
                            current = ret.key;
                            return ret;
                        }
                    } finally {
                        if(readLock != 0)
                            lock1.unlockRead(readLock);
                    }
                } finally {
                    nodesLock.unlockRead(lock);
                }
            }

            private LockedNode<K, V> findNextRoot() {
                for(int i = lastIndex; i < nodes.length(); i++) {
                    LockedNode<K, V> node = nodes.get(i);
                    if(node != null) {
                        lastIndex = i + 1;
                        root = node;
                        return node;
                    }
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                if(current == null)
                    return;
                ConcurrentLiveHashMap.this.remove(current);
            }
        }
    }
}
