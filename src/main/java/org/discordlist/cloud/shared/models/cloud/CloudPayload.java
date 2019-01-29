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

package org.discordlist.cloud.shared.models.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.discordlist.cloud.shared.models.JsonSerializable;

import java.io.IOException;

/**
 * Data class for payload packets send by Gateway
 */
public class CloudPayload extends JsonSerializable {

    private String event;
    private byte[] data;
    private long sentAt;

    /**
     * Constructor for payload
     *
     * @param event  The name of the Event. See events here {@link com.mewna.catnip.shard.DiscordEvent.Raw}
     * @param data   The data of the event
     * @param sentAt The timestamp of the event
     * @see com.mewna.catnip.shard.DiscordEvent.Raw
     */
    public CloudPayload(String event, byte[] data, long sentAt) {
        this.event = event;
        this.data = data;
        this.sentAt = sentAt;
    }

    /**
     * #NO ARGS CONSTRUCTOR
     */
    public CloudPayload() {
    }

    /**
     * @return The event
     */
    public String getEvent() {
        return event;
    }

    /**
     * @return The data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return The timestamp
     */
    public long getSentAt() {
        return sentAt;
    }

    /**
     * Parsing method for json-ified CloudPayload
     *
     * @param jsonData The Json data
     * @return The parsed object
     * @throws IOException See more information here {@link ObjectMapper#readValue(String, Class)}
     */
    public static CloudPayload fromJson(String jsonData) throws IOException {
        return new ObjectMapper().readValue(jsonData, CloudPayload.class);
    }

    /**
     * Override of toString() method for better formatting
     *
     * @return String-ified payload
     */
    @Override
    public String toString() {
        return "CloudPayload{" +
                "event='" + event + '\'' +
                ", data=" + new String(data) +
                ", sentAt=" + sentAt +
                '}';
    }
}
