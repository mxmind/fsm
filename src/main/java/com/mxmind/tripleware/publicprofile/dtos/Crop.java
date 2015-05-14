package com.mxmind.tripleware.publicprofile.dtos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class Crop {

    public static final Crop EMPTY = new Crop(0, 0, 0, 0);

    private int y;

    private int x;

    private int width;

    private int height;

    public Crop(int y, int x, int width, int height) {
        this.y = y;
        this.x = x;
        this.width = width;
        this.height = height;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof Crop)) return false;

        Crop crop = (Crop) obj;

        return new EqualsBuilder()
                .append(y, crop.y)
                .append(x, crop.x)
                .append(width, crop.width)
                .append(height, crop.height)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(y)
                .append(x)
                .append(width)
                .append(height)
                .toHashCode();
    }
}
