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

package net.creationreborn.launcher.integration.mojang.yggdrasil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(
        value = {
                "email",
                "registerIp",
                "migratedFrom",
                "migratedAt",
                "registeredAt",
                "passwordChangedAt",
                "dateOfBirth",
                "suspended",
                "blocked",
                "secured",
                "migrated",
                "emailVerified",
                "legacyUser",
                "emailSubscriptionStatus",
                "emailSubscriptionKey",
                "properties",
                "verifiedByParent",
                "migrationId",
                "hashed",
                "fromMigratedUser"
        },
        ignoreUnknown = true,
        allowSetters = true
)
public class User {

    private String id;
    private String email;
    private String username;
    private String registerIp;
    private String migratedFrom;
    private long migratedAt;
    private long registeredAt;
    private long passwordChangedAt;
    private long dateOfBirth;
    private boolean suspended;
    private boolean blocked;
    private boolean secured;
    private boolean migrated;
    private boolean emailVerified;
    private boolean legacyUser;
    private String emailSubscriptionStatus;
    private String emailSubscriptionKey;
    private boolean verifiedByParent;
    private String migrationId;
    private boolean hashed;
    private boolean fromMigratedUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        User user = (User) obj;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}