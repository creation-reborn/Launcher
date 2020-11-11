/*
 * Copyright 2020 creationreborn.net
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

package net.creationreborn.launcher.builder.integration.creeperhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.model.modpack.ManifestEntry;
import com.skcraft.launcher.util.HttpRequest;
import net.creationreborn.launcher.builder.integration.creeperhost.entity.Modpack;
import net.creationreborn.launcher.builder.integration.creeperhost.entity.Version;
import net.creationreborn.launcher.entity.Analytics;
import net.creationreborn.launcher.model.modpack.FileInstall;
import net.creationreborn.launcher.util.Toolbox;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static net.creationreborn.launcher.builder.Builder.LOGGER;

public class CreeperHostIntegration {

    private static final String API_HOST = "api.modpacks.ch";
    private static final String BASE_URL = "https://" + API_HOST + "/public/modpack/";
    private static final String FORGE_URL = "https://files.minecraftforge.net/maven";
    private Modpack modpack;
    private Version version;

    public void prepare(File directory, ObjectMapper mapper, String source) throws Exception {
        LOGGER.info("Prepare CreeperHost");

        URL versionURL = filterURL(source);
        LOGGER.info("Downloading Version Manifest " + versionURL.toExternalForm());
        version = downloadManifest(versionURL, mapper, Version.class);

        URL modpackURL = new URL(BASE_URL + version.getParent());
        LOGGER.info("Downloading Modpack Manifest " + modpackURL.toExternalForm());
        modpack = downloadManifest(modpackURL, mapper, Modpack.class);

        LOGGER.info(modpack.getName() + " v" + version.getName());
    }

    public void downloadLoaders(File directory) throws Exception {
        LOGGER.info("Downloading Loaders");
        String minecraftVersion = getMinecraftVersion();
        if (StringUtils.isBlank(minecraftVersion)) {
            throw new IllegalStateException("Missing Minecraft Version");
        }

        for (Version.Target target : version.getTargets()) {
            if (!StringUtils.equals(target.getType(), "modloader")) {
                continue;
            }

            File file;
            URL url;
            if (StringUtils.equals(target.getName(), "forge")) {
                file = new File(directory, String.format(
                        "forge-%s-%s-installer.jar",
                        minecraftVersion,
                        target.getVersion()
                ));

                url = HttpRequest.url(String.format(
                        "%s/net/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar",
                        FORGE_URL,
                        minecraftVersion,
                        target.getVersion(),
                        minecraftVersion,
                        target.getVersion()
                ));
            } else {
                throw new UnsupportedOperationException(target.getName() + " loader is not supported");
            }

            LOGGER.info("Downloading " + target.getName() + " -> " + file.getAbsolutePath());
            HttpRequest
                    .get(url)
                    .execute()
                    .expectResponseCode(200)
                    .saveContent(file);
        }
    }

    public Analytics getAnalytics() {
        Analytics analytics = new Analytics();
        analytics.setInstallUrl(BASE_URL + modpack.getId() + "/" + version.getId() + "/install");
        analytics.setPlayUrl(BASE_URL + modpack.getId() + "/" + version.getId() + "/play");
        return analytics;
    }

    public List<ManifestEntry> getEntries() {
        List<ManifestEntry> entries = Lists.newArrayList();
        for (Version.File file : version.getFiles()) {
            if (file.getSize() <= 0) {
                LOGGER.warning(String.format("Ignoring %s as it has an invalid file size", file.getName()));
                continue;
            }

            FileInstall fileInstall = new FileInstall();
            fileInstall.setDestination(file.getPath() + file.getName());
            fileInstall.setSize(file.getSize());
            fileInstall.setUrl(file.getUrl());
            entries.add(fileInstall);
        }

        return entries;
    }

    public String getName() {
        return modpack.getName();
    }

    public String getVersion() {
        return version.getName();
    }

    private String getMinecraftVersion() {
        for (Version.Target target : version.getTargets()) {
            if (target.getType().equals("game") && target.getName().equals("minecraft")) {
                return target.getVersion();
            }
        }

        return null;
    }

    /**
     * https://api.modpacks.ch/public/modpack/[packID]
     * https://api.modpacks.ch/public/modpack/[packID]/[versionID]
     */
    private URL filterURL(String source) throws MalformedURLException, URISyntaxException {
        URI uri = new URI(source);
        if (uri.getHost().equals(API_HOST)) {
            if (uri.getPath().startsWith("/public/modpack/")) {
                String path = uri.getPath();
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                URL filteredUrl = new URI(
                        "https",
                        API_HOST,
                        path,
                        uri.getQuery(),
                        uri.getFragment()
                ).toURL();

                String filteredSource = filteredUrl.toExternalForm();
                if (!source.equals(filteredSource)) {
                    LOGGER.info("Filtered " + source + " -> " + filteredSource);
                }

                return filteredUrl;
            }
        }

        return uri.toURL();
    }

    private <T> T downloadManifest(URL url, ObjectMapper mapper, Class<? extends T> type) throws IOException {
        HttpURLConnection httpURLConnection = createHttpURLConnection(url);
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Did not get expected response code, got " + responseCode + " for " + url);
        }

        return mapper.readValue(httpURLConnection.getInputStream(), type);
    }

    private HttpURLConnection createHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("User-Agent", Toolbox.USER_AGENT);
        return connection;
    }
}