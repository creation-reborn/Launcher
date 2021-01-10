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

package net.creationreborn.launcher.install;

import com.google.common.io.Files;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.install.InstallLog;
import com.skcraft.launcher.install.InstallTask;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.skcraft.launcher.util.SharedLocale.tr;

public class InstallLogZipExtract implements InstallTask {

    private static final Logger log = Logger.getLogger(InstallLogZipExtract.class.getName());
    private final InstallLog installLog;
    private final File from;
    private final File to;
    private final Function<String, String> function;

    public InstallLogZipExtract(InstallLog installLog, File from, File to) {
        this(installLog, from, to, name -> name);
    }

    public InstallLogZipExtract(InstallLog installLog, File from, File to, Function<String, String> function) {
        this.installLog = installLog;
        this.from = from;
        this.to = to;
        this.function = function;
    }

    @Override
    public void execute(Launcher launcher) throws IOException {
        InstallLogZipExtract.log.log(Level.INFO, "Extracting to {0} (from {1})...", new Object[]{to.getAbsoluteFile(), from.getName()});
        try (ZipInputStream inputStream = new ZipInputStream(Files.asByteSource(from).openBufferedStream())) {
            to.getParentFile().mkdirs();
            ZipEntry entry;

            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String name = function.apply(entry.getName());
                if (name == null) {
                    continue;
                }

                File file = new File(to, name);
                if (file.exists() && !file.canWrite()) {
                    file.setWritable(true);
                }

                file.getParentFile().mkdirs();
                file.createNewFile();
                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                    IOUtils.copy(inputStream, outputStream);
                }

                installLog.add(file, file);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("installer.extractingFiles", from, to);
    }
}