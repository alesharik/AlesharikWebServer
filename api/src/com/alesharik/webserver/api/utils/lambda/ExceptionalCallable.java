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

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.action;
import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.when;

public final class ExceptionalCallable<V> implements Callable<V> {
    private final Callable<V> callable;
    private final Action<Exception, Void> errorAction;

    ExceptionalCallable(Callable<V> callable) {
        this.callable = callable;
        this.errorAction = action(Exception.class);
    }

    @Override
    public V call() {
        try {
            return callable.call();
        } catch (Exception e) {
            errorAction.call(e);
            return null;
        }
    }

    @Nonnull
    public ExceptionalCallable<V> onError(Consumer<Exception> exceptionConsumer) {
        when(errorAction).then(exceptionConsumer);
        return this;
    }
}
