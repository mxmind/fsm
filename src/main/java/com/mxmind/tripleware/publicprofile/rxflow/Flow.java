package com.mxmind.tripleware.publicprofile.rxflow;

import rx.Observable;
import rx.Observer;
import rx.subscriptions.BooleanSubscription;

import java.util.concurrent.Executors;
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
                                      D data,
                                      Consumer<Transition> completeHandler,
                                      BiConsumer<Transition, Exception> errorHandler) {
        Flow<D> flow = new Flow<>(initState);
        flow.observable.subscribe(new Transition<>(data, completeHandler, errorHandler));
    }
}
