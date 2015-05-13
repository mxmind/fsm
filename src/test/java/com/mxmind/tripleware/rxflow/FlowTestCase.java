package com.mxmind.tripleware.rxflow;

import com.mxmind.tripleware.rxflow.configs.TestConfig;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.inject.Inject;


/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
})
public class FlowTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(FlowTestCase.class);

    @Inject
    private TestService service;

    @Test
    public void testFlow() {
        Boolean isGravatarReceived = service.processGravatar();
        Assert.assertFalse(isGravatarReceived);
    }
}
