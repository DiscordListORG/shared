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
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.function.Consumer;

public class RedisSource {

    private final String host;
    private final String password;
    private JedisPool jedis;

    public RedisSource(String host, String password) {
        this.host = host;
        this.password = password;
    }

    public void connect(Consumer<RedisSource> onSuccess) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(15);
        config.setMaxIdle(15);
        jedis = new JedisPool(host);
        onSuccess.accept(this);
    }

    /**
     * <strong>NOT RECOMMENDED</strong>
     * Use getJedis() method instead
     * @see RedisSource#getJedis()
     * @return The JedisPool
     */
    @Deprecated
    public JedisPool getJedisPool() {
        return jedis;
    }

    /**
     * Get a new Redis instance from pool and authenticates the connection
     * @return The Jedis connection
     */
    public Jedis getJedis() {
        Jedis resource = jedis.getResource();
        resource.auth(password);
        return resource;
    }
}
