package com.tsoft.jai.reqwest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;

public class TestHttpClient extends HttpClient {

    private static List<String> httpResponses;

    private static final BlockingQueue<String> eventChannel = new ArrayBlockingQueue<>(16);

    public static void newSession() {
        ReqwestClient.httpClientBuilder = new TestHttpClientBuilder();
        RequestBuilder.getHttpBodyPublisher = TestHttpRequestBodyPublisher::getPublisher;
        EventSource.eventChannelFactory = new TestEventChannelFactory(eventChannel);

        TestHttpRequestBodyPublisher.clearRequests();
        eventChannel.clear();
    }

    public static void prepareResponses(String ... responses) {
        httpResponses = Arrays.asList(responses);
    }

    public static class ResponseInfoImpl implements HttpResponse.ResponseInfo {

        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public Version version() {
            return Version.HTTP_2;
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        // All the responses already sent
        if (isEmpty(httpResponses)) {
            CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
            future.complete(new TestHttpResponse<>(500));
            return future;
        }

        responseBodyHandler.apply(new ResponseInfoImpl());

        // Send all the SSE-responses to the channel
        for (String resp : httpResponses) {
            eventChannel.offer(resp);
        }

        // Clear sent responses
        httpResponses = null;

        // Send the final response
        CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
        future.complete(new TestHttpResponse<>(200));

        return future;
    }

    public static List<String> getCapturedHttpRequests() {
        return TestHttpRequestBodyPublisher.getCapturedHttpRequests();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return null;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return null;
    }
}
