package com.mxmind.tripleware.publicprofile.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class EmailEncoder {

    private EmailEncoder () {

    }

    public static String encode(String hashAlgorithm, String value) {

        try {
            if(StringUtils.isNotBlank(value)) {
                MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
                DigestUtils.updateDigest(messageDigest, value);

                return Hex.encodeHexString(messageDigest.digest());
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

}
