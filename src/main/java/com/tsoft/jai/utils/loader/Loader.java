package com.tsoft.jai.utils.loader;

import com.tsoft.jai.utils.base.Tuple;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.utils.PathUtil.getPatchExtension;
import static com.tsoft.jai.utils.Request.DEFAULT_EXTENSION;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;
import static com.tsoft.jai.utils.base.StringUtils.splitOnce;
import static com.tsoft.jai.utils.command.Command.runLoaderCommand;

public final class Loader {

    private static final String EXTENSION_METADATA = "__extension__";

    // pub async fn load_file(loaders: &HashMap<String, String>, path: &str) -> Result<LoadedDocument> {
    //    let extension = get_patch_extension(path).unwrap_or_else(|| DEFAULT_EXTENSION.into());
    //    match loaders.get(&extension) {
    //        Some(loader_command) => load_with_command(path, &extension, loader_command),
    //        None => load_plain(path, &extension).await,
    //    }
    // }
    public static LoadedDocument loadFile(Map<String, String> loaders, String path) {
        String extension = getPatchExtension(path);
        if (isBlank(extension)) {
            extension = DEFAULT_EXTENSION;
        }
        String loaderCommand = loaders.get(extension);
        if (!isBlank(loaderCommand)) {
            return loadWithCommand(path, extension, loaderCommand);
        } else {
            return loadPlain(path, extension);
        }
    }

    // async fn load_plain(path: &str, extension: &str) -> Result<LoadedDocument> {
    //    let contents = tokio::fs::read_to_string(path).await?;
    //    let mut metadata: DocumentMetadata = Default::default();
    //    metadata.insert(EXTENSION_METADATA.into(), extension.to_string());
    //    Ok(LoadedDocument::new(path.into(), contents, metadata))
    // }
    private static LoadedDocument loadPlain(String path, String extension) {
        try {
            String contents = Files.readString(Paths.get(path));
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put(EXTENSION_METADATA, extension);
            return new LoadedDocument()
                .setPath(path)
                .setContents(contents)
                .setMetadata(metadata);
        } catch (Exception ex) {
            return null;
        }
    }

    // fn load_with_command(path: &str, extension: &str, loader_command: &str) -> Result<LoadedDocument> {
    //    let contents = run_loader_command(path, extension, loader_command)?;
    //    let mut metadata: DocumentMetadata = Default::default();
    //    metadata.insert(EXTENSION_METADATA.into(), DEFAULT_EXTENSION.to_string());
    //    Ok(LoadedDocument::new(path.into(), contents, metadata))
    // }
    private static LoadedDocument loadWithCommand(String path, String extension, String loaderCommand) {
        String contents = runLoaderCommand(path, extension, loaderCommand);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(EXTENSION_METADATA, DEFAULT_EXTENSION);
        return new LoadedDocument()
            .setPath(path)
            .setContents(contents)
            .setMetadata(metadata);
    }

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

    // pub fn load_protocol_path(
    //    loaders: &HashMap<String, String>,
    //    path: &str,
    //) -> Result<Vec<LoadedDocument>> {
    //    let (protocol, loader_command, new_path) = path
    //        .split_once(':')
    //        .and_then(|(protocol, path)| {
    //            let loader_command = loaders.get(protocol)?;
    //            Some((protocol, loader_command, path))
    //        })
    //        .ok_or_else(|| anyhow!("No document loader for '{}'", path))?;
    //    let contents = run_loader_command(new_path, protocol, loader_command)?;
    //    let output = if let Ok(list) = serde_json::from_str::<Vec<LoadedDocument>>(&contents) {
    //        list.into_iter()
    //            .map(|mut v| {
    //                if v.path.starts_with(path) {
    //                } else if v.path.starts_with(new_path) {
    //                    v.path = format!("{}:{}", protocol, v.path);
    //                } else {
    //                    v.path = format!("{}/{}", path, v.path);
    //                }
    //                v
    //            })
    //            .collect()
    //    } else {
    //        vec![LoadedDocument::new(
    //            path.into(),
    //            contents,
    //            Default::default(),
    //        )]
    //    };
    //    Ok(output)
    // }
    public static List<LoadedDocument> loadProtocolPath(Map<String, String> loaders, String path) {
        return null;
    }

    private Loader() { }
}
