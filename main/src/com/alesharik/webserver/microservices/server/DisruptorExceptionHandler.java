package com.alesharik.webserver.microservices.server;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.lmax.disruptor.ExceptionHandler;

@Prefix("[MicroserviceServerDisruptor]")
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
