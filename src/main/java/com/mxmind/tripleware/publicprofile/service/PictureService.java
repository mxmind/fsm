package com.mxmind.tripleware.publicprofile.service;

import com.mxmind.tripleware.publicprofile.dtos.*;
import com.mxmind.tripleware.publicprofile.rxflow.Flow;
import com.mxmind.tripleware.publicprofile.rxflow.FlowStates;
import com.mxmind.tripleware.publicprofile.rxflow.Matcher;
import com.mxmind.tripleware.publicprofile.rxflow.Transition;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
@Service("testService")
public class PictureService {

    private static final Logger LOG = LoggerFactory.getLogger(PictureService.class);

    public static final int MAX_HEIGHT = 600;

    public static final int MAX_WIDTH = 910;

    private final AtomicBoolean result = new AtomicBoolean();

    public Boolean processGravatarPicture() {
        final Picture picture = new GravatarPicture("test@completelybogus.wvrgroup.internal");
        return initFromState(States.gravatar, picture);
    }

    public Boolean processFacebookPicture() {
        final Picture picture = new FacebookPicture("100003234733056");

        return initFromState(States.facebook, picture);
    }

    public Boolean processManualPicture() {
        final Picture picture = new Picture();
        return initFromState(States.manual, picture);
    }

    private Boolean initFromState(States state, Picture picture) {
        Flow.initialize(state, picture, this::onComplete, this::onError);
        return result.get();
    }

    /*
     * section: transition handlers
     */

    private void onComplete(Transition<Picture> transition) {
        result.set(transition.getData().isDownloaded());
    }

    private void onError(Transition<Picture> transition, Exception ex) {

        Matcher<Exception, Picture> matcher = Matcher.when(NullPointerException.class::isInstance, cause -> {
            LOG.error(String.format("Exception occurred on state: %s", transition.fromState()), cause);
            return transition.getData();
        });

        matcher.match(ex).ifPresent(value -> transition.onNext(States.error));
    }

    /*
     * section: picture processing
     */

    private void prepareGravatarPicture(final Picture picture) {
        picture.setOptions(PictureOptions.LARGE);
    }

    private void prepareFacebookPicture(final Picture picture) {
        picture.setOptions(PictureOptions.LARGE);
    }

    private void prepareManualPicture(final Picture picture) {
        picture.setSource("manual");
    }

    protected void checkForPicture(final Picture picture) throws IOException {

        if (FacebookPicture.class.isInstance(picture)) {
            picture.setAvailable(true);
            return;
        }

        HttpClient client = getDefaultHttpClient();
        HttpGet get = new HttpGet(((GravatarPicture) picture).getCheckUrl());

        client.execute(get, response -> {
            picture.setAvailable(response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND);
            return response;
        });
        client.getConnectionManager().shutdown();
    }

    protected void receivePicture(final Picture picture) throws IOException {
        HttpClient client = getDefaultHttpClient();
        HttpGet get = new HttpGet(picture.getUrl());

        HttpResponse response = client.execute(get);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();

            if (entity.isStreaming()) {
                BufferedImage source = ImageIO.read(entity.getContent());

                picture.setContentType(entity.getContentType().getValue());
                picture.setImage(source);
                picture.setDownloaded(true);
            }
        }
        client.getConnectionManager().shutdown();
    }

    protected void processExternalPicture(final Picture picture) {
    }

    protected void resizePicture(final Picture picture) {
        BufferedImage source = (BufferedImage) picture.getImage();
        int w = source.getWidth(), h = source.getHeight();

        if (h > MAX_HEIGHT || w > MAX_WIDTH) {
            float scaleFactor = w > h ? (float) w / MAX_WIDTH : (float) h / MAX_HEIGHT;
            int sw = (int) (w / scaleFactor);
            int sh = (int) (h / scaleFactor);

            BufferedImage resizedImage = new BufferedImage(sw, sh, source.getType());

            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);

            g.drawImage(source, 0, 0, sw, sh, null);

            g.dispose();

            picture.setImage(resizedImage);
        }
    }

    protected void extendPicture(final Picture picture) {
        BufferedImage source = (BufferedImage) picture.getImage();
        int w = source.getWidth(), h = source.getHeight();
        int minHeight = picture.getOptions().getMinHeight();

        if (h < minHeight) {
            int y = (minHeight - h) >> 1;
            BufferedImage image = new BufferedImage(w, minHeight, source.getType());

            // no render hints, just copy pixels from original image;
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, minHeight);
            g.drawImage(source, 0, y, w, h + y, 0, 0, w, h, null);
            g.dispose();

            picture.setImage(image);
        }
    }

    public void cropPicture(Picture picture) {
        if(!picture.getCrop().isEmpty()) {
            Crop crop = picture.getCrop();
            BufferedImage source = (BufferedImage) picture.getImage();
            BufferedImage image = source.getSubimage(crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight());

            picture.setImage(image);
        }
    }

    protected void savePicture(final Picture picture) throws IOException {
        String ext = picture.getContentType().equalsIgnoreCase("image/png") ? "png" : "jpg";
        String pathToFile = String.format("%s/src/test/resources/%s.%s", System.getProperty("user.dir"), picture.getSource(), ext);

        Path dest = Paths.get(pathToFile);
        if (Files.exists(dest)) {
            Files.delete(dest);
        }
        ImageIO.write(picture.getImage(), ext, Files.createFile(dest).toFile());

        picture.setSaved(true);
        picture.setUuid(UUID.randomUUID().toString());
    }

    protected HttpClient getDefaultHttpClient() {
        DefaultHttpClient client = new SystemDefaultHttpClient();
        client.setHttpRequestRetryHandler(new StandardHttpRequestRetryHandler(3, true));
        client.getParams().setIntParameter(ClientPNames.MAX_REDIRECTS, 10);
        client.getParams().setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

        return client;
    }

    /*
     * section: transition states
     */

    private enum States implements FlowStates<Picture> {

        gravatar {
            @Override
            public void onTransition(Transition<Picture> transition) {
                service.prepareGravatarPicture(transition.getData());
                transition.onNext(receive_picture);
            }
        },

        facebook {
            @Override
            public void onTransition(Transition<Picture> transition) {
                service.prepareFacebookPicture(transition.getData());
                transition.onNext(receive_picture);
            }
        },

        manual {
            @Override
            public void onTransition(Transition<Picture> transition) {
                service.prepareManualPicture(transition.getData());
                transition.onNext(receive_picture);
            }
        },

        receive_picture {
            @Override
            public void onTransition(Transition<Picture> transition) {
                try {
                    service.receivePicture(transition.getData());
                } catch (IOException ex) {
                    transition.onError(ex);
                }
                transition.onNext(process_picture);
            }
        },

        process_picture {
            @Override
            public void onTransition(Transition<Picture> transition) {
                service.extendPicture(transition.getData());
                transition.onNext(complete);
            }
        },

        error {
            @Override
            public void onTransition(Transition<Picture> transition) {
                 transition.onCompleted();
            }
        },

        complete {
            @Override
            public void onTransition(Transition<Picture> transition) {
                final Picture data = transition.getData();

                Matcher<Object, Picture> matcher = Matcher.when(process_picture::equals, state -> {
                    try {
                        service.savePicture(data);
                    } catch (IOException ex) {
                        transition.onError(ex);
                    }
                    return data;
                }).orWhen(error::equals, state -> {
                    // do something useful;
                    return data;
                }).otherwise(state -> transition.getData());

                matcher.match(transition.fromState()).ifPresent(picture -> {
                    // do something useful;
                    transition.onCompleted();
                });
            }
        };

        protected PictureService service;

        public void setService(PictureService service) {
            this.service = service;
        }

        @Component
        public static class Injector {

            @Inject
            private PictureService service;

            @PostConstruct
            public void postConstruct() {
                EnumSet.allOf(States.class).forEach(state -> state.setService(service));
            }
        }
    }
}
