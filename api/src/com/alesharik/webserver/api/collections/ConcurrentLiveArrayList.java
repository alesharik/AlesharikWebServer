package com.alesharik.webserver.api.collections;

import com.alesharik.webserver.api.ticking.OneThreadTickingPool;
import com.alesharik.webserver.api.ticking.Tickable;
import one.nio.lock.RWLock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

//TODO write configurable pool
public class ConcurrentLiveArrayList<V> extends ArrayListWrapper<V> implements Tickable {
    private static final OneThreadTickingPool DEFAULT_POOL = new OneThreadTickingPool();
    private static final int DEFAULT_LIFE_TIME = 60 * 1000;
    private static final long DEFAULT_DELAY = 1000;
    private static final int DEFAULT_CAPACITY = 16;

    private ArrayList<Long> times;
    private final long delay;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    //    private final StampedLock lock = new StampedLock(); DON'T WORKS!
    private final RWLock lock = new RWLock();

    public ConcurrentLiveArrayList() {
        this(DEFAULT_DELAY, DEFAULT_CAPACITY);
    }

    public ConcurrentLiveArrayList(int count) {
        this(DEFAULT_DELAY, count);
    }

    public ConcurrentLiveArrayList(long delay) {
        this(delay, DEFAULT_CAPACITY);
    }

    public ConcurrentLiveArrayList(long delay, int count) {
        this(delay, count, true);
    }

    public ConcurrentLiveArrayList(long delay, int count, boolean autoStart) {
        super(count);
        this.times = new ArrayList<>(count);
        this.delay = delay;
        if(autoStart) {
            start();
        }
    }

    public boolean add(V v) {
        return add(v, DEFAULT_LIFE_TIME);
    }

    public boolean add(V v, long lifeTime) {
        try {
            lock.lockRead();
            int address = (super.size() > 0) ? super.size() - 1 : 0;

            lock.upgrade();
            super.add(address, v);
            times.add(address, lifeTime);
            lock.downgrade();
        } finally {
            lock.unlockRead();
        }
        return true;
    }

    @Override
    public V set(int index, V element) {
        return set(index, element, DEFAULT_LIFE_TIME);
    }

    public V set(int index, V element, long lifeTime) {
        V ret = null;
        try {
            lock.lockRead();
            V var = super.get(index);
            if(var != null) {
                lock.upgrade();
                times.set(index, lifeTime);
                ret = super.set(index, element);
                lock.downgrade();
            }
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public void add(int index, V element) {
        add(index, element, DEFAULT_LIFE_TIME);
    }

    public void add(int index, V element, long lifeTime) {
        try {
            lock.lockRead();
            lock.upgrade();
            super.add(index, element);
            times.add(index, lifeTime);
        } finally {
            lock.downgrade();
            lock.unlockRead();
        }
    }

    @Override
    public V remove(int index) {
        return remove0(index);
    }

    private V remove0(int index) {
        V ret = null;
        try {
            lock.lockRead();
            V var = super.get(index);

            if(var != null) {
                lock.upgrade();
                times.remove(index);
                ret = super.remove(index);
                lock.downgrade();
            }
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public int indexOf(Object o) {
        int ret;
        try {
            lock.lockRead();
            ret = super.indexOf(o);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public int lastIndexOf(Object o) {
        int ret;
        try {
            lock.lockRead();
            ret = super.lastIndexOf(o);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public void clear() {
        try {
            lock.lockWrite();
            super.clear();
            times.clear();
        } finally {
            lock.unlockWrite();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        return addAll(index, c, DEFAULT_LIFE_TIME);
    }

    public <T extends V> boolean addAll(int index, Collection<T> c, long lifeTime) {
        int index1 = size() - 1 + index;
        if(index1 < 0) {
            index1 = 0;
        }
        for(T elem : c) {
            add(index1, elem, lifeTime);
        }
        return true;
    }

    @Override
    public Iterator<V> iterator() {
        Iterator<V> ret;
        try {
            lock.lockRead();
            ret = super.iterator();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public ListIterator<V> listIterator() {
        ListIterator<V> ret;
        try {
            lock.lockRead();
            ret = super.listIterator();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        ListIterator<V> ret;
        try {
            lock.lockRead();
            ret = super.listIterator(index);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    public Map<Long, V> timeMap() {
        HashMap<Long, V> map;
        try {
            lock.lockRead();
            map = new HashMap<>(size());
            forEach((v, aLong) -> map.put(aLong, v));
        } finally {
            lock.unlockRead();
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        List<V> ret;
        try {
            lock.lockRead();
            ret = super.subList(fromIndex, toIndex);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        boolean ret;
        try {
            lock.lockRead();
            ret = super.isEmpty();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public boolean contains(Object o) {
        boolean ret;
        try {
            lock.lockRead();
            ret = super.contains(o);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public Object[] toArray() {
        Object[] objects;
        try {
            lock.lockRead();
            objects = super.toArray();
        } finally {
            lock.unlockRead();
        }
        return objects;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public <C> C[] toArray(C[] a) {
        C[] objects;
        try {
            lock.lockRead();
            objects = super.toArray(a);
        } finally {
            lock.unlockRead();
        }
        return objects;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = false;
        try {
            lock.lockRead();
            boolean contains = super.contains(o);
            if(contains) {
                lock.upgrade();
                ret = removeNonSync(o);
                lock.downgrade();
            }
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    private boolean removeNonSync(Object o) {
        int i = 0;
        boolean ret = false;
        Iterator<V> iterator = super.iterator();
        while(iterator.hasNext()) {
            V next = iterator.next();
            if(o.equals(next)) {
                super.remove(i);
                times.remove(i);
                ret = true;
                break;
            }
            i++;
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean ret;
        try {
            lock.lockRead();
            ret = super.containsAll(c);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        return addAll(0, c);
    }

    public boolean addAll(Collection<? extends V> c, long lifeTime) {
        return addAll(0, c, lifeTime);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for(Object o : c) {
            if(!remove(o)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> c) {
        clear();
        return c.stream()
                .map(o -> add((V) o))
                .allMatch(Boolean::booleanValue);
    }

    public boolean retainAll(Collection<? extends V> c, long lifeTime) {
        clear();
        return addAll(c, lifeTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CachedArrayList {");
        forEach((v, aLong) -> {
            sb.append("value=");
            sb.append(v);
            sb.append(", time=");
            sb.append(times);
            sb.append(";");
        });
        sb.replace(sb.lastIndexOf(";"), sb.lastIndexOf(";"), "");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void replaceAll(UnaryOperator<V> operator) {
        for(int i = 0; i < size(); i++) {
            set(i, operator.apply(get(i)));
        }
    }

    public void replaceAll(UnaryOperator<V> operator, UnaryOperator<Long> lifeTimeOperator) {
        for(int i = 0; i < size(); i++) {
            set(i, operator.apply(get(i)), lifeTimeOperator.apply(getTime(i)));
        }
    }

    /**
     * This method not used due specific of time holding
     */
    //TODO write this
    @Override
    public void sort(Comparator<? super V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<V> spliterator() {
        Spliterator<V> ret;
        try {
            lock.lockRead();
            ret = super.spliterator();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public boolean removeIf(Predicate<? super V> filter) {
        for(int i = 0; i < size(); i++) {
            V value = get(i);
            if(filter.test(value) && !remove(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<V> stream() {
        Stream<V> ret;
        try {
            lock.lockRead();
            ret = super.stream();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public Stream<V> parallelStream() {
        Stream<V> ret;
        try {
            lock.lockRead();
            ret = super.parallelStream();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        try {
            lock.lockRead();
            super.forEach(action);
        } finally {
            lock.unlockRead();
        }
    }

    public void forEach(BiConsumer<? super V, ? super Long> action) {
        try {
            lock.lockRead();
            for(int i = 0; i < super.size(); i++) {
                action.accept(super.get(i), times.get(i));
            }
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ConcurrentLiveArrayList list;
        try {
            lock.lockRead();
            list = ((ConcurrentLiveArrayList) super.clone());
            list.times = (ArrayList) times.clone();
        } finally {
            lock.unlockRead();
        }
        return list;
    }

    @Override
    public V get(int index) {
        V ret;
        try {
            lock.lockRead();
            ret = super.get(index);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    public Long getTime(int index) {
        return getTime0(index);
    }

    private Long getTime0(int index) {
        Long ret;
        try {
            lock.lockRead();
            ret = times.get(index);
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public int size() {
        int ret;
        try {
            lock.lockRead();
            ret = super.size();
        } finally {
            lock.unlockRead();
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        ConcurrentLiveArrayList that;
        lock.lockRead();
        try {
            that = this;
        } finally {
            lock.unlockRead();
        }
        if(that == o) return true;
        if(!(o instanceof ConcurrentLiveArrayList)) return false;
        if(!super.equals(o)) return false;

        ConcurrentLiveArrayList<?> list = (ConcurrentLiveArrayList<?>) o;

        return that.times != null ? that.times.equals(list.times) : list.times == null;
    }

    @Override
    public int hashCode() {
        int result;
        try {
            lock.lockRead();
            result = super.hashCode();
            result = 31 * result + (times != null ? times.hashCode() : 0);
        } finally {
            lock.unlockRead();
        }
        return result;
    }

    void updateValues(long delta) {
        for(int i = 0; i < size(); i++) {
            long current = getTime0(i) - delta;
            if(current < 0) {
                remove(i);
            } else {
                times.set(i, current);
            }
        }
    }

    public void start() {
        if(!isStarted.get()) {
//            TickingPool.addArrayList(this, delay);
            DEFAULT_POOL.startTicking(this, delay);
            isStarted.set(true);
        }
    }

    public void stop() {
        if(isStarted.get()) {
//            TickingPool.removeArrayList(this, delay);
            DEFAULT_POOL.stopTicking(this);
            isStarted.set(false);
        }
    }

    @Override
    public void tick() throws Exception {
        updateValues(delay);
    }
}
