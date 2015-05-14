package com.mxmind.tripleware.publicprofile.dtos;

import com.mxmind.tripleware.publicprofile.utils.EmailEncoder;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class GravatarPicture extends Picture {

    private static final String GRAVATAR_URL = "http://gravatar.com/avatar/%s?s=200";

    private final String email;

    public GravatarPicture(String email) {
        this.email = email;
        setSource("gravatar");
    }

    public String getUrl() {
        String emailHash = EmailEncoder.encode(MessageDigestAlgorithms.MD5, email);
        return String.format(GRAVATAR_URL, emailHash);
    }
}
