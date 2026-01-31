package com.tsoft.jai.reqwest;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import lombok.*;
import lombok.experimental.Accessors;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        try {
            HttpRequest request = httpRequest();
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Response response = new Response(StatusCode.of(httpResponse.statusCode()))
                .setValue(httpResponse.body());
            return Ok(response);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public EventSource eventSource() {
        HttpRequest request = httpRequest();
        EventSource eventSource = new EventSource();
        CompletableFuture<HttpResponse<Void>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.fromSubscriber(eventSource));
        future.handle(eventSource::onComplete);
        return eventSource;
    }

    private HttpRequest httpRequest() {
        return switch (httpMethod) {
            case GET -> httpRequestBuilder.GET().build();
            case POST -> httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
        };
    }
}
