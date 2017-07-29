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

package com.alesharik.webserver.api.server.wrapper.server;

import lombok.AllArgsConstructor;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

public abstract class BatchingForkJoinTask<K, V> extends ForkJoinTask<V> implements BatchingTask<K> {
    private static final long serialVersionUID = 6887156607316124540L;

    public static <K> BatchingForkJoinTask<K, Void> wrap(BatchingRunnableTask<K> task) {
        return new BatchingRunnableWrapper<>(task);
    }

    public static <K, V> BatchingForkJoinTask<K, V> wrap(BatchingCallableTask<K, V> task) {
        return new BatchingCallableWrapper<>(task);
    }

    @AllArgsConstructor
    private static final class BatchingRunnableWrapper<K> extends BatchingForkJoinTask<K, Void> {
        private static final long serialVersionUID = 8658281454126148502L;
        private final BatchingRunnableTask<K> task;

        @Override
        public K getKey() {
            return task.getKey();
        }

        @Override
        public Void getRawResult() {
            return null;
        }

        @Override
        protected void setRawResult(Void value) {

        }

        @Override
        protected boolean exec() {
            task.run();
            return true;
        }
    }

    private static final class BatchingCallableWrapper<K, V> extends BatchingForkJoinTask<K, V> implements RunnableFuture<V> {
        private final BatchingCallableTask<K, V> task;
        V value;

        public BatchingCallableWrapper(BatchingCallableTask<K, V> task) {
            this.task = task;
        }

        @Override
        public K getKey() {
            return task.getKey();
        }

        @Override
        public V getRawResult() {
            return value;
        }

        @Override
        protected void setRawResult(V value) {
            this.value = value;
        }

        @Override
        protected boolean exec() {
            try {
                value = task.call();
            } catch (Error | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        @Override
        public void run() {
            invoke();
        }
    }
}
