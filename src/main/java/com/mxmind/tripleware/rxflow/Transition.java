package com.mxmind.tripleware.rxflow;

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

    public Transition handle(Consumer<State> handler) {
        handler.accept(state);
        return this;
    }
}
