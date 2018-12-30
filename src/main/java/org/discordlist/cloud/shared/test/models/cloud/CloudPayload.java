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

package org.discordlist.cloud.shared.test.models.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.discordlist.cloud.shared.test.JsonSerializable;

import java.io.IOException;
import java.util.Objects;

public class CloudPayload extends JsonSerializable {

    private String event;
    private JsonNode data;
    private long sentAt;

    public CloudPayload(String event, JsonNode data, long sentAt) {
        this.event = event;
        this.data = data;
        this.sentAt = sentAt;
    }

    public CloudPayload() {
    }

    public String getEvent() {
        return event;
    }

    public JsonNode getData() {
        return data;
    }

    public long getSentAt() {
        return sentAt;
    }

    public static CloudPayload fromJson(String jsonData) throws IOException {
        return new ObjectMapper().readValue(jsonData, CloudPayload.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudPayload that = (CloudPayload) o;
        return sentAt == that.sentAt &&
                Objects.equals(event, that.event) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, data, sentAt);
    }

    @Override
    public String toString() {
        return "CloudPayload{" +
                "event='" + event + '\'' +
                ", data='" + data + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
