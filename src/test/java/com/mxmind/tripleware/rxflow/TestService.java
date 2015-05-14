package com.mxmind.tripleware.rxflow;

import com.mxmind.tripleware.publicprofile.dtos.FacebookPicture;
import com.mxmind.tripleware.publicprofile.dtos.GravatarPicture;
import com.mxmind.tripleware.publicprofile.dtos.Picture;
import com.mxmind.tripleware.publicprofile.dtos.PictureOptions;
import org.apache.http.HttpEntity;
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
public class TestService {

    private static final Logger LOG = LoggerFactory.getLogger(TestService.class);

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
     * section: fsm handlers
     */

    private void onComplete(Flow.FlowObserver<Picture> fsm) {
        result.set(fsm.getData().isDownloaded());
    }

    private void onError(Flow.FlowObserver<Picture> fsm, Exception ex) {
        if (LOG.isErrorEnabled()) {
            LOG.error(String.format("Exception occurred on state: %s", fsm.fromState()), ex);
        }
        fsm.onNext(States.error);
    }

    /*
     * section: picture processing
     */

    private void prepareGravatarPicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();
        picture.setOptions(PictureOptions.LARGE);
    }

    private void prepareFacebookPicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();
        picture.setOptions(PictureOptions.LARGE);
    }

    private void prepareManualPicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();
        picture.setSource("manual");
    }

    private boolean checkForPicture(Transition<Picture> transition) {
        AtomicBoolean result = new AtomicBoolean(Boolean.FALSE);

        if (FacebookPicture.class.isInstance(transition.getData())) {
            return result.get();
        }
        GravatarPicture picture = (GravatarPicture) transition.getData();

        HttpClient client = getDefaultHttpClient();
        HttpGet get = new HttpGet(picture.getCheckUrl());

        try {
            client.execute(get, response -> {
                result.set(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND);
                return response;
            });
            client.getConnectionManager().shutdown();
        } catch (IOException ex) {
            transition.fsm().onError(ex);
        }
        return result.get();
    }

    private void receivePicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();

        //TODO: create factory
        HttpClient client = getDefaultHttpClient();
        HttpGet get = new HttpGet(transition.getData().getUrl());

        try {
            client.execute(get, response -> {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();

                    if (entity.isStreaming()) {
                        picture.setContentType(entity.getContentType().getValue());
                        BufferedImage source = ImageIO.read(entity.getContent());
                        picture.setImage(source);
                    }
                }

                return response;
            });
            client.getConnectionManager().shutdown();
        } catch (IOException ex) {
            transition.fsm().onError(ex);
        }
    }

    private void processExternalPicture(Transition<Picture> transition) {

    }

    private void processPicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();

        BufferedImage source = (BufferedImage) picture.getImage();
        int w = source.getWidth(), h = source.getHeight();
        int minHeight = picture.getOptions().getMinHeight();

        if (h < minHeight) {
            int y = (minHeight - h) >> 1;
            BufferedImage image = new BufferedImage(w, minHeight, source.getType());

            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, minHeight);
            g.drawImage(source, 0, y, w, h + y, 0, 0, w, h, null);
            g.dispose();

            picture.setImage(image);
        }
    }

    private void savePicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();

        try {
            String ext = picture.getContentType().equalsIgnoreCase("image/png") ? "png" : "jpg";
            String pathToFile = String.format(
                    "/Users/vzdomish/Development/RxPicture/src/test/resources/%s.%s",
                    picture.getSource(),
                    ext
            );
            Path dest = Paths.get(pathToFile);
            if (Files.exists(dest)) {
                Files.delete(dest);
            }
            ImageIO.write(picture.getImage(), ext, Files.createFile(dest).toFile());

            picture.setDownloaded(true);
            picture.setUuid(UUID.randomUUID().toString());

        } catch (IOException ex) {
            transition.fsm().onError(ex);
        }
    }

    private HttpClient getDefaultHttpClient() {
        DefaultHttpClient client = new SystemDefaultHttpClient();
        client.setHttpRequestRetryHandler(new StandardHttpRequestRetryHandler(2, true));
        client.getParams().setIntParameter(ClientPNames.MAX_REDIRECTS, 10);
        client.getParams().setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);

        return client;
    }

    /*
     * section: fsm states
     */

    private enum States implements FlowStates<Picture> {

        gravatar {
            @Override
            public void onTransition(Transition<Picture> transition) {
                transition.handle((state) -> {
                    service.prepareGravatarPicture(transition);
                    transition.fsm().onNext(receive_picture);
                });
            }
        },

        facebook {
            @Override
            public void onTransition(Transition<Picture> transition) {
                transition.handle((state) -> {
                    service.prepareFacebookPicture(transition);
                    transition.fsm().onNext(receive_picture);
                });
            }
        },

        manual {
            @Override
            public void onTransition(Transition<Picture> transition) {
                transition.handle((state) -> {
                    service.prepareManualPicture(transition);
                    transition.fsm().onNext(receive_picture);
                });
            }
        },

        receive_picture {
            @Override
            public void onTransition(Transition<Picture> transition) {
                transition.handle((state) -> {
                    service.receivePicture(transition);
                    transition.fsm().onNext(process_picture);
                });
            }
        },

        process_picture {
            @Override
            public void onTransition(Transition<Picture> transition) {
                transition.handle((state) -> {
                    service.processPicture(transition);
                    transition.fsm().onNext(complete);
                });
            }
        },

        error {
            @Override
            public void onTransition(Transition<Picture> transition) {
                // perform recovery
                super.onTransition(transition);
            }
        },

        complete {
            @Override
            public void onTransition(Transition<Picture> transition) {
                transition.handle((state) -> {
                    if (state.equals(process_picture)) {
                        service.savePicture(transition);
                    }
                });

                super.onTransition(transition);
            }
        };

        @Component
        public static class Injector {

            @Inject
            private TestService service;

            @PostConstruct
            public void postConstruct() {
                EnumSet.allOf(States.class).forEach(state -> state.setService(service));
            }
        }

        protected TestService service;

        public void setService(TestService service) {
            this.service = service;
        }
    }
}
