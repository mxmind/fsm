package com.mxmind.tripleware.publicprofile.dtos;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PictureOptions {

    SQUARE(50), LARGE(180), NORMAL(80), SMALL(50);

    PictureOptions(int minHeight) {
        this.minHeight = minHeight;
    }
    private int minHeight;

    public int getMinHeight() {
        return minHeight;
    }
}
