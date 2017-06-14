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

package com.alesharik.webserver.microservices.server;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.lmax.disruptor.ExceptionHandler;

@Prefixes("[MicroserviceServerDisruptor]")
class DisruptorExceptionHandler<T> implements ExceptionHandler<T> {
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        Logger.log("Exception in event handler with sequence " + sequence + " and event class " + event.getClass().getName());
        Logger.log(ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        Logger.log("Can't initialize disruptor!");
        Logger.log(ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        Logger.log("Can't shutdown disruptor!");
        Logger.log(ex);
    }
}
