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

package net.creationreborn.launcher.integration.microsoft.entity.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class XboxRequest {

    @JsonProperty("Properties")
    private Properties properties;

    @JsonProperty("RelyingParty")
    private String relyingParty;

    @JsonProperty("TokenType")
    private String tokenType;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getRelyingParty() {
        return relyingParty;
    }

    public void setRelyingParty(String relyingParty) {
        this.relyingParty = relyingParty;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Properties {

        @JsonProperty("AuthMethod")
        private String authMethod;

        @JsonProperty("RpsTicket")
        private String rpsTicket;

        @JsonProperty("SandboxId")
        private String sandboxId;

        @JsonProperty("SiteName")
        private String siteName;

        @JsonProperty("UserTokens")
        private List<String> userTokens;

        public String getAuthMethod() {
            return authMethod;
        }

        public void setAuthMethod(String authMethod) {
            this.authMethod = authMethod;
        }

        public String getRpsTicket() {
            return rpsTicket;
        }

        public void setRpsTicket(String rpsTicket) {
            this.rpsTicket = rpsTicket;
        }

        public String getSandboxId() {
            return sandboxId;
        }

        public void setSandboxId(String sandboxId) {
            this.sandboxId = sandboxId;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public List<String> getUserTokens() {
            return userTokens;
        }

        public void setUserTokens(List<String> userTokens) {
            this.userTokens = userTokens;
        }
    }
}