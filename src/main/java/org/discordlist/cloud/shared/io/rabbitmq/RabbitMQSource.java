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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQSource {

    private final String host;
    private final String username;
    private final String password;
    private final boolean isDev;
    private Connection connection;
    private Channel channel;

    public RabbitMQSource(String host, String username, String password, boolean isDev) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.isDev = isDev;
    }

    public RabbitMQSource(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.isDev = false;
    }

    public void connect(Consumer<RabbitMQSource> onSuccess) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        this.connection = connectionFactory.newConnection();
        this.channel = connection.createChannel();
        onSuccess.accept(this);
    }

    public String get(RabbitKey key) {
        return isDev ? String.format("dev.%s", key.key) : key.key;
    }

    public boolean isConnected() {
        return connection.isOpen() && channel.isOpen();
    }

    public Connection getConnection() {
        return connection;
    }

    public Channel getChannel() {
        return channel;
    }
}
