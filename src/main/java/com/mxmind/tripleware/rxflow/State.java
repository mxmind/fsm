package com.mxmind.tripleware.rxflow;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public interface State<D> {

    default void onTransition(Transition<D> transition){
        transition.fsm().onCompleted();
    }
}
