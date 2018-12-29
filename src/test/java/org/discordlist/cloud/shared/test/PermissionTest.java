/*
 * Gateway - The gateway service between the Discord API and the discordlist.org nodes.
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

package org.discordlist.cloud.shared.test;

import org.discordlist.cloud.shared.test.models.guild.Permission;
import org.discordlist.cloud.shared.test.models.guild.PermissionGroup;
import org.junit.Test;

import java.io.IOException;


public class PermissionTest {

    @Test
    public void testPermissionConverting() throws IOException {

        Permission permission = new Permission(PermissionGroup.SERVER_HEAD, "test", false);
        // Serialization
        String jsonData = permission.toJson();
        System.out.println(jsonData);

        // Deserialization
        System.out.println(Permission.fromJson(jsonData));
    }
}
