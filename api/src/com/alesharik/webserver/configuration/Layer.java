package com.alesharik.webserver.configuration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Layer hold submodules and another layers
 *
 * @implNote Class must be thread-safe!
 */
@ThreadSafe
public interface Layer {
    /**
     * Return all Layer submodules
     */
    @Nonnull
    List<SubModule> getSubModules();

    /**
     * Return all sub layers
     */
    @Nonnull
    List<Layer> getSubLayers();

    /**
     * Return layer name
     */
    @Nonnull
    String getName();

    default void start() {
        getSubModules().forEach(SubModule::start);
        getSubLayers().forEach(Layer::start);
    }

    default void shutdown() {
        getSubModules().forEach(SubModule::shutdown);
        getSubLayers().forEach(Layer::shutdown);
    }

    default void shutdownNow() {
        getSubModules().forEach(SubModule::shutdownNow);
        getSubLayers().forEach(Layer::shutdownNow);
    }

    default void reload() {
        getSubModules().forEach(SubModule::reload);
        getSubLayers().forEach(Layer::reload);
    }

    /**
     * Search for submodule only in this layer
     *
     * @param name submodule name
     * @return Optional with submodule or empty
     */
    @Nonnull
    default Optional<SubModule> getSubModuleByName(@Nonnull String name) {
        for(SubModule subModule : getSubModules()) {
            if(name.equals(subModule.getName())) {
                return Optional.of(subModule);
            }
        }
        return Optional.empty();
    }

    /**
     * Search for submodule in this layer and sub-layers
     *
     * @param name submodule name
     * @return Optional with submodule or empty
     */
    @Nonnull
    default Optional<SubModule> findSubModuleByName(@Nonnull String name) {
        return Stream.concat(getSubModules().stream(), getSubLayers().stream()
                .flatMap(new Function<Layer, Stream<? extends SubModule>>() {
                    @Override
                    public Stream<? extends SubModule> apply(Layer layer) {
                        return Stream.concat(layer.getSubModules().stream(), layer.getSubLayers().stream().flatMap(this));
                    }
                }))
                .filter(subModule -> name.equals(subModule.getName()))
                .findAny();
    }

    /**
     * Search for sub-layer in this layer
     *
     * @param name layer name
     * @return Optional with layer or empty
     */
    @Nonnull
    default Optional<Layer> getLayerByName(@Nonnull String name) {
        for(Layer subLayer : getSubLayers()) {
            if(name.equals(subLayer.getName())) {
                return Optional.of(subLayer);
            }
        }
        return Optional.empty();
    }

    /**
     * Search for sub-layer in this layer and sub-layers
     *
     * @param name layer name
     * @return Optional with layer or empty
     */
    @Nonnull
    default Optional<Layer> findLayerByName(@Nonnull String name) {
        return getSubLayers().stream()
                .flatMap(new Function<Layer, Stream<? extends Layer>>() {
                    @Override
                    public Stream<? extends Layer> apply(Layer layer) {
                        return Stream.concat(Stream.of(layer), layer.getSubLayers().stream().flatMap(this));
                    }
                })
                .filter(r -> name.equals(r.getName()))
                .findAny();
    }
}
