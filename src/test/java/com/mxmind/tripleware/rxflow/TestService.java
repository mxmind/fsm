package com.mxmind.tripleware.rxflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RxPicture
 *
 * @version 1.0.0
 * @autho mxmind
 * @since 1.0.0
 */
@Service("testService")
public class TestService {

    private static final Logger LOG = LoggerFactory.getLogger(TestService.class);

    public Boolean processGravatar() {

        final AtomicBoolean result = new AtomicBoolean();

        new Flow<>(States.init, States.error).init((fsm) -> {
            final TestData data = (TestData) fsm.getData();
            result.set(data.hasGravatar());
        });

        return result.get();
    }

    /*
     * section: fsm handlers
     */

    private void receiveGravatarPicture(Transition<TestData> transition) {
        final TestData data = transition.getData();

        try {
            Thread.sleep(2000);
            LOG.debug("Receive the gravatar picture");
            transition.fsm().onNext(States.gravatar);

        } catch (InterruptedException ex) {
            transition.fsm().onError(ex);
        }
    }

    /*
     * end section
     */

    private enum States implements State<TestData> {

        init {
            @Override
            public void onTransition(Transition<TestData> transition) {

                transition.handle((state) -> {
                    final TestData data = new TestData();
                    data.addState(this.name());

                    transition.setData(data);
                    transition.fsm().onNext(gravatar);
                });
            }
        },

        gravatar {
            @Override
            public void onTransition(Transition<TestData> transition) {
                final Flow.FlowObserver<TestData> fsm = transition.fsm();
                final TestData data = transition.getData();

                transition.handle((state) -> {
                    if (!data.hasGravatar() && data.getLatch().getCount() > 0) {
                        data.getLatch().countDown();
                        service.receiveGravatarPicture(transition);

                    } else if (data.hasGravatar()){
                        fsm.onNext(complete);
                    } else {
                        fsm.onNext(error);
                    }
                });
            }
        },

        state2 {
            @Override
            public void onTransition(Transition<TestData> transition) {
                switch ((States) transition.fromState()) {
                    case gravatar:
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
        };

        @Component
        public static class Injector {

            @Inject
            private TestService service;

            @PostConstruct
            public void postConstruct() {
                EnumSet.allOf(States.class).forEach(state -> state.setService(service));
            }
        }

        protected TestService service;

        public void setService(TestService service) {
            this.service = service;
        }
    }
}
