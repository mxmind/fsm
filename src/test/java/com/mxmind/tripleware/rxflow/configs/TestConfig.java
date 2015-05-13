package com.mxmind.tripleware.rxflow.configs;

import com.mxmind.tripleware.rxflow.TestService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.client.SystemDefaultHttpClient;
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
    HttpClient httpClient(){
        DefaultHttpClient client = new SystemDefaultHttpClient();
        HttpRequestRetryHandler handler = new StandardHttpRequestRetryHandler(3, true);
        client.setHttpRequestRetryHandler(handler);

        return client;
    }

    @Bean
    TestService testService(){
        return new TestService();
    }
}
