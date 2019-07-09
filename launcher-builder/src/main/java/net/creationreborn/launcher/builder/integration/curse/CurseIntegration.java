/*
 * Copyright 2019 creationreborn.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.creationreborn.launcher.builder.integration.curse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.skcraft.launcher.model.modpack.ManifestEntry;
import com.skcraft.launcher.util.HttpRequest;
import net.creationreborn.launcher.builder.integration.curse.manifest.AddonFile;
import net.creationreborn.launcher.builder.integration.curse.manifest.Manifest;
import net.creationreborn.launcher.builder.integration.curse.manifest.ModLoader;
import net.creationreborn.launcher.builder.integration.curse.meta.Metadata;
import net.creationreborn.launcher.install.ZipExtract;
import net.creationreborn.launcher.model.modpack.FileInstall;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static net.creationreborn.launcher.builder.Builder.LOGGER;

public class CurseIntegration {

    // https://twitchappapi.docs.apiary.io/
    private static final String BASE_URL = "https://addons-ecs.forgesvc.net/api/v2";
    private static final String FORGE_URL = "https://files.minecraftforge.net/maven";
    private Manifest manifest;

    public void prepare(File directory, ObjectMapper mapper, String source) throws Exception {
        URL url = filterURL(source);

        LOGGER.info("Prepare Curse: " + url.toExternalForm());
        File packFile = new File(directory, "pack.zip");

        LOGGER.info("Downloading to " + packFile.getAbsolutePath());
        HttpRequest.get(url).execute().expectResponseCode(200).saveContent(packFile);

        LOGGER.info("Extracting " + packFile.getAbsolutePath() + " -> " + directory.getAbsolutePath());
        ZipExtract zipExtract = new ZipExtract(Files.asByteSource(packFile), directory, name -> {
            if (name.equals("manifest.json")) {
                return name;
            }

            return null;
        });

        zipExtract.run();
        packFile.delete();

        File manifestFile = new File(directory, "manifest.json");
        manifest = mapper.readValue(manifestFile, Manifest.class);
        manifestFile.delete();
    }

    public void downloadLoaders(File directory) throws Exception {
        LOGGER.info("Downloading Loaders");
        for (ModLoader modLoader : manifest.getMinecraft().getModLoaders()) {
            String type = StringUtils.substringBefore(modLoader.getId(), "-");
            String version = StringUtils.substringAfter(modLoader.getId(), "-");

            File file;
            URL url;
            if (type.equalsIgnoreCase("Forge")) {
                file = new File(directory, String.format(
                        "forge-%s-%s-installer.jar",
                        manifest.getMinecraft().getVersion(),
                        version
                ));

                url = HttpRequest.url(String.format(
                        "%s/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar",
                        FORGE_URL,
                        manifest.getMinecraft().getVersion(),
                        version,
                        manifest.getMinecraft().getVersion(),
                        version
                ));
            } else {
                throw new UnsupportedOperationException(type + " loader is not supported");
            }

            LOGGER.info("Downloading " + modLoader.getId() + " -> " + file.getAbsolutePath());
            HttpRequest
                    .get(url)
                    .execute()
                    .expectResponseCode(200)
                    .saveContent(file);
        }
    }

    public List<ManifestEntry> getEntries() throws Exception {
        List<ManifestEntry> entries = Lists.newArrayList();
        for (AddonFile addonFile : manifest.getFiles()) {
            ManifestEntry entry = resolveAddon(addonFile.getProjectID(), addonFile.getFileID());
            entries.add(entry);
        }

        return entries;
    }

    public String getName() {
        return manifest.getName();
    }

    public String getVersion() {
        return manifest.getVersion();
    }

    private FileInstall resolveAddon(int projectId, int fileId) throws InterruptedException, IOException {
        Metadata metadata = HttpRequest
                .get(HttpRequest.url(String.format("%s/addon/%s/file/%s", BASE_URL, projectId, fileId)))
                .execute()
                .expectResponseCode(200)
                .returnContent().asJson(Metadata.class);

        LOGGER.info("Resolved Addon: " + metadata.getFileName() + " (" + projectId + "/" + fileId + ")");
        FileInstall fileInstall = new FileInstall();
        fileInstall.setDestination(String.format("mods/%s", metadata.getFileName()));
        fileInstall.setSize(metadata.getFileLength());
        fileInstall.setUrl(metadata.getDownloadUrl());
        return fileInstall;
    }

    /**
     * https://www.curseforge.com/minecraft/mc-mods/[projectID]/files/[fileID]
     * https://www.curseforge.com/minecraft/mc-mods/[projectID]/download/[fileID]
     * https://www.curseforge.com/minecraft/mc-mods/[projectID]/download/[fileID]/files
     */
    private static URL filterURL(String url) throws MalformedURLException, URISyntaxException {
        URI uri = new URI(url);
        if (uri.getHost().endsWith("curseforge.com")) {
            if (uri.getPath().startsWith("/minecraft/modpacks/") && !uri.getPath().endsWith("/file")) {
                return new URI(
                        uri.getScheme(),
                        uri.getAuthority(),
                        uri.getPath().replace("files", "download") + "/file",
                        null,
                        uri.getFragment()
                ).toURL();
            }
        }

        return uri.toURL();
    }
}