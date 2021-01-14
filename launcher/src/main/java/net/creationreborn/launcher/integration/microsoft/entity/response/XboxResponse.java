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

package net.creationreborn.launcher.integration.microsoft.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class XboxResponse {

    @JsonProperty("DisplayClaims")
    private DisplayClaims displayClaims;

    @JsonProperty("XErr")
    private long errorCode;

    @JsonProperty("Identity")
    private String identity;

    @JsonProperty("IssueInstant")
    private String issueInstant;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("NotAfter")
    private String notAfter;

    @JsonProperty("Redirect")
    private String redirect;

    @JsonProperty("Token")
    private String token;

    public String getUhs() {
        if (getDisplayClaims() == null) {
            return null;
        }

        List<DisplayClaims.XUI> xuis = getDisplayClaims().getXuis();
        if (xuis == null || xuis.size() == 0) {
            return null;
        }

        return xuis.get(0).getUhs();
    }

    public DisplayClaims getDisplayClaims() {
        return displayClaims;
    }

    public long getErrorCode() {
        return errorCode;
    }

    public String getIdentity() {
        return identity;
    }

    public String getIssueInstant() {
        return issueInstant;
    }

    public String getMessage() {
        return message;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public String getRedirect() {
        return redirect;
    }

    public String getToken() {
        return token;
    }

    public static class DisplayClaims {

        @JsonProperty("xui")
        private List<XUI> xuis;

        public List<XUI> getXuis() {
            return xuis;
        }

        public static class XUI {

            @JsonProperty("uhs")
            private String uhs;

            public String getUhs() {
                return uhs;
            }
        }
    }
}