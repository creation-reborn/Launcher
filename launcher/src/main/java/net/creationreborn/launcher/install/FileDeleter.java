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

package net.creationreborn.launcher.install;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.install.InstallTask;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.skcraft.launcher.util.SharedLocale.tr;

public class FileDeleter implements InstallTask {

    private static final Logger log = Logger.getLogger(FileDeleter.class.getName());
    private final File from;

    public FileDeleter(File from) {
        this.from = from;
    }

    @Override
    public void execute(Launcher launcher) throws Exception {
        FileDeleter.log.log(Level.INFO, "Deleting {0}...", new Object[]{from.getAbsoluteFile()});
        from.delete();
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("installer.deletingFile", from);
    }
}