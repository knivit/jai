package com.tsoft.jai.reqwest;

import java.time.Duration;

public class ClientBuilder {

    private final ReqwestClient client = new ReqwestClient();

    public ClientBuilder connectTimeout(Duration duration) {
        client.connectTimeout(duration);
        return this;
    }

    public ReqwestClient build() {
        return client;
    }
}
