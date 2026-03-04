package com.tsoft.jai.client.common;

import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.reqwest.ReqwestClient;
import com.tsoft.jai.serde.Value;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.tsoft.jai.serde.Value.jsonPatch;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Slf4j
@Data
@Accessors(chain = true)
public class RequestData {

    private String url;
    private Map<String, String> headers;
    private Value body;

    public RequestData(String url, Value body) {
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

    // pub fn header<K, V>(&mut self, key: K, value: V)
    // where
    //     K: std::fmt::Display,
    //     V: std::fmt::Display,
    // {
    //     self.headers.insert(key.to_string(), value.to_string());
    // }
    public void header(String key, String value) {
        headers.put(key, value);
    }

    // pub fn into_builder(self, client: &ReqwestClient) -> RequestBuilder {
    //    let RequestData { url, headers, body } = self;
    //    debug!("Request {url} {body}");
    //
    //    let mut builder = client.post(url);
    //    for (key, value) in headers {
    //        builder = builder.header(key, value);
    //    }
    //    builder = builder.json(&body);
    //    builder
    // }
    public RequestBuilder intoBuilder(ReqwestClient client) {
        log.trace("Request {} {}", url, body);

        RequestBuilder builder = client.post(url);
        if (!isEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder = builder.header(key, value);
            }
        }
        builder = builder.json(body);
        return builder;
    }

    // pub fn apply_patch(&mut self, patch: Value) {
    //    if let Some(patch_url) = patch["url"].as_str() {
    //        self.url = patch_url.into();
    //    }
    //    if let Some(patch_body) = patch.get("body") {
    //        json_patch::merge(&mut self.body, patch_body)
    //    }
    //    if let Some(patch_headers) = patch["headers"].as_object() {
    //        for (key, value) in patch_headers {
    //            if let Some(value) = value.as_str() {
    //                self.header(key, value)
    //            } else if value.is_null() {
    //                self.headers.swap_remove(key);
    //            }
    //        }
    //    }
    // }
    public void applyPatch(Value patch) {
        String patchUrl = patch.asStr("url");
        if (!isBlank(patchUrl)) {
            url = patchUrl;
        }
        Value patchBody = patch.get("body");
        if (patchBody != null) {
            body = jsonPatch(body, patchBody);
        }
        Map<String, String> patchHeaders = patch.asMap("headers");
        if (!isEmpty(patchHeaders)) {
            for (Map.Entry<String, String> entry : patchHeaders.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!isBlank(value)) {
                    header(key, value);
                } else if (value == null) {
                    headers.remove(key);
                }
            }
        }
    }
}
