package com.mxmind.tripleware.publicprofile.dtos;

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

    private boolean downloaded;

    public void setImage(RenderedImage image) {
        this.image = image;
    }

    public RenderedImage getImage() {
        return image;
    }

    public String getSource() {
        return source;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSource(String value) {
        this.source = value;
    }

    public String getUrl() {
        return null;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
