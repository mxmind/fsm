package com.mxmind.tripleware.publicprofile.dtos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.awt.image.RenderedImage;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class Picture {

    private RenderedImage image;

    private String contentType;

    private String source;

    private String uuid;

    private boolean available;

    private boolean downloaded;

    private Crop crop;

    private PictureOptions options;

    /**
     * Gets image.
     *
     * @return the image
     */
    public RenderedImage getImage() {
        return image;
    }

    /**
     * Sets image.
     *
     * @param value the image
     */
    public void setImage(RenderedImage value) {
        this.image = value;
    }

    /**
     * Gets source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets source.
     *
     * @param value the value
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets content type.
     *
     * @param value the content type
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets url.
     *
     * @apiNote Should be overrided into sub-classes
     * @return the url
     */
    public String getUrl() {
        return null;
    }

    /**
     * Is available.
     *
     * @return the boolean
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Sets available.
     *
     * @param value the available
     */
    public void setAvailable(boolean value) {
        this.available = value;
    }

    /**
     * Is downloaded.
     *
     * @return the boolean
     */
    public boolean isDownloaded() {
        return downloaded;
    }

    /**
     * Sets downloaded.
     *
     * @param value the downloaded
     */
    public void setDownloaded(boolean value) {
        this.downloaded = value;
    }

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets uuid.
     *
     * @param value the uuid
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets crop.
     *
     * @return the crop
     */
    public Crop getCrop() {
        if (crop == null) {
            return Crop.EMPTY;
        }
        return crop;
    }

    /**
     * Sets crop.
     *
     * @param value the crop
     */
    public void setCrop(Crop value) {
        this.crop = value;
    }

    /**
     * Gets options.
     *
     * @return the options
     */
    public PictureOptions getOptions() {
        return options;
    }

    /**
     * Sets options.
     *
     * @param value the options
     */
    public void setOptions(PictureOptions value) {
        this.options = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof Picture)) return false;

        Picture that = (Picture) obj;

        return new EqualsBuilder()
                .append(available, that.available)
                .append(downloaded, that.downloaded)
                .append(image, that.image)
                .append(contentType, that.contentType)
                .append(source, that.source)
                .append(uuid, that.uuid)
                .append(crop, that.crop)
                .append(options, that.options)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(image)
                .append(contentType)
                .append(source)
                .append(uuid)
                .append(available)
                .append(downloaded)
                .append(crop)
                .append(options)
                .toHashCode();
    }
}
