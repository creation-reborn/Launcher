/*
 * Copyright 2021 creationreborn.net
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

package net.creationreborn.launcher.integration.microsoft;

import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import net.creationreborn.launcher.auth.Account;
import net.creationreborn.launcher.integration.microsoft.entity.request.MicrosoftRequest;
import net.creationreborn.launcher.integration.microsoft.entity.request.MinecraftRequest;
import net.creationreborn.launcher.integration.microsoft.entity.request.XboxRequest;
import net.creationreborn.launcher.integration.microsoft.entity.response.MicrosoftResponse;
import net.creationreborn.launcher.integration.microsoft.entity.response.MinecraftResponse;
import net.creationreborn.launcher.integration.microsoft.entity.response.XboxResponse;
import net.creationreborn.launcher.integration.mojang.yggdrasil.Profile;
import net.creationreborn.launcher.util.Progress;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

public class MicrosoftIntegration {

    private static final String CLIENT_ID = "1aa0808c-9cb9-4ed3-ac56-1f83788d4d46";
    private static final String REDIRECT_URI = "http://127.0.0.1";
    private static final Server SERVER = new Server();

    public static void login(Account account, Progress progress) {
        if (SERVER.isAlive()) {
            return;
        }

        try {
            SERVER.start();
            if (!authorize()) {
                throw new IllegalStateException("Browse action not supported");
            }

            Server.Result result = SERVER.await();
            if (result == null) {
                throw new NullPointerException("Missing Result");
            }

            if (result.getError() != null || result.getErrorDescription() != null) {
                SwingHelper.showErrorDialog(null, result.getErrorDescription(), result.getError());
                return;
            }

            // Microsoft
            progress.setStatus(SharedLocale.tr("login.status.microsoft"));
            MicrosoftResponse microsoftResponse = getMicrosoftToken(result.getCode());
            if (microsoftResponse == null || microsoftResponse.getAccessToken() == null) {
                SwingHelper.showErrorDialog(null, SharedLocale.tr("login.status.microsoft.error"), SharedLocale.tr("errorTitle"));
                return;
            }

            account.getUser().setUsername(microsoftResponse.getUserId());

            // Xbox
            progress.setStatus(SharedLocale.tr("login.status.xbox"));
            XboxResponse xboxToken = getXboxToken(microsoftResponse.getAccessToken());
            if (xboxToken == null || xboxToken.getToken() == null || xboxToken.getUHS() == null) {
                SwingHelper.showErrorDialog(null, SharedLocale.tr("login.status.xbox.error"), SharedLocale.tr("errorTitle"));
                return;
            }

            // XSTS
            progress.setStatus(SharedLocale.tr("login.status.xsts"));
            XboxResponse xstsToken = getXSTSToken(xboxToken.getToken());
            if (xstsToken == null || xstsToken.getToken() == null || xstsToken.getUHS() == null) {
                SwingHelper.showErrorDialog(null, SharedLocale.tr("login.status.xsts.error"), SharedLocale.tr("errorTitle"));
                return;
            }

            // Minecraft
            progress.setStatus(SharedLocale.tr("login.status.minecraft"));
            MinecraftResponse minecraftResponse = getMinecraftToken(xstsToken.getUHS(), xstsToken.getToken());
            if (minecraftResponse == null || minecraftResponse.getAccessToken() == null) {
                SwingHelper.showErrorDialog(null, SharedLocale.tr("login.status.minecraft.error"), SharedLocale.tr("errorTitle"));
                return;
            }

            // Minecraft Profile
            progress.setStatus(SharedLocale.tr("login.status.profiles"));
            Profile profile = getProfile(minecraftResponse.getAccessToken());
            if (profile == null || profile.getId() == null || profile.getName() == null) {
                SwingHelper.showErrorDialog(null, SharedLocale.tr("login.status.profiles.error"), SharedLocale.tr("errorTitle"));
                return;
            }

            account.getProfiles().add(profile);
            account.setCurrentProfile(profile);
            account.setAccessToken(minecraftResponse.getAccessToken());
        } catch (Exception ex) {
            SwingHelper.showErrorDialog(null, SharedLocale.tr("errors.genericError"), SharedLocale.tr("errorTitle"), ex);
        } finally {
            SERVER.stop();
        }
    }

    public static boolean authorize() throws Exception {
        URL url = HttpRequest.url("https://login.live.com/oauth20_authorize.srf"
                + "?client_id=" + CLIENT_ID
                + "&response_type=code"
                + "&scope=XboxLive.signin%20offline_access"
                + "&redirect_uri=" + getRedirectUri());

        return canBrowse() && browse(url.toURI());
    }

    public static MicrosoftResponse getMicrosoftToken(String authorizationCode) throws Exception {
        MicrosoftRequest request = new MicrosoftRequest();
        request.setClientId(CLIENT_ID);
        request.setCode(authorizationCode);
        request.setGrantType("authorization_code");

        return HttpRequest
                .post(HttpRequest.url("https://login.live.com/oauth20_token.srf"))
                .header("Accept", "application/json")
                .bodyForm(HttpRequest.Form.form()
                        .add("client_id", CLIENT_ID)
                        .add("code", authorizationCode)
                        .add("grant_type", "authorization_code")
                        .add("redirect_uri", getRedirectUri()))
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(MicrosoftResponse.class);
    }

    public static XboxResponse getXboxToken(String rspTicket) throws Exception {
        XboxRequest.Properties properties = new XboxRequest.Properties();
        properties.setAuthMethod("RPS");
        properties.setRpsTicket("d=" + rspTicket);
        properties.setSiteName("user.auth.xboxlive.com");

        XboxRequest request = new XboxRequest();
        request.setProperties(properties);
        request.setTokenType("JWT");
        request.setRelyingParty("http://auth.xboxlive.com");

        return HttpRequest
                .post(HttpRequest.url("https://user.auth.xboxlive.com/user/authenticate"))
                .header("Accept", "application/json")
                .bodyJson(request)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(XboxResponse.class);
    }

    public static XboxResponse getXSTSToken(String xboxToken) throws Exception {
        XboxRequest.Properties properties = new XboxRequest.Properties();
        properties.setSandboxId("RETAIL");
        properties.setUserTokens(Collections.singletonList(xboxToken));

        XboxRequest request = new XboxRequest();
        request.setProperties(properties);
        request.setTokenType("JWT");
        request.setRelyingParty("rp://api.minecraftservices.com/");

        return HttpRequest
                .post(HttpRequest.url("https://xsts.auth.xboxlive.com/xsts/authorize"))
                .header("Accept", "application/json")
                .bodyJson(request)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(XboxResponse.class);
    }

    public static MinecraftResponse getMinecraftToken(String uhsToken, String xstsToken) throws Exception {
        MinecraftRequest request = new MinecraftRequest();
        request.setIdentityToken("XBL3.0 x=" + uhsToken + ";" + xstsToken);

        return HttpRequest
                .post(HttpRequest.url("https://api.minecraftservices.com/authentication/login_with_xbox"))
                .header("Accept", "application/json")
                .bodyJson(request)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(MinecraftResponse.class);
    }

    public static Profile getProfile(String token) throws Exception {
        return HttpRequest
                .get(HttpRequest.url("https://api.minecraftservices.com/minecraft/profile"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .execute()
                .expectResponseCode(200, 404)
                .returnContent()
                .asJson(Profile.class);
    }

    public static boolean canBrowse() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    public static boolean browse(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static String getRedirectUri() {
        return REDIRECT_URI + ":" + SERVER.getListeningPort();
    }
}