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

package net.creationreborn.launcher.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import net.creationreborn.launcher.auth.Account;
import net.creationreborn.launcher.dialog.LoginDialog;
import net.creationreborn.launcher.dialog.ProfileSelectionDialog;
import net.creationreborn.launcher.integration.mojang.yggdrasil.User;
import org.apache.commons.lang3.RegExUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Toolbox {

    public static final String USER_AGENT = String.format("CRLauncher/@version@ (%s; +https://creationreborn.net/)", System.getProperty("os.name"));
    private static final Logger LOGGER = Logger.getLogger(Toolbox.class.getName());

    /**
     * Removes non-printable characters (excluding new line and carriage return) in the provided {@link java.lang.String String}.
     *
     * @param string The {@link java.lang.String String} to filter.
     * @return The filtered {@link java.lang.String String}.
     */
    public static String filter(String string) {
        return RegExUtils.replaceAll(string, "[^\\x20-\\x7E\\x0A\\x0D]", "");
    }

    public static <T> T first(List<T> list) {
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    public static String join(List<? extends CharSequence> list) {
        if (list != null) {
            return String.join("\n", list);
        }

        return null;
    }

    public static Session getSession(Window window, Launcher launcher) {
        if (launcher.getAccounts().getSize() > 0) {
            ProfileSelectionDialog profileSelectionDialog = new ProfileSelectionDialog(window, launcher);
            profileSelectionDialog.setVisible(true);
            if (profileSelectionDialog.isCancelled() || profileSelectionDialog.getSession() != null) {
                return profileSelectionDialog.getSession();
            }
        }

        LoginDialog loginDialog = new LoginDialog(window, launcher);
        launcher.getAccounts().getCurrentAccount()
                .map(Account::getUser)
                .map(User::getUsername)
                .ifPresent(loginDialog::setUsername);

        loginDialog.setVisible(true);
        return loginDialog.getSession();
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

    @SuppressWarnings("UnstableApiUsage")
    public static <V> void addCallback(ListenableFuture<V> future, Consumer<V> success, Consumer<Throwable> failure, Executor executor) {
        Futures.addCallback(future, new FutureCallback<V>() {
            @Override
            public void onSuccess(@Nullable V result) {
                success.accept(result);
            }

            @Override
            public void onFailure(Throwable t) {
                failure.accept(t);
            }
        }, executor);
    }
}