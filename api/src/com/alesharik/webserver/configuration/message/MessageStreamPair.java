package com.alesharik.webserver.configuration.message;

import javax.annotation.Nonnull;

/**
 * {@link MessageStreamPair} create new {@link MessageStream} pair and connect it.
 *
 * @param <S> {@link MessageStream} class
 * @param <M> {@link Message} class
 */
public interface MessageStreamPair<M extends Message, S extends MessageStream> {
    /**
     * Return first stream
     */
    @Nonnull
    S first();

    /**
     * Return second stream
     */
    @Nonnull
    S second();
}
