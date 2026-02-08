package com.tsoft.jai.reqwest;

import lombok.Data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TestHttpRequestBuilder implements HttpRequest.Builder {

    @Data
    public static class Request {
        private String method;
        private URI uri;
        private Map<String, String> headers;
        private Duration timeout;
    }

    private Request request;

    @Override
    public HttpRequest.Builder uri(URI uri) {
        request.uri = uri;
        return this;
    }

    @Override
    public HttpRequest.Builder expectContinue(boolean enable) {
        return this;
    }

    @Override
    public HttpRequest.Builder version(HttpClient.Version version) {
        return this;
    }

    private void ensureHeaders() {
        if (request.headers == null) {
            request.headers = new HashMap<>();
        }
    }

    @Override
    public HttpRequest.Builder header(String name, String value) {
        ensureHeaders();
        request.headers.put(name, value);
        return this;
    }

    @Override
    public HttpRequest.Builder headers(String... headers) {
        ensureHeaders();
        for (int i = 0; i < headers.length; i += 2) {
            request.headers.put(headers[i], headers[i + 1]);
        }
        return this;
    }

    @Override
    public HttpRequest.Builder timeout(Duration duration) {
        request.timeout = duration;
        return null;
    }

    @Override
    public HttpRequest.Builder setHeader(String name, String value) {
        return null;
    }

    @Override
    public HttpRequest.Builder GET() {
        request.method = "GET";
        return this;
    }

    @Override
    public HttpRequest.Builder POST(HttpRequest.BodyPublisher bodyPublisher) {
        request.method = "POST";
        return this;
    }

    @Override
    public HttpRequest.Builder PUT(HttpRequest.BodyPublisher bodyPublisher) {
        return null;
    }

    @Override
    public HttpRequest.Builder DELETE() {
        return null;
    }

    @Override
    public HttpRequest.Builder method(String method, HttpRequest.BodyPublisher bodyPublisher) {
        return null;
    }

    @Override
    public HttpRequest build() {
        return null;
    }

    @Override
    public HttpRequest.Builder copy() {
        return null;
    }
}
