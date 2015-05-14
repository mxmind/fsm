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

    private static final String GRAVATAR_URL_200 = "http://gravatar.com/avatar/%s?s=200";

    private static final String GRAVATAR_URL_404 = "http://gravatar.com/avatar/%s?d=404";

    private final String email;

    public GravatarPicture(String email) {
        this.email = email;
        setSource("gravatar");
    }

    public String getUrl() {
        String emailHash = EmailEncoder.encode(MessageDigestAlgorithms.MD5, email);
        return String.format(GRAVATAR_URL_200, emailHash);
    }

    public String getCheckUrl(){
        String emailHash = EmailEncoder.encode(MessageDigestAlgorithms.MD5, email);
        return String.format(GRAVATAR_URL_404, emailHash);
    }
}
