package com.tsoft.jai.client.stream;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.reqwest.RequestBuilder;

import static com.tsoft.jai.anyhow.Result.Ok;

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
        String chunk;
        while (!(chunk = builder.next()).equals("[DONE]")) {
            System.out.println("Received: " + chunk);
        }
        return Ok();
    }
}
