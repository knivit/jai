package com.tsoft.jai.http;

import com.tsoft.jai.std.Result;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;

public final class HttpUtils {

    public static HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
    public static Function<String, HttpRequest.BodyPublisher> getHttpBodyPublisher = HttpRequest.BodyPublishers::ofString;

    public static Result<HttpRequestContext> buildHttpRequestContext() {
        return Ok(new HttpRequestContext());
    }

    public static Result<HttpRequest> buildHttpRequest(HttpRequestContext ctx) {
        try {
            return Ok(switch (ctx.getMethod()) {
                case GET -> ctx.getHttpRequestBuilder().GET().build();
                case POST -> ctx.getHttpRequestBuilder().POST(getHttpBodyPublisher.apply(ctx.getBody())).build();
            });
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<HttpResponseContext> sendHttpRequest(HttpRequest rq) {
        try (HttpClient httpClient = httpClientBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)
                .build()) {
            HttpResponse<String> httpResponse = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());
            HttpResponseContext rs = new HttpResponseContext(httpResponse.statusCode(), httpResponse.body());
            return Ok(rs);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<?> sendHttpStream(HttpRequest rq, Consumer<String> onLine) {
        try (HttpClient httpClient = httpClientBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)
                .build()) {
            httpClient.send(rq, HttpResponse.BodyHandlers.ofLines())
                .body()
                .forEach(onLine);
            return Ok();
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private HttpUtils() { }
}
