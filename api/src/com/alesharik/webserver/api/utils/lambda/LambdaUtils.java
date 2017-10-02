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

package com.alesharik.webserver.api.utils.lambda;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.jcip.annotations.NotThreadSafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class provides some utilities like streams and completable futures
 */
@SuppressWarnings("unused")
@UtilityClass
@NotThreadSafe
public class LambdaUtils {
    /**
     * Create new {@link Action}
     *
     * @param ret   value, which will be returned in the end of {@link When}
     * @param clazz set action argument type
     * @param <C>   action argument type
     * @param <R>   return object type
     * @return new action
     */
    @Nonnull
    public static <C, R> Action<C, R> action(@Nullable Class<C> clazz, @Nonnull R ret) {
        return new ActionImpl<>(ret);
    }

    /**
     * Create new {@link Action} without return type(it is <code>null</code>)
     *
     * @param clazz set action argument type
     * @param <C>   action argument type
     * @return new action
     */
    @Nonnull
    public static <C> Action<C, Void> action(@Nullable Class<C> clazz) {
        return new ActionImpl<>(null);
    }

    /**
     * Create when instruction. It will be called in action fire method
     *
     * @param action the action
     * @param <C>    action argument type
     * @param <R>    return object type
     * @return when instruction
     * @throws IllegalArgumentException because actions must be created by {@link #action(Class)} or {@link #action(Class, Object)} methods
     */
    @Nonnull
    public static <C, R> When<C, R> when(@Nonnull Action<C, R> action) {
        if(!(action instanceof ActionImpl))
            throw new IllegalArgumentException("Support only actions created by #action methods");
        return ((ActionImpl<C, R>) action).newWhen();
    }

    /**
     * Fire action
     *
     * @param action the action
     * @param arg    action argument
     * @param <C>    action argument type
     * @param <R>    return object type
     */
    public static <C, R> void fire(@Nonnull Action<C, R> action, @Nullable C arg) {
        action.call(arg);
    }

    /**
     * Create Lazy Singleton Factory. This factory contains only one instance of <code>O</code> type and creates it when it needed
     *
     * @param factory the factory
     * @param <O>     factory return type
     * @return Lazy Singleton Factory
     */
    @Nonnull
    public static <O> Supplier<O> lazySingleton(@Nonnull Supplier<O> factory) {
        return new LazySupplier<>(factory);
    }

    private static final class ActionImpl<C, R> implements Action<C, R> {
        private final List<When<C, R>> whens;
        private final R ret;

        public ActionImpl(R ret) {
            this.ret = ret;
            this.whens = new ArrayList<>();
        }

        When<C, R> newWhen() {
            WhenImpl<C, R> w = new WhenImpl<>(ret);
            whens.add(w);
            return w;
        }

        @Override
        public void call(C arg) {
            for(When<C, R> when : whens) {
                if(((WhenImpl<C, R>) when).c != null)
                    for(Consumer<C> cConsumer : ((WhenImpl<C, R>) when).c)
                        cConsumer.accept(arg);
            }
        }
    }

    private static final class WhenImpl<C, R> implements When<C, R> {
        private volatile R ret;
        private volatile List<Consumer<C>> c;

        public WhenImpl(R ret) {
            this.ret = ret;
        }

        void clearRet() {
            ret = null;
        }

        @Override
        public R then(Consumer<C> c) {
            if(this.c != null)
                throw new IllegalStateException("When already filled!");

            this.c = Collections.singletonList(c);
            R ret = this.ret;
            clearRet();
            return ret;
        }

        @Override
        public R batch(Consumer<Batch<C>> batchConsumer) {
            if(this.c != null)
                throw new IllegalStateException("When already filled!");

            BatchImpl<C> chain = new BatchImpl<>();
            batchConsumer.accept(chain);
            this.c = chain.getRet();
            R ret = this.ret;
            clearRet();
            return ret;
        }

        private static final class BatchImpl<C> implements Batch<C> {
            @Getter
            private final List<Consumer<C>> ret;

            public BatchImpl() {
                this.ret = new ArrayList<>();
            }

            @Override
            public Batch<C> then(Consumer<C> c) {
                ret.add(c);
                return this;
            }
        }
    }

    private static final class LazySupplier<T> implements Supplier<T> {
        private final Supplier<T> supplier;
        private volatile T val;

        LazySupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            T v = val;
            if(v == null) {
                synchronized (this) {
                    if((v = val) == null) {
                        v = val = supplier.get();
                    }
                }
            }
            return v;
        }
    }
}
