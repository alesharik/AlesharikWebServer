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

package com.alesharik.database.proxy;

import com.alesharik.database.transaction.Transaction;
import com.alesharik.database.transaction.TransactionProvider;
import com.alesharik.database.transaction.reflect.TransactionMethod;
import com.alesharik.database.transaction.reflect.Transactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public final class TransactionProxyFactory {
    private TransactionProxyFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends T> T newTransactionProxy(Class<T> iface, R realisation, TransactionProvider provider) {
        if(!iface.isInterface())
            throw new IllegalArgumentException("Interface class must be interface!");
        if(!iface.isAnnotationPresent(Transactional.class)) {
            System.err.println("Interface " + iface.getCanonicalName() + " MUST be annotated with @Transactional annotation for transaction working!");
            return realisation;
        }
        return (T) Proxy.newProxyInstance(realisation.getClass().getClassLoader(), new Class[]{iface}, new TransactionInvocationHandler(provider, realisation));
    }

    private static final class TransactionInvocationHandler implements InvocationHandler {
        private final TransactionProvider transactionProvider;
        private final Object realisation;
        private final AtomicReference<TransactionProvider> currentProvider;

        public TransactionInvocationHandler(TransactionProvider transactionProvider, Object realisation) {
            this.transactionProvider = transactionProvider;
            this.realisation = realisation;
            this.currentProvider = new AtomicReference<>(transactionProvider);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            method.setAccessible(true);
            TransactionMethod annotation = method.getAnnotation(TransactionMethod.class);
            if(annotation == null || annotation.ignored()) {
                return method.invoke(realisation, args);
            } else {
                Object ret = null;
                if(annotation.requirePrivateSavepoint() || currentProvider.get() == transactionProvider) {
                    TransactionProvider parent = currentProvider.get();
                    Transaction transaction = annotation.savepointName().isEmpty() ? currentProvider.get().begin() : currentProvider.get().begin(annotation.savepointName());
                    currentProvider.set(transaction);
                    try {
                        ret = method.invoke(realisation, args);
                        if(currentProvider.get() == transactionProvider)
                            transaction.commit();
                    } catch (Throwable throwable) {
                        transaction.rollback();
                    } finally {
                        currentProvider.set(parent);
                    }
                } else {
                    return method.invoke(realisation, args);
                }
                return ret;
            }
        }
    }
}
