package com.tsoft.jai.reqwest;

import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class RequestBuilder {

    public enum HttpMethod {
        GET,
        POST
    }

    private final HttpMethod method;
    private final HttpRequest.Builder request = HttpRequest.newBuilder();

    public HttpClient build() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://foo.com/"))
            .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofFile(Paths.get("file.json")))
            .build();

        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
            .authenticator(Authenticator.getDefault())
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        return null;
    }

    public RequestBuilder url(String url) {
        request.uri(URI.create(url));
        return this;
    }

    public RequestBuilder header(String name, String value) {
        request.header(name, value);
        return this;
    }

    public RequestBuilder json(Value value) {
        String body = SerDe.toJsonString(value);
        request.POST(HttpRequest.BodyPublishers.ofString(body));
        return this;
    }

    public Response send() {
        return null;
    }
}
