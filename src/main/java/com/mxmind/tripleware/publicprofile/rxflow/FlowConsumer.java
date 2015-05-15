package com.mxmind.tripleware.publicprofile.rxflow;

import java.util.Objects;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface FlowConsumer<T> {

    void accept(T t) throws Exception;

    default FlowConsumer<T> then(FlowConsumer<? super T> after) throws Exception {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    default FlowConsumer<T> before(FlowConsumer<? super T> before) throws Exception {
        Objects.requireNonNull(before);
        return (T t) -> {
            before.accept(t);
            accept(t);
        };
    }
}
