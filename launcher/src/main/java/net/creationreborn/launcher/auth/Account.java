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

package net.creationreborn.launcher.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Sets;
import net.creationreborn.launcher.integration.mojang.yggdrasil.Profile;
import net.creationreborn.launcher.integration.mojang.yggdrasil.User;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE
)
public class Account implements Comparable<Account> {

    @JsonProperty
    private String accessToken;

    @JsonProperty
    private String activeProfile;

    @JsonProperty
    private String clientToken;

    @JsonProperty
    @JsonDeserialize(as = HashSet.class, contentAs = Profile.class)
    private Set<Profile> profiles = Sets.newHashSet();

    @JsonProperty
    private User user;

    @JsonProperty
    private AccountType type;

    public Optional<Profile> getCurrentProfile() {
        if (StringUtils.isBlank(activeProfile)) {
            return Optional.empty();
        }

        for (Profile profile : getProfiles()) {
            if (activeProfile.equals(profile.getId())) {
                return Optional.of(profile);
            }
        }

        return Optional.empty();
    }

    public boolean setCurrentProfile(Profile profile) {
        if (profiles.contains(profile)) {
            activeProfile = profile.getId();
            return true;
        }

        return false;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    @Override
    public int compareTo(Account o) {
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser().getUsername());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Account account = (Account) obj;
        return getUser().getUsername() != null && getUser().getUsername().equalsIgnoreCase(account.getUser().getUsername());
    }

    @Override
    public String toString() {
        return getUser().getUsername();
    }
}