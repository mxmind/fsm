package com.mxmind.tripleware.publicprofile.common;

import com.mxmind.tripleware.publicprofile.service.PictureService;
import org.springframework.context.annotation.*;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */

@Configuration
@ComponentScan("com.mxmind.tripleware.publicprofile.rxflow")
public class PictureConfiguration {

    @Bean
    PictureService testService(){
        return new PictureService();
    }
}
