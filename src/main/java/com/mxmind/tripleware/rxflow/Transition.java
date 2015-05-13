package com.mxmind.tripleware.rxflow;

import com.mxmind.tripleware.rxflow.exceptions.RxTransitionException;

import java.util.function.Consumer;


/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class Transition<D> {

    private Flow.FlowObserver<D> fsm;

    private State state;

    public <F extends Flow.FlowObserver<D>> Transition(F fsm, State state) {
        this.fsm = fsm;
        this.state = state;
    }

    public Flow.FlowObserver<D> fsm(){
        return fsm;
    }

    public void setData(D value) {
        fsm.setData(value);
    }

    public D getData(){
        return fsm.getData();
    }

    public Transition handle(Consumer<State> handler, Consumer<State> before, Consumer<State> after) {
        return doHandle(state, handler, before, after);
    }

    public Transition handle(Consumer<State> func) {
        return handle(func, (state) -> {}, (state) -> {});
    }

    private Transition doHandle(State state, Consumer<State> handler, Consumer<State> before, Consumer<State> after) {
        try {
            before.accept(state);
        } catch (Exception ex) {
            fsm().onError(new RxTransitionException(ex, RxTransitionException.Cause.BEFORE));
        } finally {
            handler.accept(state);
            try {
                after.accept(state);
            } catch (Exception ex) {
                fsm().onError(new RxTransitionException(ex, RxTransitionException.Cause.AFTER));
            }
        }
        return this;
    }
}
