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

package net.creationreborn.launcher.bootstrap.util;

import com.skcraft.launcher.Bootstrap;
import com.skcraft.launcher.bootstrap.SharedLocale;
import com.skcraft.launcher.bootstrap.SwingHelper;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.logging.Logger;

public class Toolbox {

    public static final String USER_AGENT = String.format("CRLauncher/@version@ (%s; +https://creationreborn.net/)", System.getProperty("os.name"));
    private static final Logger LOGGER = Logger.getLogger(Toolbox.class.getName());
    public static final String CHANNEL = getChannel();

    public static boolean containsIgnoreCase(String string, String searchString) {
        return string.toLowerCase().contains(searchString.toLowerCase());
    }

    public static boolean startsWithIgnoreCase(String string, String searchString) {
        return string.toLowerCase().startsWith(searchString.toLowerCase());
    }

    public static boolean deleteDirectory(Path path) {
        try {
            Files.walk(path)
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void detectLegacy() {
        Path path = getPath("LolnetData");
        if (Files.exists(path)) {
            LOGGER.info("Detected legacy launcher: " + path.toAbsolutePath().toString());
            boolean result = SwingHelper.confirmDialog(
                    null,
                    SharedLocale.tr("legacy.message"),
                    SharedLocale.tr("legacy.title"));

            if (result) {
                if (deleteDirectory(path)) {
                    LOGGER.info("Successfully deleted legacy launcher");
                } else {
                    LOGGER.warning("Failed to delete legacy launcher");
                }
            }
        }
    }

    public static File getBinariesDirectory(File baseDir) {
        if (isDevelopmentChannel()) {
            return new File(baseDir, "launcher-development");
        }

        return new File(baseDir, "launcher");
    }

    public static Path getPath(String name) {
        String osName = System.getProperty("os.name");
        String userHome = System.getProperty("user.home");

        if (osName != null && userHome != null) {
            if (containsIgnoreCase(osName, "BSD") || containsIgnoreCase(osName, "Linux") || containsIgnoreCase(osName, "Unix")) {
                return Paths.get(userHome, name);
            }

            if (startsWithIgnoreCase(osName, "Mac OS")) {
                return Paths.get(userHome, "Library", "Application Support", name);
            }

            if (startsWithIgnoreCase(osName, "Windows")) {
                return Paths.get(userHome, "AppData", "Roaming", name);
            }
        }

        return Paths.get(name);
    }

    public static boolean isDevelopmentChannel() {
        return CHANNEL.equals("development");
    }

    public static void setAppName(String name) {
        try {
            String currentDesktop = System.getenv("XDG_CURRENT_DESKTOP");
            if (currentDesktop == null || !currentDesktop.equalsIgnoreCase("GNOME")) {
                return;
            }

            Toolkit toolkit = Toolkit.getDefaultToolkit();

            Field field = toolkit.getClass().getDeclaredField("awtAppClassName");
            field.setAccessible(true);
            field.set(toolkit, name);
        } catch (Exception ex) {
            LOGGER.warning("Failed to set awtAppClassName");
        }
    }

    private static String getChannel() {
        String channel = Bootstrap.class.getPackage().getSpecificationTitle();
        if (channel != null) {
            return channel;
        }

        return "release";
    }
}