package com.mxmind.tripleware.rxflow.configs;

import com.mxmind.tripleware.rxflow.TestService;
import org.springframework.context.annotation.*;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */

@Configuration
@ComponentScan("com.mxmind.tripleware.rxflow")
public class TestConfig {

    @Bean
    HttpClientFactoryBean httpClient(){
        return new HttpClientFactoryBean();
    }

    @Bean
    TestService testService(){
        return new TestService();
    }
}
