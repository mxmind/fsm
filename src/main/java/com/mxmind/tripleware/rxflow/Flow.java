package com.mxmind.tripleware.rxflow;

import rx.Observable;
import rx.Observer;
import rx.subscriptions.BooleanSubscription;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class Flow<D> {

    private final Observable<State> fsm;

    private State initState;

    private State errorState;

    private D data;

    {
        fsm = Observable.create((final Observer<State> observer) -> {
            BooleanSubscription sub = new BooleanSubscription();
            try {
                Executors.callable(() -> {
                    if (!sub.isUnsubscribed()) observer.onNext(initState);
                }).call();
            } catch (Exception ex) {
                observer.onError(ex);
            }
            return sub;
        });
    }

    public Flow(final State initState, final State errorState) {
        this.initState = initState;
        this.errorState = errorState;
    }

    public void init(Consumer<FlowObserver> callback) {
        fsm.subscribe(new FlowObserver<>(this, callback));
    }

    public State getErrorState() {
        return errorState;
    }

    public void setErrorState(State errorState) {
        this.errorState = errorState;
    }

    public static class FlowObserver<D> implements Observer<State> {

        private final AtomicReference<State> toState = new AtomicReference<>();

        private final AtomicReference<State> fromState = new AtomicReference<>();

        private D data;

        private Flow<D> flow;

        private Consumer<FlowObserver> callback;

        public FlowObserver(Flow<D> flow, Consumer<FlowObserver> callback) {
            this.flow = flow;
            this.callback = callback;
        }

        @Override
        public void onCompleted() {
            callback.accept(this);
        }

        @Override
        public void onError(Exception ex) {
            this.onNext(flow.getErrorState());
        }

        @Override
        public void onNext(State state) {
            if (fromState.get() == null) {
                fromState.set(state);
                toState.set(state);
            } else {
                fromState.set(toState.getAndSet(state));
            }
            final Transition<D> transition = new Transition<>(this, fromState.get());
            toState.get().onTransition(transition);
        }

        public void setData(D value) {
            this.data = value;
        }

        public D getData() {
            return data;
        }
    }
}
