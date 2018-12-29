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

package org.discordlist.cloud.shared.test;

import org.discordlist.cloud.shared.test.models.cloud.CloudPayload;
import org.junit.Test;

import java.io.IOException;

public class CloudPayloadTest {

    @Test
    public void testCloudPayloadConverting() throws IOException {

        CloudPayload cloudPayload = new CloudPayload("TEST", "{\"aad\": \"badadawd\"}", 1337L);
        // Serialization
        String jsonData = cloudPayload.toJson();
        System.out.println(jsonData);

        // Deserialization
        System.out.println(CloudPayload.fromJson(jsonData));
    }
}
