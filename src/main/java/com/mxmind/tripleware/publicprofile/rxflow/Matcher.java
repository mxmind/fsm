package com.mxmind.tripleware.publicprofile.rxflow;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface Matcher<R, T> {

    static <R, T> Matcher<R, T> when(Predicate<R> predicate, Function<R, T> action) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(action);

        return value -> predicate.test(value) ? Optional.of(action.apply(value)) : Optional.empty();
    }

    default Matcher<R, T> otherwise(Function<R, T> action) {
        Objects.requireNonNull(action);

        return value -> {
            final Optional<T> result = match(value);
            return result.isPresent() ? result : Optional.of(action.apply(value));
        };
    }

    default Matcher<R, T> orWhen(Predicate<R> predicate, Function<R, T> action) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(action);

        return value -> {
            final Optional<T> result = match(value);
            if (result.isPresent()) {
                return result;
            } else {
                return predicate.test(value) ? Optional.of(action.apply(value)) : Optional.empty();
            }
        };
    }

    Optional<T> match(R value);
}
