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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.creationreborn.launcher.integration.mojang.yggdrasil.Profile;
import net.creationreborn.launcher.integration.mojang.yggdrasil.User;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE
)
public class AccountList {

    @JsonProperty
    @JsonDeserialize(as = HashSet.class, contentAs = Account.class)
    private Set<Account> accounts = Sets.newHashSet();

    @JsonProperty
    private String activeAccount;

    public synchronized void add(Account account) {
        remove(account);
        accounts.add(account);
    }

    public synchronized void remove(Account account) {
        for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext(); ) {
            Account other = iterator.next();
            if (account.equals(other)) {
                iterator.remove();
                break;
            }
        }
    }

    public Account create(AccountType type) {
        Account account = new Account();
        account.setClientToken(StringUtils.remove(UUID.randomUUID().toString(), '-'));
        account.setUser(new User());
        account.setType(type);
        return account;
    }

    public Account getOrCreate(String username, AccountType type) {
        for (Account account : accounts) {
            if (StringUtils.isNotBlank(username) && username.equalsIgnoreCase(account.getUser().getUsername())) {
                return account;
            }
        }

        Account account = create(type);
        account.getUser().setUsername(username);
        return account;
    }

    public Optional<Account> getAccount(Profile profile) {
        for (Account account : accounts) {
            if (account.getProfiles().contains(profile)) {
                return Optional.of(account);
            }
        }

        return Optional.empty();
    }

    public List<Profile> getAllProfiles() {
        List<Profile> profiles = Lists.newArrayList();
        for (Account account : accounts) {
            profiles.addAll(account.getProfiles());
        }

        return profiles;
    }

    public Optional<Account> getCurrentAccount() {
        if (StringUtils.isBlank(activeAccount)) {
            return Optional.empty();
        }

        for (Account account : accounts) {
            if (activeAccount.equals(account.getUser().getUsername())) {
                return Optional.of(account);
            }
        }

        return Optional.empty();
    }

    public boolean setCurrentAccount(Account account) {
        if (accounts.contains(account)) {
            activeAccount = account.getUser().getUsername();
            return true;
        }

        return false;
    }

    public int getSize() {
        return accounts.size();
    }
}