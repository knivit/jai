package com.tsoft.jai.client.stream;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.reqwest.Event;
import com.tsoft.jai.reqwest.EventSource;
import com.tsoft.jai.reqwest.EventSourceError;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.anyhow.Result.isErr;
import static com.tsoft.jai.client.common.Common.catchError;

public class Stream {

    // pub async fn sse_stream<F>(builder: RequestBuilder, mut handle: F) -> Result<()>
    // where
    //    F: FnMut(SseMmessage) -> Result<bool>,
    // {
    //    let mut es = builder.eventsource()?;
    //    while let Some(event) = es.next().await {
    //        match event {
    //            Ok(Event::Open) => {}
    //            Ok(Event::Message(message)) => {
    //                let message = SseMmessage {
    //                    event: message.event,
    //                    data: message.data,
    //                };
    //                if handle(message)? {
    //                    break;
    //                }
    //            }
    //            Err(err) => {
    //                match err {
    //                    EventSourceError::StreamEnded => {}
    //                    EventSourceError::InvalidStatusCode(status, res) => {
    //                        let text = res.text().await?;
    //                        let data: Value = match text.parse() {
    //                            Ok(data) => data,
    //                            Err(_) => {
    //                                bail!(
    //                                    "Invalid response data: {text} (status: {})",
    //                                    status.as_u16()
    //                                );
    //                            }
    //                        };
    //                        catch_error(&data, status.as_u16())?;
    //                    }
    //                    EventSourceError::InvalidContentType(header_value, res) => {
    //                        let text = res.text().await?;
    //                        bail!(
    //                            "Invalid response event-stream. content-type: {}, data: {text}",
    //                            header_value.to_str().unwrap_or_default()
    //                        );
    //                    }
    //                    _ => {
    //                        bail!("{}", err);
    //                    }
    //                }
    //                es.close();
    //            }
    //        }
    //    }
    //    Ok(())
    // }
    public static Result<?> sseStream(RequestBuilder builder, StreamHandler handler) {
        EventSource es = builder.eventSource();

        Result<Event> res;
        boolean stop = false;
        while (!stop && (res = es.next()) != null) {
            switch (res.getType()) {
                case Ok: {
                    Event event = res.getValue();
                    switch (event.getType()) {
                        case Open -> {}
                        case Message -> {
                            SseMessage message = new SseMessage()
                                .setEvent(event.getMessage().getEvent())
                                .setData(event.getMessage().getData());
                            if (isErr(handler.handle(message))) {
                                stop = true;
                            }
                        }
                    }
                    break;
                }
                case Err: {
                    EventSourceError err = (EventSourceError) res.getErr().getErrValue();
                    switch (err.getType()) {
                        case StreamEnded -> {}
                        case InvalidStatusCode -> {
                            String text = err.getText();
                            Result<Value> data = SerDe.parseJson(text);
                            if (isErr(data)) {
                                return bail("Invalid response data: {} (status: {})", text, err.getStatus());
                            } else {
                                return catchError(data.getValue(), err.getStatus());
                            }
                        }
                        case InvalidContentType -> {
                            String text = err.getText();
                            String headerValue = err.getHeaderValue();
                            return bail("Invalid response event-stream. content-type: {}, data: {}", headerValue, text);
                        }
                        default -> {
                            return bail("{}", err);
                        }
                    }
                    es.close();
                    break;
                }
            }
        }

        return Ok();
    }
}
