package com.tsoft.jai.reqwest;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReqwestClient {

    private final HttpClient.Builder clientBuilder = HttpClient.newBuilder();
    private final Map<String, List<String>> headerMap = new LinkedHashMap<>();

    private HttpClient client;

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public ReqwestClient insertHeader(String name, String value) {
        List<String> values = headerMap.computeIfAbsent(name, (e) -> new ArrayList<>());
        values.add(value);
        return this;
    }

    public RequestBuilder post(String url) {
        return new RequestBuilder(RequestBuilder.HttpMethod.POST)
            .url(url);
    }

    public ReqwestClient connectTimeout(Duration duration) {
        clientBuilder.connectTimeout(duration);
        return this;
    }

    public ReqwestClient build() {
        client = clientBuilder.build();
        return this;
    }
}
