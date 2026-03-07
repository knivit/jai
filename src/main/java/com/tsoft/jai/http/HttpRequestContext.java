package com.tsoft.jai.http;

import com.tsoft.jai.std.Result;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public class HttpRequestContext {

    @Getter
    private final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();

    @Getter
    private HttpMethod method;

    @Getter
    private String body;

    public Result<HttpRequestContext> setMethod(HttpMethod method) {
        this.method = method;
        return Ok(this);
    }

    public Result<HttpRequestContext> setUrl(String url) {
        try {
            httpRequestBuilder.uri(URI.create(url));
            return Ok(this);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public Result<HttpRequestContext> setHeaders(Map<String, List<String>> map) {
        if (!isEmpty(map)) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String name = entry.getKey();
                String value = isEmpty(entry.getValue()) ? null : String.join(";", entry.getValue());
                if (!isBlank(name) && !isBlank(value)) {
                    setHeader(name, value);
                }
            }
        }
        return Ok(this);
    }

    public Result<HttpRequestContext> setHeader(String name, String value) {
        try {
            httpRequestBuilder.header(name, value);
            return Ok(this);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public Result<HttpRequestContext> setBody(String body) {
        this.body = body;
        return Ok(this);
    }
}
