package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.Crop;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
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
public class CropPictureTestCase extends BasePictureTestCase {

    private Picture picture;

    @Before
    public void setup() throws URISyntaxException, IOException {
        picture = new Picture();
        picture.setImage(ImageIO.read(getImageFile("/high_test.jpg")));
    }

    @Test
    public void testSkipCropPicture() throws IOException {
        int oldW = picture.getImage().getWidth();
        int oldH = picture.getImage().getHeight();

        service.cropPicture(picture);

        int w = picture.getImage().getWidth();
        int h = picture.getImage().getHeight();

        assertEquals(w, oldW);
        assertEquals(h, oldH);
    }

    @Test
    public void testCropPicture() throws IOException {
        picture.setCrop(new Crop(50, 100, 450, 250));
        picture.setContentType("image/jpeg");

        service.cropPicture(picture);

        int w = picture.getImage().getWidth();
        int h = picture.getImage().getHeight();

        assertEquals(w, 450);
        assertEquals(h, 250);
    }
}
