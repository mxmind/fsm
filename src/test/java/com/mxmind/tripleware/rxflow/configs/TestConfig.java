package com.mxmind.tripleware.rxflow.configs;

import com.mxmind.tripleware.rxflow.TestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
    TestService testService(){
        return new TestService();
    }
}
