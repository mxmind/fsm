package com.mxmind.tripleware.rxflow;

import rx.Observable;
import rx.Observer;
import rx.subscriptions.BooleanSubscription;

import java.util.concurrent.Executors;
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
public class Flow<D> {

    private FlowStates<D> initState;

    private final Observable<FlowStates<D>> observable;

    {
        observable = Observable.create((final Observer<FlowStates<D>> observer) -> {
            BooleanSubscription sub = new BooleanSubscription();
            try {
                Executors.callable(() -> {
                    if (!sub.isUnsubscribed()) {
                        observer.onNext(initState);
                    }
                }).call();
            } catch (Exception ex) {
                observer.onError(ex);
            }
            return sub;
        });
    }

    private Flow(final FlowStates<D> initState) {
        this.initState = initState;
    }

    public static <D> void initialize(FlowStates<D> initState,
                                   Consumer<FlowObserver> completeHandler,
                                   BiConsumer<FlowObserver, Exception> errorHandler) {
        Flow<D> flow = new Flow<>(initState);
        flow.observable.subscribe(new FlowObserver<>(completeHandler, errorHandler));
    }

    public static class FlowObserver<D> implements Observer<FlowStates<D>> {

        private final AtomicReference<FlowStates<D>> toState = new AtomicReference<>();

        private final AtomicReference<FlowStates<D>> fromState = new AtomicReference<>();

        private D data;

        private Consumer<FlowObserver> completeHandler;

        private BiConsumer<FlowObserver, Exception> errorHandler;

        public FlowObserver(Consumer<FlowObserver> completeHandler, BiConsumer<FlowObserver, Exception> errorHandler) {
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
            final Transition<D> transition = new Transition<>(this, fromState.get());
            toState.get().onTransition(transition);
        }

        public FlowStates<D> fromState() {
            return fromState.get();
        }

        public void setData(D value) {
            this.data = value;
        }

        public D getData() {
            return data;
        }
    }
}
