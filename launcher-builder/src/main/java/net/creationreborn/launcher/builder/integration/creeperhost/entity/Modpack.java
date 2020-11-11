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

package net.creationreborn.launcher.builder.integration.creeperhost.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Modpack {

    @JsonProperty
    private String synopsis;

    @JsonProperty
    private String description;

    @JsonProperty
    private List<Art> art;

    @JsonProperty
    private List<Object> links;

    @JsonProperty
    private List<Author> authors;

    @JsonProperty
    private List<Version> versions;

    @JsonProperty
    private long installs;

    @JsonProperty
    private long plays;

    @JsonProperty
    private boolean featured;

    @JsonProperty
    private long refreshed;

    @JsonProperty
    private String notification;

    @JsonProperty
    private Rating rating;

    @JsonProperty
    private String status;

    @JsonProperty
    private long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String type;

    @JsonProperty
    private long updated;

    @JsonProperty
    private List<Object> tags;

    public String getSynopsis() {
        return synopsis;
    }

    public String getDescription() {
        return description;
    }

    public List<Art> getArt() {
        return art;
    }

    public List<Object> getLinks() {
        return links;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public long getInstalls() {
        return installs;
    }

    public long getPlays() {
        return plays;
    }

    public boolean isFeatured() {
        return featured;
    }

    public long getRefreshed() {
        return refreshed;
    }

    public String getNotification() {
        return notification;
    }

    public Rating getRating() {
        return rating;
    }

    public String getStatus() {
        return status;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getUpdated() {
        return updated;
    }

    public List<Object> getTags() {
        return tags;
    }

    public static class Art {

        @JsonProperty
        private int width;

        @JsonProperty
        private int height;

        @JsonProperty
        private boolean compressed;

        @JsonProperty
        private String url;

        @JsonProperty
        private String sha1;

        @JsonProperty
        private long size;

        @JsonProperty
        private long id;

        @JsonProperty
        private String type;

        @JsonProperty
        private long updated;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getUrl() {
            return url;
        }

        public String getSha1() {
            return sha1;
        }

        public long getSize() {
            return size;
        }

        public long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public long getUpdated() {
            return updated;
        }
    }

    public static class Author {

        @JsonProperty
        private String website;

        @JsonProperty
        private long id;

        @JsonProperty
        private String name;

        @JsonProperty
        private String type;

        @JsonProperty
        private long updated;

        public String getWebsite() {
            return website;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public long getUpdated() {
            return updated;
        }
    }

    public static class Rating {

        @JsonProperty
        private long id;

        @JsonProperty
        private boolean configured;

        @JsonProperty
        private boolean verified;

        @JsonProperty
        private int age;

        @JsonProperty
        private boolean gambling;

        @JsonProperty
        private boolean frightening;

        @JsonProperty
        private boolean alcoholdrugs;

        @JsonProperty
        private boolean nuditysexual;

        @JsonProperty
        private boolean sterotypeshate;

        @JsonProperty
        private boolean languageviolence;

        public long getId() {
            return id;
        }

        public boolean isConfigured() {
            return configured;
        }

        public boolean isVerified() {
            return verified;
        }

        public int getAge() {
            return age;
        }

        public boolean isGambling() {
            return gambling;
        }

        public boolean isFrightening() {
            return frightening;
        }

        public boolean isAlcoholdrugs() {
            return alcoholdrugs;
        }

        public boolean isNuditysexual() {
            return nuditysexual;
        }

        public boolean isSterotypeshate() {
            return sterotypeshate;
        }

        public boolean isLanguageviolence() {
            return languageviolence;
        }
    }

    public static class Version {

        @JsonProperty
        private Specs specs;

        @JsonProperty
        private long id;

        @JsonProperty
        private String name;

        @JsonProperty
        private String type;

        @JsonProperty
        private long updated;

        public Specs getSpecs() {
            return specs;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public long getUpdated() {
            return updated;
        }
    }
}