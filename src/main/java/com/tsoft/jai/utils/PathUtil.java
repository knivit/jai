package com.tsoft.jai.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public final class PathUtil {

    // pub fn to_absolute_path(path: &str) -> Result<String> {
    //    Ok(Path::new(&path).absolutize()?.display().to_string())
    // }
    public static String toAbsolutePath(String path) {
        return toAbsolutePath(path, (e) -> { });
    }

    public static String toAbsolutePath(String path, Consumer<String> withContext) {
        try {
            return Path.of(path).toAbsolutePath().toString();
        } catch (Exception ex) {
            if (withContext != null) {
                withContext.accept(path);
            }
            return null;
        }
    }

    // pub fn resolve_home_dir(path: &str) -> String {
    //    let mut path = path.to_string();
    //    if path.starts_with("~/") || path.starts_with("~\\") {
    //        if let Some(home_dir) = dirs::home_dir() {
    //            path.replace_range(..1, &home_dir.display().to_string());
    //        }
    //    }
    //    path
    // }
    public static String resolveHomeDir(String path) {
        if (path != null) {
            if (path.startsWith("~/") || path.startsWith("~\\")) {
                String homeDir = System.getProperty("user.home");
                if (!isBlank(homeDir)) {
                    return homeDir + path.substring(1);
                }
            }
        }
        return path;
    }

    // pub async fn expand_glob_paths<T: AsRef<str>>(
    //    paths: &[T],
    //    bail_non_exist: bool,
    //) -> Result<IndexSet<String>> {
    //    let mut new_paths = IndexSet::new();
    //    for path in paths {
    //        let (path_str, suffixes, current_only) = parse_glob(path.as_ref())?;
    //        list_files(
    //            &mut new_paths,
    //            Path::new(&path_str),
    //            suffixes.as_ref(),
    //            current_only,
    //            bail_non_exist,
    //        )
    //        .await?;
    //    }
    //    Ok(new_paths)
    // }
    public static Set<String> expandGlobPaths(Collection<String> paths, boolean bailNonExist) {
        Set<String> newPaths = new LinkedHashSet<>();
        if (!isEmpty(paths)) {
            for (String path : paths) {
                Triple<String, List<String>, Boolean> triple = parseGlob(path);
                String pathStr = triple.first();
                List<String> suffixes = triple.second();
                Boolean currentOnly = triple.third();
                listFiles(newPaths, Path.of(pathStr), suffixes, currentOnly, bailNonExist);
            }
        }
        return newPaths;
    }

    // fn parse_glob(path_str: &str) -> Result<(String, Option<Vec<String>>, bool)> {
    //    let glob_result =
    //        if let Some(start) = path_str.find("/**/*.").or_else(|| path_str.find(r"\**\*.")) {
    //            Some((start, 6, false))
    //        } else if let Some(start) = path_str.find("**/*.").or_else(|| path_str.find(r"**\*.")) {
    //            if start == 0 {
    //                Some((start, 5, false))
    //            } else {
    //                None
    //            }
    //        } else if let Some(start) = path_str.find("/*.").or_else(|| path_str.find(r"\*.")) {
    //            Some((start, 3, true))
    //        } else if let Some(start) = path_str.find("*.") {
    //            if start == 0 {
    //                Some((start, 2, true))
    //            } else {
    //                None
    //            }
    //        } else {
    //            None
    //        };
    //    if let Some((start, offset, current_only)) = glob_result {
    //        let mut base_path = path_str[..start].to_string();
    //        if base_path.is_empty() {
    //            base_path = if path_str
    //                .chars()
    //                .next()
    //                .map(|v| v == '/')
    //                .unwrap_or_default()
    //            {
    //                "/"
    //            } else {
    //                "."
    //            }
    //            .into();
    //        }
    //
    //        let extensions = if let Some(curly_brace_end) = path_str[start..].find('}') {
    //            let end = start + curly_brace_end;
    //            let extensions_str = &path_str[start + offset..end + 1];
    //            if extensions_str.starts_with('{') && extensions_str.ends_with('}') {
    //                extensions_str[1..extensions_str.len() - 1]
    //                    .split(',')
    //                    .map(|s| s.to_string())
    //                    .collect::<Vec<String>>()
    //            } else {
    //                bail!("Invalid path '{path_str}'");
    //            }
    //        } else {
    //            let extensions_str = &path_str[start + offset..];
    //            vec![extensions_str.to_string()]
    //        };
    //        let extensions = if extensions.is_empty() {
    //            None
    //        } else {
    //            Some(extensions)
    //        };
    //        Ok((base_path, extensions, current_only))
    //    } else if path_str.ends_with("/**") || path_str.ends_with(r"\**") {
    //        Ok((path_str[0..path_str.len() - 3].to_string(), None, false))
    //    } else {
    //        Ok((path_str.to_string(), None, false))
    //    }
    // }
    public static Triple<String, List<String>, Boolean> parseGlob(String path) {
        return null;
    }

    // #[async_recursion::async_recursion]
    //async fn list_files(
    //    files: &mut IndexSet<String>,
    //    entry_path: &Path,
    //    suffixes: Option<&Vec<String>>,
    //    current_only: bool,
    //    bail_non_exist: bool,
    //) -> Result<()> {
    //    if !entry_path.exists() {
    //        if bail_non_exist {
    //            bail!("Not found '{}'", entry_path.display());
    //        } else {
    //            return Ok(());
    //        }
    //    }
    //    if entry_path.is_dir() {
    //        let mut reader = tokio::fs::read_dir(entry_path).await?;
    //        while let Some(entry) = reader.next_entry().await? {
    //            let path = entry.path();
    //            if path.is_dir() {
    //                if !current_only {
    //                    list_files(files, &path, suffixes, current_only, bail_non_exist).await?;
    //                }
    //            } else {
    //                add_file(files, suffixes, &path);
    //            }
    //        }
    //    } else {
    //        add_file(files, suffixes, entry_path);
    //    }
    //    Ok(())
    // }
    private static void listFiles(Set<String> newPaths, Path of, List<String> suffixes, Boolean currentOnly, boolean bailNonExist) {

    }

    // pub fn get_patch_extension(path: &str) -> Option<String> {
    //    Path::new(&path)
    //        .extension()
    //        .map(|v| v.to_string_lossy().to_lowercase())
    // }
    public static String getPatchExtension(String path) {
        if (path == null) {
            return null;
        }
        int n = path.lastIndexOf('.');
        if (n != -1) {
            return path.substring(n + 1).toLowerCase();
        }
        return null;
    }

    private PathUtil() { }
}
