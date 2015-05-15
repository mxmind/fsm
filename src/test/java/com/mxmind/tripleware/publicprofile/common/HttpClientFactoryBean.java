package com.mxmind.tripleware.publicprofile.common;

import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.*;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class HttpClientFactoryBean implements FactoryBean<HttpClient> {

    @Override
    public HttpClient getObject() throws Exception {
        DefaultHttpClient client = new SystemDefaultHttpClient();
        HttpRequestRetryHandler handler = new StandardHttpRequestRetryHandler(2, true);
        client.setHttpRequestRetryHandler(handler);
        client.getParams().setIntParameter(ClientPNames.MAX_REDIRECTS, 10);
        client.getParams().setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);

        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return HttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
