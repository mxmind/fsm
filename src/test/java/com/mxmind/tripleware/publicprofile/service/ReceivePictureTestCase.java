package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(URIUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class ReceivePictureTestCase {

    @InjectMocks
    private PictureService service = new PictureService();

    private final static Logger LOG = LoggerFactory.getLogger(ReceivePictureTestCase.class);

    private final static ContentType CONTENT_TYPE = ContentType.create("image/jpeg");

    private PictureService spyService;

    private TestServer testServer;

    private Picture picture;

    private File pictureFile;

    private HttpHost httpHost;

    @Before
    public void setupTestMethod() throws Exception {

        spyService = PowerMockito.spy(service);
        testServer = new TestServer();
        testServer.start();

        httpHost = new HttpHost(TestServer.TEST_SERVER_ADDR.getHostName(), testServer.getServicePort(), "http");

        picture = new FacebookPicture("100003234733056");
        pictureFile = getImageFile("/facebook_test.jpg");
    }

    @After
    public void tearDown() throws Exception {
        testServer.stop();
    }

    @Test
    public void testFlow() {
        service.processFacebookPicture();
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
        HttpGet get = new HttpGet(picture.getUrl());

        new TestServerMockRegistrar(get).register((request, response, context) -> {
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            String uri = request.getRequestLine().getUri();

            if (uri.contains(get.getURI().getPath())) {
                InputStream stream = new FileInputStream(pictureFile);
                InputStreamEntity entity = new InputStreamEntity(stream, pictureFile.length(), CONTENT_TYPE);

                response.setEntity(entity);
                response.setStatusLine(ver, HttpStatus.SC_INTERNAL_SERVER_ERROR);
                response.setReasonPhrase("Internal Server Error");
            }
        });

        service.receivePicture(picture);

        assertNull(picture.getImage());
        assertFalse(picture.isDownloaded());
    }

    @Test
    public void testCircularRedirect() throws Exception {
        HttpGet get = new HttpGet("/circular-0/");
        CountDownLatch latch = new CountDownLatch(10);

        new TestServerMockRegistrar(get).register("*", (request, response, context) -> {
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            String uri = request.getRequestLine().getUri();

            if (uri.startsWith("/circular-1")) {
                prepareResponse(ver, response, 2);
                latch.countDown();
            } else if (uri.startsWith("/circular-2")) {
                prepareResponse(ver, response, 1);
                latch.countDown();
            } else {
                prepareResponse(ver, response, 1);
            }
        });

        try {
            service.getDefaultHttpClient().execute(serverHttp(), get);
        } catch (ClientProtocolException ex) {
            assertTrue(RedirectException.class.isInstance(ex.getCause()));
            assertEquals(latch.getCount(), 0);
        }
    }

    private HttpHost serverHttp() {
        return httpHost;
    }

    private void prepareResponse(ProtocolVersion ver, HttpResponse response, int locIncrement){
        response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
        response.addHeader(new BasicHeader("Location", String.format("/circular-location-%d", locIncrement)));
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

    protected File getImageFile(String pathToImage) throws URISyntaxException {
        return Paths.get(this.getClass().getResource(pathToImage).toURI()).toFile();
    }

    private class TestServerMockRegistrar {

        private HttpRequestBase request;

        private String methodName = "extractHost";

        private String pattern = "*";

        public TestServerMockRegistrar(HttpRequestBase request) {
            this.request = request;
        }

        public void register(HttpRequestHandler handler) throws Exception {
            URI uri = request.getURI();
            PowerMockito.mockStatic(URIUtils.class, withSettings()
                .name(methodName)
                .defaultAnswer(invocation -> invocation.getMethod().getName().equals(methodName) ? serverHttp() : uri)
            );
            PowerMockito.when(URIUtils.class, methodName, uri).thenReturn(serverHttp());

            testServer.register(pattern, handler);
        }

        public void register(String pattern, HttpRequestHandler handler) throws Exception {
            testServer.register(pattern, handler);
        }
    }
}
