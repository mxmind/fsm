package com.mxmind.tripleware.rxflow;

import org.junit.Test;

import java.util.StringJoiner;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class FlowTestCase {

    private enum States implements State<TestData> {

        init {
            @Override
            public void onTransition(Transition<TestData> transition) {

                transition.handle((state) -> {
                    final TestData data = new TestData();
                    data.addState(this.name());

                    transition.setData(data);
                    transition.fsm().onNext(state1);
                });
            }
        },

        state1 {
            @Override
            public void onTransition(Transition<TestData> transition) {

                transition.handle((state) -> {
                    transition.getData().addState(this.name());
                    transition.fsm().onNext(state2);
                });
            }
        },

        state2 {
            @Override
            public void onTransition(Transition<TestData> transition) {
                switch ((States) transition.fromState()) {
                    case state1:
                        transition.handle((state) -> {
                            transition.getData().addState(this.name());
                            throw new RuntimeException("bang-bang!!!");
                        });
                        break;

                    case state3:
                        transition.handle((state) -> {
                            transition.getData().addState(this.name());
                            transition.fsm().onNext(state4);
                        });
                        break;
                }
            }
        },

        state3 {
            @Override
            public void onTransition(Transition<TestData> transition) {
                transition.handle((state) -> {
                    transition.getData().addState(this.name());
                    transition.fsm().onNext(state2);
                });
            }
        },

        state4 {
            @Override
            public void onTransition(Transition<TestData> transition) {
                transition.handle((state) -> {
                    transition.getData().addState(this.name());
                    transition.fsm().onNext(complete);
                });
            }
        },

        error {
            @Override
            public void onTransition(Transition<TestData> transition) {
                transition.handle((state) -> {
                    transition.getData().addState(this.name());
                    transition.fsm().onNext(state3);
                });
            }
        },

        complete {
            @Override
            public void onTransition(Transition<TestData> transition) {
                transition.handle((state) -> {
                    transition.getData().addState(this.name());
                    transition.fsm().onCompleted();
                });
            }
        }
    }

    @Test
    public void testFlow() {
        final Flow<TestData> flow = new Flow<>(States.init, States.error);

        flow.init((fsm) -> {
            final TestData data = (TestData) fsm.getData();
            final StringJoiner joiner = new StringJoiner(" > ");

            data.getProceededStates().forEach(joiner::add);
            System.out.println(joiner.toString());
        });
    }
}
