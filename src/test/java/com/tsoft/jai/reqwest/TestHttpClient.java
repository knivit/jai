package com.tsoft.jai.reqwest;

import com.tsoft.jai.serdejson.SerDe;
import lombok.Getter;

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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class TestHttpClient extends HttpClient {

    @Getter
    private static String httpRequest;

    private static List<String> httpResponses;

    private static final BlockingQueue<String> eventChannel = new ArrayBlockingQueue<>(100_000);

    public static void newSession(String ... responses) {
        ReqwestClient.httpClientBuilder = new TestHttpClientBuilder();
        EventSource.eventChannelFactory = new TestEventChannelFactory(eventChannel);

        httpRequest = null;
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

    private List<ByteBuffer> toByteBuffers(String text) {
        return List.of(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        httpRequest = SerDe.toJsonString(request);
        HttpResponse.BodySubscriber<T> subscriber = responseBodyHandler.apply(new ResponseInfoImpl());
        subscriber.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                //
            }

            @Override
            public void cancel() {
                //
            }
        });

        for (String resp : httpResponses) {
            eventChannel.offer(resp);
        }

        CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();//() -> new TestHttpResponse<>() );
        return future;
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
