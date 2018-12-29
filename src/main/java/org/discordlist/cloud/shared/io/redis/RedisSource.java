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

package org.discordlist.cloud.shared.io.redis;

import redis.clients.jedis.Jedis;

import java.util.function.Consumer;

public class RedisSource {

    private final String host;
    private final String password;
    private Jedis jedis;

    public RedisSource(String host, String password) {
        this.host = host;
        this.password = password;
    }

    public void connect(Consumer<RedisSource> onSuccess) {
        jedis = new Jedis(host);
        jedis.auth(password);
        onSuccess.accept(this);
    }

    public Jedis getJedis() {
        return jedis;
    }
}
