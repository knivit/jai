package com.tsoft.jai.reqwest;

import java.net.http.HttpClient;
import java.time.Duration;

public class ReqwestClient {

    private final HttpClient.Builder client = HttpClient.newBuilder();

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public RequestBuilder post(String url) {
        return new RequestBuilder(RequestBuilder.HttpMethod.POST)
            .url(url);
    }

    public ReqwestClient connectTimeout(Duration duration) {
        client.connectTimeout(duration);
        return this;
    }
}
