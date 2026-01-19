package com.tsoft.jai.utils.loader;

import com.tsoft.jai.utils.Tuple;

import java.util.Map;

import static com.tsoft.jai.utils.StringUtils.isBlank;
import static com.tsoft.jai.utils.StringUtils.splitOnce;

public final class Loader {

    // pub fn is_loader_protocol(loaders: &HashMap<String, String>, path: &str) -> bool {
    //    match path.split_once(':') {
    //        Some((protocol, _)) => loaders.contains_key(protocol),
    //        None => false,
    //    }
    // }
    public static boolean isLoaderProtocol(Map<String, String> loaders, String path) {
        Tuple<String, String> tuple = splitOnce(path, ':');
        String protocol = tuple.first();
        String __ = tuple.second();
        if (!isBlank(protocol) && !isBlank(__)) {
            return loaders.containsKey(protocol);
        }
        return false;
    }

    private Loader() { }
}
