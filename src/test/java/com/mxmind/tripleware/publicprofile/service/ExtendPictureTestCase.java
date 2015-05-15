package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import com.mxmind.tripleware.publicprofile.dtos.PictureOptions;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExtendPictureTestCase extends BasePictureTestCase {

    private File fbImage;

    private File fbProcessedImage;

    private File highImage;

    private Picture picture;

    @Before
    public void setup() throws URISyntaxException {
        picture = new FacebookPicture("100003234733056");
        picture.setOptions(PictureOptions.LARGE);

        fbImage = getImageFile("/facebook_test.jpg");
        fbProcessedImage = getImageFile("/facebook.jpg");

        highImage = getImageFile("/high_test.jpg");
    }

    @Test
    public void testSkipExtendHighPicture() throws IOException {
        picture.setImage(ImageIO.read(highImage));
        int w = picture.getImage().getWidth();
        int h = picture.getImage().getHeight();

        service.extendPicture(picture);

        assertEquals(w, picture.getImage().getWidth());
        assertEquals(h, picture.getImage().getHeight());
    }

    @Test
    public void testExtendPicture() throws IOException {
        picture.setImage(ImageIO.read(fbImage));
        int oldW = picture.getImage().getWidth();
        int oldH = picture.getImage().getHeight();

        BufferedImage standardImage = ImageIO.read(fbProcessedImage);
        int w = standardImage.getWidth();
        int h = standardImage.getHeight();

        service.extendPicture(picture);

        assertEquals(w, picture.getImage().getWidth());
        assertEquals(oldW, picture.getImage().getWidth());

        assertTrue(oldH < picture.getImage().getHeight());
        assertEquals(h, picture.getImage().getHeight());
    }
}
