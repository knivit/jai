package com.tsoft.jai.provider.openai.api;

import com.tsoft.jai.http.HttpMethod;
import com.tsoft.jai.http.HttpUtils;
import com.tsoft.jai.provider.openai.api.v1.Model;
import com.tsoft.jai.provider.openai.api.v1.Models;
import com.tsoft.jai.std.Result;
import com.tsoft.jai.utils.SerdeJson;

import java.util.List;

import static com.tsoft.jai.http.HttpUtils.buildHttpRequestContext;
import static com.tsoft.jai.std.Result.*;

public final class OpenAiApi {

    // HTTP GET http://localhost:11434/v1/models
    // {
    //  "object": "list",
    //  "data": [
    //    {
    //      "id": "rnj-1:latest",
    //      "object": "model",
    //      "created": 1771694631,
    //      "owned_by": "library"
    //    },
    //    ...
    // }
    public static Result<List<String>> getModels(String apiBase) {
        return Ok()
            .then(_ -> buildHttpRequestContext())
            .then(rqCtx -> rqCtx.setMethod(HttpMethod.GET))
            .then(rqCtx -> rqCtx.setUrl(apiBase + "/models"))
            .then(rqCtx -> rqCtx.setHeader("accept", "application/json"))
            .then(HttpUtils::buildHttpRequest)
            .then(HttpUtils::sendHttpRequest)
            .then(rsCtx -> rsCtx.getHttpCode() == 200 ? Ok(rsCtx.getBody()) : Err("Request for models failed."))
            .then(OpenAiApi::toModels);
    }

    private static Result<List<String>> toModels(String json) {
        Result<Models> res = SerdeJson.fromStr(json, Models.class);
        if (isErr(res)) {
            return Err(res);
        }

        return Ok(res.unwrap().getData().stream()
            .map(Model::getId)
            .toList());
    }

    private OpenAiApi() { }
}
