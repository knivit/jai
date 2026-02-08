package com.tsoft.jai.reqwest;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class TestHttpRequestBodyPublisher implements HttpRequest.BodyPublisher {

    public static final TestHttpRequestBodyPublisher INSTANCE = new TestHttpRequestBodyPublisher();

    private final List<String> requests = new ArrayList<>();

    public static HttpRequest.BodyPublisher getPublisher(String body) {
        INSTANCE.requests.add(body);
        return INSTANCE;
    }

    public static void clearRequests() {
        INSTANCE.requests.clear();
    }

    public static List<String> getCapturedHttpRequests() {
        return INSTANCE.requests;
    }

    @Override
    public long contentLength() {
        return (requests.getLast() == null) ? 0 : requests.getLast().length();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {

    }
}
