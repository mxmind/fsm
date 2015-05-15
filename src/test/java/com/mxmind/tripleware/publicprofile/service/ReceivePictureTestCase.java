package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReceivePictureTestCase extends BasePictureTestCase {

    private final static Logger LOG = LoggerFactory.getLogger(ReceivePictureTestCase.class);

    private final static ContentType CONTENT_TYPE = ContentType.create("image/jpeg");

    private PictureService spyService;

    private Picture picture;

    private File pictureFile;

    @Before
    public void setupTestMethod() throws URISyntaxException {
        spyService = PowerMockito.spy(service);

        picture = new FacebookPicture("100003234733056");
        pictureFile = Paths.get(this.getClass().getResource("/facebook_test.jpg").toURI()).toFile();
    }

    @Test
    public void testRecivePictureWithOkStatus() throws Exception {

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.getConnectionManager()).thenReturn(new BasicClientConnectionManager());
        PowerMockito.when(spyService.getDefaultHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(mockResponse(200, pictureFile, ""));

        spyService.receivePicture(picture);

        assertNotNull(picture.getImage());
        assertTrue(picture.isDownloaded());
        assertEquals(200, picture.getImage().getWidth());
        assertEquals(150, picture.getImage().getHeight());

        Mockito.reset(httpClient);
    }

    @Test
    public void testRecivePictureWithNotFoundStatus() throws Exception {

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.getConnectionManager()).thenReturn(new BasicClientConnectionManager());
        PowerMockito.when(spyService.getDefaultHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(mockResponse(404, pictureFile, "Not Found"));

        spyService.receivePicture(picture);

        assertNull(picture.getImage());
        assertFalse(picture.isDownloaded());
    }

    @Test
    public void testRecivePictureWithInternalServerErrorStatus() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.getConnectionManager()).thenReturn(new BasicClientConnectionManager());
        PowerMockito.when(spyService.getDefaultHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(mockResponse(500, pictureFile, "Internal Server Error"));

        spyService.receivePicture(picture);

        assertNull(picture.getImage());
        assertFalse(picture.isDownloaded());
    }

    private BasicHttpResponse prepareResponse(int responseStatus, String reason) {
        return new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, responseStatus, reason));
    }

    private HttpResponse mockResponse(int responseStatus, File file, String reason) {
        HttpResponse response = prepareResponse(responseStatus, reason);
        response.setStatusCode(responseStatus);

        try {
            InputStream stream = new FileInputStream(file);
            InputStreamEntity entity = new InputStreamEntity(stream, file.length(), CONTENT_TYPE);

            response.setEntity(entity);
        } catch (FileNotFoundException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot find test resource", ex);
            }
        }
        return response;
    }

    private HttpResponse mockResponse(int responseStatus, File file, ContentType contentType, String reason) {
        HttpResponse response = prepareResponse(responseStatus, reason);
        response.setStatusCode(responseStatus);
        response.setEntity(new FileEntity(file, contentType));
        return response;
    }

    private HttpResponse mockResponse(int responseStatus, String responseBody, String reason) {
        HttpResponse response = prepareResponse(responseStatus, reason);

        try {
            response.setStatusCode(responseStatus);
            response.setEntity(new StringEntity(responseBody));
        } catch (UnsupportedEncodingException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot create response entity", ex);
            }
        }
        return response;
    }
}
