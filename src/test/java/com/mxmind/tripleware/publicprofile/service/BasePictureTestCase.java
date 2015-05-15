package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.common.PictureConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.inject.Inject;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PictureConfiguration.class})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
})
public abstract class BasePictureTestCase {

    @Inject
    protected PictureService service;

    protected File getImageFile(String pathToImage) throws URISyntaxException {
        return Paths.get(this.getClass().getResource(pathToImage).toURI()).toFile();
    }
}
