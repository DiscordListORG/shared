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

package org.discordlist.cloud.shared.io.rabbitmq;

/**
 * Wrapper class for RabbitMQ keys
 */
public enum RabbitKey {
    QUEUE_DISCORD_EVENT_DISPATCH("discord_event_dispatch"),
    EXCHANGE_SYSTEM_DISPATCH("system_dispatch");

    public final String key;

    /**
     * Constructor for RabbitKey
     *
     * @param key the key of the RabbitMQ queue
     */
    RabbitKey(String key) {
        this.key = key;
    }

    /**
     * Formats a RabbitKey
     *
     * @param instanceName The current instance's name
     * @param key          The key of the queue
     * @return The formatted key
     */
    public static String get(String instanceName, String key) {
        return String.format("dlo.%s.%s", instanceName, key);
    }

    /**
     * Getter for event dispatcher queue
     *
     * @param instanceName The current instance's name
     * @return The formatted key
     * @see RabbitKey#QUEUE_DISCORD_EVENT_DISPATCH
     * @see RabbitKey#get(String, String)
     */
    public static String getEventDispatchQueue(String instanceName) {
        return get(instanceName, QUEUE_DISCORD_EVENT_DISPATCH.key);
    }

    /**
     * Getter for dispatcher dispatcher queue
     *
     * @param instanceName The current instance's name
     * @return The formatted key
     * @see RabbitKey#EXCHANGE_SYSTEM_DISPATCH
     * @see RabbitKey#get(String, String)
     */
    public static String getSystemDispatch(String instanceName) {
        return get(instanceName, EXCHANGE_SYSTEM_DISPATCH.key);
    }
}
