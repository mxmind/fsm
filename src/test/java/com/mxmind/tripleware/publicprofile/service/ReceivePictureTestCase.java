package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpRequestHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.withSettings;

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

    private TestServer server;

    private Picture picture;

    private File pictureFile;

    private HttpHost httpHost;

    @Before
    public void setupTestMethod() throws Exception {
        server = new TestServer();
        server.start();
        httpHost = new HttpHost(TestServer.SERVER_ADDR.getHostName(), server.getServicePort(), HttpHost.DEFAULT_SCHEME_NAME);

        picture = new FacebookPicture("100003234733056");
        pictureFile = getImageFile("/facebook_test.jpg");
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testFlow() {
        service.processFacebookPicture();
    }

    @Test
    public void testRecivePictureWithOkStatus() throws Exception {
        HttpGet get = new HttpGet(picture.getUrl());

        new TestServerRegistrar(get).mock().register((request, response, context) -> {
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            String uri = request.getRequestLine().getUri();

            if (uri.contains(get.getURI().getPath())) {
                InputStream stream = new FileInputStream(pictureFile);
                InputStreamEntity entity = new InputStreamEntity(stream, pictureFile.length(), CONTENT_TYPE);

                response.setEntity(entity);
                response.setStatusLine(ver, HttpStatus.SC_OK);
            }
        });

        service.receivePicture(picture);

        assertNotNull(picture.getImage());
        assertTrue(picture.isDownloaded());
        assertEquals(200, picture.getImage().getWidth());
        assertEquals(150, picture.getImage().getHeight());
    }

    @Test
    public void testRecivePictureWithNotFoundStatus() throws Exception {
        HttpGet get = new HttpGet(picture.getUrl());

        new TestServerRegistrar(get).mock().register((request, response, context) -> {
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            String uri = request.getRequestLine().getUri();

            if (uri.contains(get.getURI().getPath())) {
                response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
                response.setReasonPhrase("Not Found");
            }
        });

        service.receivePicture(picture);

        assertNull(picture.getImage());
        assertFalse(picture.isDownloaded());
    }

    @Test
    public void testRecivePictureWithInternalServerErrorStatus() throws Exception {
        HttpGet get = new HttpGet(picture.getUrl());

        new TestServerRegistrar(get).mock().register((request, response, context) -> {
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

        new TestServerRegistrar(get).register((request, response, context) -> {
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            String uri = request.getRequestLine().getUri();

            if (uri.startsWith("/circular-0")) {
                moveTemporarily(ver, response, 1);
            } else {
                int incr = uri.startsWith("/circular-1") ? 2 : 1;
                moveTemporarily(ver, response, incr);
                latch.countDown();
            }
        });

        try {
            service.getDefaultHttpClient().execute(httpHost, get);
        } catch (ClientProtocolException ex) {
            assertTrue(RedirectException.class.isInstance(ex.getCause()));
            assertEquals(latch.getCount(), 0);
        }
    }

    private void moveTemporarily(ProtocolVersion ver, HttpResponse response, int index){
        response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
        response.addHeader(new BasicHeader("Location", String.format("/circular-location-%d", index)));
    }

    private File getImageFile(String pathToImage) throws URISyntaxException {
        return Paths.get(this.getClass().getResource(pathToImage).toURI()).toFile();
    }

    private class TestServerRegistrar {

        private HttpRequestBase request;

        private String methodName = "extractHost";

        private String pattern = "*";

        public TestServerRegistrar(HttpRequestBase request) {
            this.request = request;
        }

        public TestServerRegistrar mock() throws Exception {
            URI uri = request.getURI();

            PowerMockito.mockStatic(URIUtils.class, withSettings()
                            .name(methodName)
                            .defaultAnswer(invocation -> invocation.getMethod().getName().equals(methodName) ? httpHost : uri)
            );
            PowerMockito.when(URIUtils.class, methodName, uri).thenReturn(httpHost);
            return this;
        }

        public TestServerRegistrar register(HttpRequestHandler handler) throws Exception {
            server.register(pattern, handler);
            return this;
        }
    }
}
