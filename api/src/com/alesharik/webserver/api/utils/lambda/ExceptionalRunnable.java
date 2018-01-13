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
import java.util.function.Consumer;

import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.*;

public final class ExceptionalRunnable implements ExceptionRunnable {
    private final Action<Exception, Void> exceptionAction;
    private final ExceptionRunnable runnable;

    ExceptionalRunnable(ExceptionRunnable runnable) {
        this.runnable = runnable;
        exceptionAction = action(Exception.class);
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Exception e) {
            fire(exceptionAction, e);
        }
    }

    @Nonnull
    public ExceptionalRunnable onError(Consumer<Exception> exceptionConsumer) {
        when(exceptionAction).then(exceptionConsumer);
        return this;
    }
}
