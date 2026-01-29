package com.tsoft.jai.reqwest;

import lombok.Getter;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ReqwestClient {

    private final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
    private final Map<String, List<String>> headerMap = new LinkedHashMap<>();

    private HttpClient httpClient;

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public RequestBuilder post(String url) {
        return new RequestBuilder()
            .httpClient(httpClient)
            .httpMethod(RequestBuilder.HttpMethod.POST)
            .url(url)
            .headers(headerMap);
    }

    public ReqwestClient insertHeader(String name, String value) {
        List<String> values = headerMap.computeIfAbsent(name, (e) -> new ArrayList<>());
        values.add(value);
        return this;
    }

    public ReqwestClient connectTimeout(Duration duration) {
        httpClientBuilder.connectTimeout(duration);
        return this;
    }

    public ReqwestClient build() {
        httpClient = httpClientBuilder
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        return this;
    }
}
