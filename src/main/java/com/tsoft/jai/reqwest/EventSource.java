package com.tsoft.jai.reqwest;

import com.tsoft.jai.anyhow.Result;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public class EventSource implements HttpResponse.BodySubscriber<Void> {

    public interface EventChannelFactory {

        BlockingQueue<String> build();
    }

    private static class EventChannelFactoryImpl implements EventChannelFactory {

        @Override
        public BlockingQueue<String> build() {
            return new ArrayBlockingQueue<>(100_000);
        }
    }

    public static EventChannelFactory eventChannelFactory = new EventChannelFactoryImpl();

    private static final String DONE_MARKER = UUID.randomUUID().toString();
    private static final String ERROR_MARKER = UUID.randomUUID().toString();

    public final BlockingQueue<String> channel = eventChannelFactory.build();
    private final AtomicReference<EventSourceError> error = new AtomicReference<>();
    private volatile boolean completed = false;

    private Flow.Subscription subscription;

    /**
     * Blocking call: returns the next payload or "[DONE]" when the server
     * closes the stream. After "[DONE]" every subsequent call immediately
     * returns "[DONE]".
     */
    public Result<Event> next() {
        if (completed) {
            return null;
        }

        try {
            String data = channel.take().trim();      // blocks

            if (DONE_MARKER.equals(data)) {
                completed = true;
                return null;
            }

            if (ERROR_MARKER.equals(data)) {
                completed = true;
                return Err(error.get());
            }

            MessageEvent message = new MessageEvent().setData(data);
            return Ok(Event.Message(message));
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            return Err(ex);
        }
    }

    public void close() {
        completed = true;
    }

    @Override
    public void onSubscribe(Flow.Subscription s) {
        this.subscription = s;
        s.request(1);
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        // concat all buffers of this push
        StringBuilder sb = new StringBuilder();
        for (ByteBuffer bb : buffers) {
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        }

        channel.offer(sb.toString());            // non-blocking offer
        subscription.request(1);                 // keep demand open
    }

    public Void onComplete(HttpResponse<?> response, Throwable ex) {
        if (ex != null) {
            onError(ex);
        } else if (response != null && response.statusCode() >= 400) {
            Object body = response.body();
            String error = (body == null) ? "Unexpected error from LLM" : body.toString();
            onError(response.statusCode(), error);
        } else {
            onComplete();
        }
        return null;
    }

    @Override
    public void onComplete() {
        channel.offer(DONE_MARKER);
    }

    @Override
    public void onError(Throwable ex) {
        onError(500, ex.getMessage());
    }

    public void onError(int statusCode, String message) {
        error.set(EventSourceError.InvalidStatusCode(statusCode, message));
        channel.offer(ERROR_MARKER);
    }

    @Override
    public CompletableFuture<Void> getBody() {
        return CompletableFuture.completedFuture(null);
    }
}
