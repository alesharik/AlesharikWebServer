package com.alesharik.webserver.api.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class LiveArrayList<V> extends ArrayListWrapper<V> {
    private static final int DEFAULT_LIFE_TIME = 60 * 1000;
    private static final long DEFAULT_DELAY = 1000;
    private static final int DEFAULT_CAPACITY = 16;

    private ArrayList<Long> times;
    private long delay;
    private boolean isStarted = false;

    public LiveArrayList() {
        this(DEFAULT_DELAY, DEFAULT_CAPACITY);
    }

    public LiveArrayList(int count) {
        this(DEFAULT_DELAY, count);
    }

    public LiveArrayList(long delay) {
        this(delay, DEFAULT_CAPACITY);
    }

    public LiveArrayList(long delay, int count) {
        this(delay, count, true);
    }

    public LiveArrayList test() {
        add((V) "test");
        return this;
    }

    public LiveArrayList(long delay, int count, boolean autoStart) {
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
        int address = (super.size() > 0) ? super.size() - 1 : 0;
        super.add(address, v);
        times.add(address, lifeTime);
        return true;
    }

    @Override
    public V set(int index, V element) {
        return set(index, element, DEFAULT_LIFE_TIME);
    }

    public V set(int index, V element, long lifeTime) {
        if(super.get(index) != null) {
            times.set(index, lifeTime);
            return super.set(index, element);
        } else {
            return null;
        }
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
    public V remove(int index) {
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
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
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
        c.forEach(o -> add(size() - 1 + index, o, lifeTime));
        return true;
    }

    @Override
    public Iterator<V> iterator() {
        return super.iterator();
    }

    @Override
    public ListIterator<V> listIterator() {
        return super.listIterator();
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        return super.listIterator(index);
    }

    public HashMap<Long, V> timeMap() {
        HashMap<Long, V> map = new HashMap<>(size());
        forEach((v, aLong) -> map.put(aLong, v));
        return map;
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public <C> C[] toArray(C[] a) {
        return super.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        if(contains(o)) {
            int i = 0;
            for(Object object : this) {
                i++;
                if(object.equals(o)) {
                    super.remove(i);
                    times.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("CoachedArrayList {");
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
        return super.stream();
    }

    @Override
    public Stream<V> parallelStream() {
        return super.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        super.forEach(action);
    }

    public void forEach(BiConsumer<? super V, ? super Long> action) {
        for(int i = 0; i < size(); i++) {
            action.accept(super.get(i), times.get(i));
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        LiveArrayList list = ((LiveArrayList) super.clone());
        list.times = times;
        return list;
    }

    @Override
    public V get(int index) {
        return super.get(index);
    }

    public Long getTime(int index) {
        return getTime0(index);
    }

    private Long getTime0(int index) {
        return times.get(index);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof LiveArrayList)) return false;
        if(!super.equals(o)) return false;

        LiveArrayList<?> list = (LiveArrayList<?>) o;

        return times != null ? times.equals(list.times) : list.times == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (times != null ? times.hashCode() : 0);
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
        if(!isStarted) {
            TickingPool.addArrayList(this, delay);
            isStarted = true;
        }
    }

    public void stop() {
        if(isStarted) {
            TickingPool.removeArrayList(this, delay);
            isStarted = false;
        }
    }
}
