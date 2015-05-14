package com.mxmind.tripleware.rxflow;

import com.mxmind.tripleware.publicprofile.utils.EmailEncoder;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
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
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
 * @version 1.0.0
 * @author mxmind
 * @since 1.0.0
 */
@Service("testService")
public class TestService {

    private static final Logger LOG = LoggerFactory.getLogger(TestService.class);

    private static final String GRAVATAR_URL = "http://gravatar.com/avatar/%s?s=200";

    private static final String FACEBOOK_URL = "http://graph.facebook.com/%s/picture?type=large";

    @Resource(name = "httpClient")
    private HttpClient client;

    private final AtomicBoolean result = new AtomicBoolean();

    public Boolean processGravatarPicture() {
        Flow.initialize(States.gravatar, this::onComplete, this::onError);
        return result.get();
    }

    public Boolean processFacebookPicture() {
        Flow.initialize(States.facebook, this::onComplete, this::onError);
        return result.get();
    }

    public Boolean processManualPicture() {
        Flow.initialize(States.manual, this::onComplete, this::onError);
        return result.get();
    }

    /*
     * section: fsm handlers
     */

    private void onComplete(Flow.FlowObserver<Picture> fsm){
        result.set(fsm.getData().isDownloaded());
    }

    private void onError(Flow.FlowObserver<Picture> fsm, Exception ex) {
        if(LOG.isErrorEnabled()) {
            LOG.error(String.format("Exception occurred on state: %s", fsm.fromState()), ex);
        }
        fsm.onNext(States.error);
    }

    /*
     * section: picture processing
     */

    private void prepareGravatarPicture(Transition<Picture> transition) {
        final Picture picture = new Picture();
        final String emailHash = EmailEncoder.encode(MessageDigestAlgorithms.MD5, "mxmind@gmail.com");

        picture.setUrl(String.format(GRAVATAR_URL, emailHash));
        picture.setSource("gravatar");

        transition.setData(picture);
    }

    private void prepareFacebookPicture(Transition<Picture> transition) {
        final Picture picture = new Picture();
        final String fbUid = "100003234733056";

        picture.setUrl(String.format(FACEBOOK_URL, fbUid));
        picture.setSource("facebook");

        transition.setData(picture);
    }

    private void prepareManualPicture(Transition<Picture> transition) {
        final Picture picture = new Picture();
        picture.setSource("manual");

        transition.setData(picture);
    }

    private void receivePicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();

        DefaultHttpClient client = new SystemDefaultHttpClient();
        client.setHttpRequestRetryHandler(new StandardHttpRequestRetryHandler(2, true));
        client.getParams().setIntParameter(ClientPNames.MAX_REDIRECTS, 10);
        client.getParams().setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);

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
        } catch (IOException ex) {
            transition.fsm().onError(ex);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    private void processExternalPicture(Transition<Picture> transition) {

    }

    private void processPicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();

        BufferedImage source = (BufferedImage) picture.getImage();
        BufferedImage image = new BufferedImage(100, 100, source.getType());

        int width = source.getWidth(), height = source.getHeight();
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, 100, 100, 0, 0, width, height, null);
        graphics.dispose();

        picture.setImage(image);
    }

    private void savePicture(Transition<Picture> transition) {
        final Picture picture = transition.getData();
        final String ext = picture.getContentType().equalsIgnoreCase("image/png") ? "png" : "jpg";

        try {
            final String pathToFile = String.format(
                "/Users/vzdomish/Development/RxPicture/src/test/resources/%s.%s",
                picture.getSource(),
                ext
            );
            Path path = Paths.get(pathToFile);
            if (Files.exists(path)) Files.delete(path);

            File dest = new File(path.toString());
            if(dest.createNewFile()) {
                ImageIO.write(picture.getImage(), ext, dest);

                picture.setDownloaded(true);
                picture.setUuid(UUID.randomUUID().toString());
            }
        } catch (IOException ex) {
            transition.fsm().onError(ex);
        }
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
