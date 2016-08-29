package com.alesharik.webserver.microservices.server;

import com.alesharik.webserver.api.server.Server;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.microservices.api.Microservice;
import com.alesharik.webserver.microservices.client.MicroserviceClient;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManagerBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Prefix("[MicroserviceServer]")
public class MicroserviceServer extends Server implements Runnable {
    private MicroserviceClient client;
    private ConcurrentHashMap<String, Microservice> microservices = new ConcurrentHashMap<>();
    private Disruptor<Event> disruptor;
    private volatile RingBuffer<Event> ringBuffer;
    private MicroserviceServerRequestProcessor processor;

    public MicroserviceServer(String host, int port, WorkingMode mode) {
        //TODO use CPU L3 cache size without 1024
        disruptor = new Disruptor<>(new EventFactoryImpl(), 1024, new DisruptorThreadFactory());
        disruptor.setDefaultExceptionHandler(new DisruptorExceptionHandler<>());
        disruptor.handleEventsWith(new DisruptorEventHandler(microservices));

        processor = new MicroserviceServerRequestProcessor(null, host, port, mode);

        if(mode == WorkingMode.ADVANCED) {
            client = new MicroserviceClient(MicroserviceClient.WorkingMode.SIMPLE);
        }
        Logger.log("Microservice server successfully initialized!");
    }

    public void registerMicroservice(String name, Microservice microservice) {
        microservices.put(name, microservice);
        microservice.setup();
    }

    public void setupMicorserviceAccessManagerBuilder(MicroserviceAccessManagerBuilder builder) {
        builder.setClient(client);
        builder.setServer(this);
    }

    @Override
    public void start() throws IOException {
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
        processor.setBuffer(ringBuffer);
        new Thread(this).start();
    }

    @Override
    public void shutdown() {
        disruptor.shutdown();
        ringBuffer = null;
        processor.shutdown();
        processor.setBuffer(null);
    }

    @Override
    public void run() {
        processor.start();
    }

    public ArrayList<String> getMicroserviceNames() {
        ArrayList<String> arrayList = new ArrayList<>();
        microservices.keySet().forEach(arrayList::add);
        return arrayList;
    }

    public enum WorkingMode {
        SIMPLE,
        ADVANCED
    }
}
