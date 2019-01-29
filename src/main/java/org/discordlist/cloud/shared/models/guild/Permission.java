/*
 * Shared files of the DLO Project
 *
 * Copyright (C) 2018  Yannick Seeger & Michael Rittmeister
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package org.discordlist.cloud.shared.models.guild;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.discordlist.cloud.shared.models.JsonSerializable;

import java.io.IOException;
import java.util.Objects;

/**
 * Permission object class
 */
public class Permission extends JsonSerializable {

    private PermissionGroup group;
    private String node;
    @JsonProperty("default")
    private boolean isDefault;

    /**
     * Constructor for permissions
     *
     * @param group     The PermissionGroup {@link org.discordlist.cloud.shared.models.guild.PermissionGroup}
     * @param node      The name of the node
     * @param isDefault Whether the permission is default or not
     */
    public Permission(PermissionGroup group, String node, boolean isDefault) {
        this.group = group;
        this.node = node;
        this.isDefault = isDefault;
    }

    /**
     * Used for deserialization
     */
    public Permission() {
    }

    /**
     * @return The permission group
     */
    public PermissionGroup getGroup() {
        return group;
    }

    /**
     * @return The name of the node
     */
    public String getNode() {
        return node;
    }

    /**
     * @return Whether the permission is default or not
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Parsing method for json-ified Permission object
     *
     * @param jsonData The Json data
     * @return The parsed object
     * @throws IOException See more information here {@link ObjectMapper#readValue(String, Class)}
     */
    public static Permission fromJson(String jsonData) throws IOException {
        return new ObjectMapper().readValue(jsonData, Permission.class);
    }

    /**
     * Override for better capability
     *
     * @param o The other object
     * @return Whether the objects are identical
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return isDefault == that.isDefault &&
                group == that.group &&
                Objects.equals(node, that.node);
    }

    /**
     * Override for better capability
     *
     * @return The objects hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(group, node, isDefault);
    }

    /**
     * Override for better capability
     *
     * @return The stringified objects
     */
    @Override
    public String toString() {
        return "Permission{" +
                "group=" + group +
                ", node='" + node + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
