package com.mxmind.tripleware.publicprofile.rxflow;

import rx.Observer;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class Transition<D> implements Observer<FlowStates<D>> {

    private final AtomicReference<FlowStates<D>> toState = new AtomicReference<>();

    private final AtomicReference<FlowStates<D>> fromState = new AtomicReference<>();

    private final D data;

    private Consumer<Transition> completeHandler;

    private BiConsumer<Transition, Exception> errorHandler;

    public Transition(D data, Consumer<Transition> completeHandler, BiConsumer<Transition, Exception> errorHandler) {
        this.data = data;
        this.completeHandler = completeHandler;
        this.errorHandler = errorHandler;
    }

    @Override
    public void onCompleted() {
        completeHandler.accept(this);
    }

    @Override
    public void onError(Exception ex) {
        errorHandler.accept(this, ex);
    }

    @Override
    public void onNext(FlowStates<D> state) {
        if (fromState.get() == null) {
            fromState.set(state);
            toState.set(state);
        } else {
            fromState.set(toState.getAndSet(state));
        }
        toState.get().onTransition(this);
    }

    public FlowStates<D> fromState() {
        return fromState.get();
    }

    public D getData() {
        return data;
    }
}
