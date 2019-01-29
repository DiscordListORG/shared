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
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.discordlist.cloud.shared.util.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper class for connection to RabbitMQ
 */
public class RabbitMQSource {

    private final Vertx vertx;
    private final List<String> hosts;
    private final String username;
    private final String password;
    private RabbitMQOptions rabbitMQOptions;
    private RabbitMQClient rabbitMQClient;

    /**
     * Constructor for RabbitMQ source
     * @param vertx The Vertx instance that should be used
     * @param hosts The list of hosts that should be connected to
     * @param username The user name which should be used for authentication
     * @param password The user name which should be used for authentication
     */
    public RabbitMQSource(Vertx vertx, List<String> hosts, String username, String password) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(hosts);
        Lists.notEmpty(hosts);
        this.vertx = vertx;
        this.hosts = hosts;
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor for RabbitMQ source without authentication
     * @param vertx The Vertx instance that should be used
     * @param hosts The list of hosts that should be connected to
     * @see RabbitMQSource#RabbitMQSource(Vertx, List, String, String)
     */
    public RabbitMQSource(Vertx vertx, List<String> hosts) {
        this(vertx, hosts, null, null);
    }

    /**
     * Constructor for RabbitMQ source without authentication and localhost as hostname
     * @param vertx The Vertx instance that should be used
     * @see RabbitMQSource#RabbitMQSource(Vertx, List)
     */
    public RabbitMQSource(Vertx vertx) {
        this(vertx, Collections.singletonList("localhost"));
    }

    /**
     * Constructor for RabbitMQ source without authentication and localhost as hostname and default Vert.x instance {@link Vertx#vertx()}
     * @see RabbitMQSource#RabbitMQSource(Vertx)
     */
    public RabbitMQSource() {
        this(Vertx.vertx());
    }

    /**
     * Connects to the RabbitMQ server.
     * @param  resultHandler Handler for results
     * @see RabbitMQClient#create(Vertx, RabbitMQOptions)
     */
    public RabbitMQClient connect(Handler<AsyncResult<Void>> resultHandler) {
        RabbitMQOptions options = rabbitMQOptions != null ? new RabbitMQOptions(rabbitMQOptions) : new RabbitMQOptions();
        options.setAddresses(Arrays.asList(Address.parseAddresses(String.join(",", hosts.toArray(new String[]{})))));
        if (username != null)
            options.setUser(username);
        if (password != null)
            options.setPassword(password);
        this.rabbitMQClient = RabbitMQClient.create(vertx, options);
        this.rabbitMQClient.start(resultHandler);
        return rabbitMQClient;
    }

    /**
     * Provide custom {@link RabbitMQOptions}.
     *
     * @param rabbitMQOptions the {@link RabbitMQOptions} object
     * @return this for fluent use
     */
    public RabbitMQSource rabbitMQOptions(RabbitMQOptions rabbitMQOptions) {
        Objects.requireNonNull(rabbitMQOptions);
        this.rabbitMQOptions = rabbitMQOptions;
        return this;
    }

    /**
     * @return this for fluent use
     */
    public RabbitMQClient client() {
        return rabbitMQClient;
    }
}