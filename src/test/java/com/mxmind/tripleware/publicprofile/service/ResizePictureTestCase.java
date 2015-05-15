package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class ResizePictureTestCase extends BasePictureTestCase {

    private File wideImage;

    private File fbImage;

    private File highImage;

    private Picture picture;

    @Before
    public void setup() throws URISyntaxException {
        picture = new FacebookPicture("100003234733056");

        fbImage = getImageFile("/facebook.jpg");
        wideImage = getImageFile("/wide_test.png");
        highImage = getImageFile("/high_test.jpg");
    }

    @Test
    public void testResizeWidePicture() throws IOException {
        picture.setImage(ImageIO.read(wideImage));
        int w = picture.getImage().getWidth();
        int h = picture.getImage().getHeight();

        service.resizePicture(picture);

        assertEquals(910, picture.getImage().getWidth());
        assertEquals(365, picture.getImage().getHeight());
    }

    @Test
    public void testResizeHighPicture() throws IOException {
        picture.setImage(ImageIO.read(highImage));
        int w = picture.getImage().getWidth();
        int h = picture.getImage().getHeight();

        service.resizePicture(picture);

        assertEquals(369, picture.getImage().getWidth());
        assertEquals(600, picture.getImage().getHeight());
    }

    @Test
    public void testSkipResize() throws IOException {
        picture.setImage(ImageIO.read(fbImage));
        int w = picture.getImage().getWidth();
        int h = picture.getImage().getHeight();

        service.resizePicture(picture);

        assertEquals(w, picture.getImage().getWidth());
        assertEquals(h, picture.getImage().getHeight());
    }
}