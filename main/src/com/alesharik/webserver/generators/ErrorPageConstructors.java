package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ThreadSafe
class ErrorPageConstructors {
    private final CopyOnWriteArrayList<ErrorPageConstructor> constructors;
    private final ConcurrentHashMap<Integer, ErrorPageConstructor> defaults;

    public ErrorPageConstructors() {
        constructors = new CopyOnWriteArrayList<>();
        defaults = new ConcurrentHashMap<>();
    }

    public void addConstructor(ErrorPageConstructor constructor) {
        constructors.add(constructor);
    }

    public void removeErrorPageConstructor(ErrorPageConstructor constructor) {
        constructors.remove(constructor);
    }

    public boolean containsConstructor(ErrorPageConstructor constructor) {
        return constructors.contains(constructor);
    }

    public void setDefault(ErrorPageConstructor errorPageConstructor, int status) {
        if(!errorPageConstructor.support(status)) {
            throw new IllegalArgumentException("Error page constructor doesn't support given status!");
        }

        defaults.put(status, errorPageConstructor);
    }

    public Optional<ErrorPageConstructor> getConstructor(int status) {
        if(defaults.containsKey(status)) {
            ErrorPageConstructor constructor = defaults.get(status);
            if(constructor != null) {
                return Optional.of(constructor);
            }
        }
        return constructors.stream()
                .filter(constructor -> constructor.support(status))
                .findFirst();
    }

    public List<ErrorPageConstructor> constructors(int status) {
        ArrayList<ErrorPageConstructor> ret = new ArrayList<>();
        constructors.stream()
                .filter(constructor -> constructor.support(status))
                .forEach(ret::add);
        return Collections.unmodifiableList(ret);
    }
}
