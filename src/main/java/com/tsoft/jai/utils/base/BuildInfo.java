package com.tsoft.jai.utils.base;

import lombok.extern.slf4j.Slf4j;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Slf4j
public class BuildInfo {

    private static final String PROJECT_KEY = "project";
    private static final String VERSION_KEY = "version";

    private static final TupleN buildInfo = getBuildInfo();

    public static String getProject() {
        return buildInfo.get(PROJECT_KEY);
    }

    public static String getVersion() {
        return buildInfo.get(VERSION_KEY);
    }

    private static TupleN getBuildInfo() {
        String implTitle  = BuildInfo.class.getPackage().getImplementationTitle();
        String implVers   = BuildInfo.class.getPackage().getImplementationVersion();

        /* If the manifest is not yet present (e.g. inside IDE) fall back
           to reading the JAR file that contains this class */
        if (implTitle == null || implVers == null) {
            var codeSource = BuildInfo.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                try (JarFile jar = new JarFile(codeSource.getLocation().getPath())) {
                    Manifest mf = jar.getManifest();
                    Attributes attrs = mf.getMainAttributes();
                    implTitle = attrs.getValue("Implementation-Title");
                    implVers  = attrs.getValue("Implementation-Version");
                } catch (Exception ex) {
                    log.warn("Can't find out JAR's title and version");
                }
            }
        }

        return TupleN.asMap(
            PROJECT_KEY, (implTitle == null ? "unknown" : implTitle),
            VERSION_KEY, (implVers  == null ? "unknown" : implVers));
    }
}
