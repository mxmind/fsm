package com.mxmind.tripleware.publicprofile.rxflow;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class Transition<D> {

    private Flow.FlowObserver<D> fsm;

    private FlowStates state;

    public Transition(Flow.FlowObserver<D> fsm, FlowStates state) {
        this.fsm = fsm;
        this.state = state;
    }

    public Flow.FlowObserver<D> fsm(){
        return fsm;
    }

    public D getData(){
        return fsm.getData();
    }

    public Transition handle(FlowConsumer<FlowStates> handler) {
        try {
            handler.accept(state);
        } catch (Exception ex) {
            fsm.onError(ex);
        }
        return this;
    }
}
