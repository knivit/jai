package com.tsoft.jai.client.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class RequestData {

    private String url;
    private Map<String, String> headers;
    private Object body;

    public RequestData(String url, Object body) {
        this.url = url;
        this.headers = new LinkedHashMap<>();
        this.body = body;
    }

    // pub fn bearer_auth<T>(&mut self, auth: T)
    // where
    //     T: std::fmt::Display,
    // {
    //     self.headers
    //         .insert("authorization".into(), format!("Bearer {auth}"));
    // }
    public void bearerAuth(String auth) {
        headers.put("authorization", "Bearer %s".formatted(auth));
    }
}
