package com.mxmind.tripleware.publicprofile.rxflow;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FlowStates<D> {

    default void onTransition(Transition<D> transition){
        transition.fsm().onCompleted();
    }
}
