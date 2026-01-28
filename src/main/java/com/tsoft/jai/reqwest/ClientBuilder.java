package com.tsoft.jai.reqwest;

import com.tsoft.jai.anyhow.Result;

import java.time.Duration;

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

public class ClientBuilder {

    private static final String USER_AGENT = "user-agent";

    private final ReqwestClient client = new ReqwestClient();

    public ClientBuilder connectTimeout(Duration duration) {
        client.connectTimeout(duration);
        return this;
    }

    public Result<ReqwestClient> build() {
        client.build();
        return Ok(client);
    }

    // pub fn user_agent<V>(mut self, value: V) -> ClientBuilder
    // where
    //     V: TryInto<HeaderValue>,
    //     V::Error: Into<http::Error>,
    // {
    //     match value.try_into() {
    //         Ok(value) => {
    //             self.config.headers.insert(USER_AGENT, value);
    //         }
    //         Err(e) => {
    //             self.config.error = Some(crate::error::builder(e.into()));
    //         }
    //     };
    //     self
    // }
    public ClientBuilder userAgent(String value) {
        if (!isBlank(value)) {
            client.insertHeader(USER_AGENT, value);
        }
        return this;
    }

    // pub fn no_proxy(mut self) -> ClientBuilder {
    //    self.config.proxies.clear();
    //    self.config.auto_sys_proxy = false;
    //    self
    // }
    public ClientBuilder noProxy() {
        return this;
    }
}
