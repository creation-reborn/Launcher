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

package net.creationreborn.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.skcraft.launcher.install.InstallLog;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.install.UpdateCache;
import net.creationreborn.launcher.install.FileDeleter;
import net.creationreborn.launcher.install.InstallLogZipExtract;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ZipFileInstall extends FileInstall {

    @JsonProperty
    @JsonDeserialize(as = HashMap.class, contentAs = String.class, keyAs = String.class)
    private Map<String, String> extracts = Maps.newHashMap();

    @Override
    public void install(Installer installer, InstallLog log, UpdateCache cache, File contentDir) throws Exception {
        if (getWhen() != null && !getWhen().matches()) {
            return;
        }

        File targetFile = new File(contentDir, getDestination());
        URL url = new URL(getUrl());

        Preconditions.checkState(getSize() > 0, "Invalid Size");
        File tempFile = installer.getDownloader().download(url, getDestination(), getSize(), getDestination());
        installer.queue(new InstallLogZipExtract(log, tempFile, targetFile, name -> {
            if (getExtracts().isEmpty()) {
                return name;
            }

            for (Map.Entry<String, String> entry : getExtracts().entrySet()) {
                if (name.matches(entry.getKey())) {
                    return name.replaceAll(entry.getKey(), entry.getValue());
                }
            }

            return null;
        }));

        installer.queue(new FileDeleter(tempFile));
    }

    public Map<String, String> getExtracts() {
        return extracts;
    }
}