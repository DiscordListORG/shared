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

import com.rabbitmq.client.Address;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.discordlist.cloud.shared.util.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RabbitMQSource {

    private final  List<String> hosts;
    private final String username;
    private final String password;
    private RabbitMQOptions rabbitMQOptions;
    private RabbitMQClient rabbitMQClient;

    public RabbitMQSource(Vertx vertx, List<String> hosts, String username, String password) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(hosts);
        Lists.notEmpty(hosts);
        this.hosts = hosts;
        this.username = username;
        this.password = password;
    }

    public RabbitMQSource(Vertx vertx, List<String> host) {
        this(vertx, host, null, null);
    }

    public RabbitMQSource(Vertx vertx) {
        this(vertx, Collections.singletonList("localhost"));
    }

    public RabbitMQSource() {
        this(Vertx.vertx());
    }

    /**
     * Connects to the RabbitMQ server.
     * @return this for fluent use
     */
    public RabbitMQSource connect() {
        RabbitMQOptions options = rabbitMQOptions != null ? new RabbitMQOptions(rabbitMQOptions) : new RabbitMQOptions();
        options.setAddresses(Arrays.asList(Address.parseAddresses(String.join(",", hosts.toArray(new String[]{})))));
        if (username != null)
            options.setUser(username);
        if (password != null)
            options.setPassword(password);
        return this;
    }

    /**
     * Provide custom {@link RabbitMQOptions}.
     * @param rabbitMQOptions the {@link RabbitMQOptions} object
     * @return this for fluent use
     */
    public RabbitMQSource rabbitMQOptions(RabbitMQOptions rabbitMQOptions) {
        Objects.requireNonNull(rabbitMQOptions);
        this.rabbitMQOptions = rabbitMQOptions;
        return this;
    }

    /**
     *
     * @return this for fluent use
     */
    public RabbitMQClient client() {
        return rabbitMQClient;
    }
}