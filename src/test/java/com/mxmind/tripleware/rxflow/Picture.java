package com.mxmind.tripleware.rxflow;

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

    private String url;

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
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}