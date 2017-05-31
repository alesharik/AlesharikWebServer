package com.alesharik.webserver.api.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * This list updates only on method call.
 * If method has no lifeTime, it set DEFAULT_LIFE_TIME as lifeTime
 */
public class CachedArrayList<V> extends ArrayListWrapper<V> implements Cloneable {
    private static final int DEFAULT_LIFE_TIME = 60 * 1000;

    private ArrayList<Long> times;
    private long lastTime;
    private AtomicBoolean isRunning = new AtomicBoolean();

    public CachedArrayList() {
        super();
        this.times = new ArrayList<>(16);
        lastTime = System.currentTimeMillis();
        isRunning = new AtomicBoolean(true);
    }

    public CachedArrayList(int count) {
        super(count);
        this.times = new ArrayList<>(count);
        lastTime = System.currentTimeMillis();
    }

    public boolean add(V v) {
        return add(v, DEFAULT_LIFE_TIME);
    }

    public boolean add(V v, long lifeTime) {
        int address = super.size();
        super.add(address, v);
        times.add(address, lifeTime);
        return true;
    }

    @Override
    public void add(int index, V element) {
        add(index, element, DEFAULT_LIFE_TIME);
    }

    public void add(int index, V element, long lifeTime) {
        super.add(index, element);
        times.add(index, lifeTime);
    }

    @Override
    public V set(int index, V element) {
        return set(index, element, DEFAULT_LIFE_TIME);
    }

    public V set(int index, V element, long lifeTime) {
        update();
        if(super.get(index) != null) {
            times.set(index, lifeTime);
            return super.set(index, element);
        } else {
            return null;
        }
    }

    @Override
    public V remove(int index) {
        update();
        return remove0(index);
    }

    private V remove0(int index) {
        if(super.get(index) != null) {
            times.remove(index);
            return super.remove(index);
        } else {
            return null;
        }
    }

    @Override
    public int indexOf(Object o) {
        update();
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        update();
        return super.lastIndexOf(o);
    }

    @Override
    public void clear() {
        super.clear();
        times.clear();
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        return addAll(index, c, DEFAULT_LIFE_TIME);
    }

    public boolean addAll(int index, Collection<? extends V> c, long lifeTime) {
        c.forEach(o -> add(super.size() + index, o, lifeTime));
        return true;
    }

    @Override
    public Iterator<V> iterator() {
        update();
        return super.iterator();
    }

    @Override
    public ListIterator<V> listIterator() {
        update();
        return super.listIterator();
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        update();
        return super.listIterator(index);
    }

    @Deprecated
    public HashMap<Long, V> timeMap() {
//        HashMap<V, Long> map = new HashMap<>(size());
//        forEach((v, aLong) -> map.put(aLong, v));
//        return map;
        return null;
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        update();
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public boolean isEmpty() {
        update();
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        update();
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        update();
        return super.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public <C> C[] toArray(C[] a) {
        update();
        return super.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        if(contains(o)) {
            int i = 0;
            for(Object o1 : this) {
                if(o1.equals(o)) {
                    times.remove(i);
                    super.remove(i);
                    return true;
                }
                i++;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        update();
        return super.containsAll(c);
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
        return addAll((Collection<? extends V>) c);
    }

    public boolean retainAll(Collection<? extends V> c, long lifeTime) {
        clear();
        return addAll(c, lifeTime);
    }

    @Override
    public String toString() {
        update();
        StringBuilder sb = new StringBuilder();
        sb.append("CoachedArrayList {");
        forEach((v, aLong) -> {
            sb.append("value=");
            sb.append(v);
            sb.append(", time=");
            sb.append(times);
            sb.append(';');
        });
        sb.replace(sb.lastIndexOf(";"), sb.lastIndexOf(";"), "");
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void replaceAll(UnaryOperator<V> operator) {
        update();
        for(int i = 0; i < size(); i++) {
            set(i, operator.apply(get(i)));
        }
    }

    public void replaceAll(UnaryOperator<V> operator, UnaryOperator<Long> lifeTimeOperator) {
        update();
        for(int i = 0; i < size(); i++) {
            set(i, operator.apply(get(i)), lifeTimeOperator.apply(getTime(i)));
        }
    }

    /**
     * This method not used due specific of time holding
     */
    @Override
    public void sort(Comparator<? super V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<V> spliterator() {
        return super.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super V> filter) {
        update();
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
        update();
        return super.stream();
    }

    @Override
    public Stream<V> parallelStream() {
        update();
        return super.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        update();
        super.forEach(action);
    }

    public void forEach(BiConsumer<? super V, ? super Long> action) {
        update();
        for(int i = 0; i < super.size(); i++) {
            action.accept(super.get(i), times.get(i));
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        update();
        CachedArrayList list = ((CachedArrayList) super.clone());
        list.times = new ArrayList<>(times);
        return list;
    }

    @Override
    public V get(int index) {
        update();
        return super.get(index);
    }

    public Long getTime(int index) {
        update();
        return getTime0(index);
    }

    private Long getTime0(int index) {
        return times.get(index);
    }

    @Override
    public int size() {
        update();
        return super.size();
    }

    @Override
    public boolean equals(Object o) {
        update();
        if(this == o) return true;
        if(!(o instanceof CachedArrayList)) return false;
        if(!super.equals(o)) return false;

        CachedArrayList<?> list = (CachedArrayList<?>) o;

        return times != null ? times.equals(list.times) : list.times == null;
    }

    @Override
    public int hashCode() {
        update();
        int result = super.hashCode();
        result = 31 * result + (times != null ? times.hashCode() : 0);
        return result;
    }

    private void update() {
        if(!isRunning.get()) {
            return;
        }
        long time = System.currentTimeMillis();
        updateValues(time - lastTime);
        lastTime = time;
    }

    private void updateValues(long delta) {
        for(int i = 0; i < super.size(); i++) {
            long current = getTime0(i) - delta;
            if(current < 0) {
                remove0(i);
            } else {
                times.set(i, current);
            }
        }
    }

    public void start() {
        isRunning.set(true);
    }

    public void stop() {
        isRunning.set(false);
    }

    public AtomicBoolean isRunning() {
        return this.isRunning;
    }
}
