package com.tsoft.jai.reqwest;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class RequestBuilder {

    public enum HttpMethod {
        GET,
        POST
    }

    private final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();

    private HttpClient httpClient;
    private HttpMethod httpMethod;
    private String body;

    public RequestBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public RequestBuilder httpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public RequestBuilder url(String url) {
        httpRequestBuilder.uri(URI.create(url));
        return this;
    }

    public RequestBuilder headers(Map<String, List<String>> map) {
        if (!isEmpty(map)) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String name = entry.getKey();
                String value = isEmpty(entry.getValue()) ? null : String.join(";", entry.getValue());
                if (!isBlank(name) && !isBlank(value)) {
                    header(name, value);
                }
            }
        }
        return this;
    }

    public RequestBuilder header(String name, String value) {
        httpRequestBuilder.header(name, value);
        return this;
    }

    public RequestBuilder json(Value value) {
        this.body = SerDe.toJsonString(value);
        return this;
    }

    public Result<Response> send() {
        HttpRequest request = switch (httpMethod) {
            case GET -> httpRequestBuilder.GET().build();
            case POST -> httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
        };

        try {
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Response response = new Response(StatusCode.of(httpResponse.statusCode()), httpResponse.body());
            return Ok(response);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public Event next() {

    }

    // public final class StreamingClient {
    //
    //    private final BlockingQueue<String> channel = new ArrayBlockingQueue<>(10_000);
    //    private volatile boolean completed = false;
    //
    //    /* ---------- public API ------------------------------------------------ */
    //
    //    /** Start the HTTP/2 stream in a background thread. */
    //    public void connect(String url) {
    //        HttpClient client = HttpClient.newBuilder()
    //                                      .version(HttpClient.Version.HTTP_2)
    //                                      .connectTimeout(Duration.ofSeconds(10))
    //                                      .build();
    //
    //        HttpRequest request = HttpRequest.newBuilder()
    //                                         .uri(URI.create(url))
    //                                         .timeout(Duration.ofSeconds(30))
    //                                         .headers("Accept", "text/plain")
    //                                         .GET()
    //                                         .build();
    //
    //        // non-blocking send: the subscriber will feed the queue
    //        client.sendAsync(request, this::toBlockingQueue);
    //    }
    //
    //    /**
    //     * Blocking call: returns the next payload or "[DONE]" when the server
    //     * closes the stream. After "[DONE]" every subsequent call immediately
    //     * returns "[DONE]".
    //     */
    //    public String next() {
    //        if (completed) return "[DONE]";
    //        try {
    //            String item = channel.take();          // blocks
    //            if ("[DONE]".equals(item)) completed = true;
    //            return item;
    //        } catch (InterruptedException e) {
    //            Thread.currentThread().interrupt();
    //            return "[DONE]";
    //        }
    //    }
    //
    //    /* ---------- internal plumbing ---------------------------------------- */
    //
    //    private HttpResponse.BodySubscriber<Void> toBlockingQueue(HttpResponse.ResponseInfo resp) {
    //        return new QueueFeeder();
    //    }
    //
    //    private final class QueueFeeder implements HttpResponse.BodySubscriber<Void> {
    //        private java.util.concurrent.Flow.Subscription subscription;
    //
    //        @Override
    //        public void onSubscribe(java.util.concurrent.Flow.Subscription s) {
    //            this.subscription = s;
    //            s.request(1);
    //        }
    //
    //        @Override
    //        public void onNext(java.util.List<java.nio.ByteBuffer> buffers) {
    //            // concat all buffers of this push
    //            StringBuilder sb = new StringBuilder();
    //            for (var bb : buffers) {
    //                byte[] bytes = new byte[bb.remaining()];
    //                bb.get(bytes);
    //                sb.append(new String(bytes));
    //            }
    //            channel.offer(sb.toString());            // non-blocking offer
    //            subscription.request(1);                 // keep demand open
    //        }
    //
    //        @Override
    //        public void onError(Throwable t) {
    //            channel.offer("[DONE]");
    //        }
    //
    //        @Override
    //        public void onComplete() {
    //            channel.offer("[DONE]");
    //        }
    //
    //        @Override
    //        public java.util.concurrent.CompletableFuture<Void> getBody() {
    //            return java.util.concurrent.CompletableFuture.completedFuture(null);
    //        }
    //    }
    //
    //    /* ---------- simple demo ------------------------------------------------ */
    //    public static void main(String[] args) throws Exception {
    //        StreamingClient client = new StreamingClient();
    //        client.connect("https://stream.example.com/events");
    //
    //        String chunk;
    //        while (!(chunk = client.next()).equals("[DONE]")) {
    //            System.out.println("Received: " + chunk);
    //        }
    //        System.out.println("Stream finished.");
    //    }
    // }

    // /**
    // * HTTP/2 streaming client – prints every server push to stdout.
    // * Run with:  java --enable-preview StreamingClient
    // * (preview flag only needed if you compile with Java 21-24; Java 25 removes it)
    // */
    //public class StreamingClient {
    //
    //    public static void main(String[] args) throws Exception {
    //
    //        // 1. Build an HTTP/2 client (HTTP/1.1 fallback disabled so we *must* speak h2)
    //        HttpClient client = HttpClient.newBuilder()
    //                .version(HttpClient.Version.HTTP_2)   // force h2
    //                .followRedirects(HttpClient.Redirect.NORMAL)
    //                .connectTimeout(Duration.ofSeconds(10))
    //                .build();
    //
    //        // 2. Build the request (replace with your streaming endpoint)
    //        HttpRequest request = HttpRequest.newBuilder()
    //                .uri(URI.create("https://stream.example.com/events"))
    //                .timeout(Duration.ofSeconds(30))
    //                .headers("Accept", "text/plain")
    //                .GET()
    //                .build();
    //
    //        // 3. Send the request and handle the response body as an endless stream
    //        client.sendAsync(request, BodyHandlers.ofLineSubscriber())
    //                .thenApply(HttpResponse::body)          // returns Flow.Subscriber<String>
    //                .thenAccept(sub -> ((LineSubscriber) sub).setStdoutConsumer())
    //                .join();                                // keep main thread alive
    //    }
    //
    //    /* Simple subscriber that prints every line the moment it arrives */
    //    private static class LineSubscriber implements HttpResponse.BodySubscriber<String> {
    //        private final java.util.concurrent.Flow.Subscriber<? super java.util.List<ByteBuffer>> upstream =
    //                new UpstreamPrinter();
    //
    //        @Override
    //        public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
    //            upstream.onSubscribe(subscription);
    //        }
    //
    //        @Override
    //        public void onNext(java.util.concurrent.Flow.Publisher<java.util.List<ByteBuffer>> item) {
    //            item.subscribe(upstream);
    //        }
    //
    //        @Override
    //        public void onError(Throwable throwable) {
    //            throwable.printStackTrace();
    //        }
    //
    //        @Override
    //        public void onComplete() {
    //            System.out.println("--- stream ended ---");
    //        }
    //
    //        @Override
    //        public java.util.concurrent.CompletionStage<String> getBody() {
    //            return java.util.concurrent.CompletableFuture.completedFuture("done");
    //        }
    //
    //        /* expose a convenience hook so main can wire stdout printing */
    //        void setStdoutConsumer() { /* no-op, already printing */ }
    //
    //        private static class UpstreamPrinter implements java.util.concurrent.Flow.Subscriber<java.util.List<ByteBuffer>> {
    //            private java.util.concurrent.Flow.Subscription sub;
    //
    //            @Override
    //            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
    //                this.sub = subscription;
    //                subscription.request(1); // start pumping
    //            }
    //
    //            @Override
    //            public void onNext(java.util.List<ByteBuffer> buffers) {
    //                buffers.forEach(bb -> {
    //                    byte[] bytes = new byte[bb.remaining()];
    //                    bb.get(bytes);
    //                    System.out.print(new String(bytes)); // raw bytes -> stdout
    //                });
    //                sub.request(1); // keep demand open
    //            }
    //
    //            @Override
    //            public void onError(Throwable t) { t.printStackTrace(); }
    //
    //            @Override
    //            public void onComplete() { System.out.flush(); }
    //        }
    //    }
    // }
}
