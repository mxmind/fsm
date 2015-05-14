package com.mxmind.tripleware.publicprofile.dtos;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacebookPicture extends Picture {

    private static final String FACEBOOK_URL = "http://graph.facebook.com/%s/picture?type=large";

    private final String fbUID;

    public FacebookPicture(String fbUID) {
        this.fbUID = fbUID;
        setSource("facebook");
    }

    public String getUrl() {
        return String.format(FACEBOOK_URL, fbUID);
    }
}
