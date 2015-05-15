package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.GravatarPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class CheckForPictureTestCase extends BasePictureTestCase {

    @Test
    public void testCheckForFacebookPictureIfAvailable() throws IOException {
        Picture picture = new FacebookPicture("100003234733056");
        service.checkForPicture(picture);

        assertTrue(picture.isAvailable());
    }

    @Test
    public void testCheckForGravatarPictureIfAvailable() throws IOException {
        Picture picture = new GravatarPicture("mxmind@gmail.com");
        service.checkForPicture(picture);

        assertTrue(picture.isAvailable());
    }

    @Test
    public void testCheckForGravatarPictureIfNotAvailable() throws IOException {
        Picture picture = new GravatarPicture("test@completelybogus.wvrgroup.internal");
        service.checkForPicture(picture);

        assertFalse(picture.isAvailable());
    }
}
