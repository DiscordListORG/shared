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

import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.Objects;

public class RedisSource {

    private final Vertx vertx;
    private final String host;
    private final String password;
    private RedisOptions redisOptions;
    private RedisClient redisClient;

    /**
     * Creates a new {@link RedisSource} instance.
     * @param vertx the {@link Vertx} instance
     * @param host the Redis host
     * @param password the Redis password
     */
    public RedisSource(Vertx vertx, String host, String password) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(host);
        this.vertx = vertx;
        this.host = host;
        this.password = password;
    }

    /**
     * @see RedisSource#RedisSource(Vertx, String, String)
     * @param vertx the {@link Vertx} instance
     * @param host the redis host
     */
    public RedisSource(Vertx vertx, String host) {
        this(vertx, host, null);
    }

    /**
     * @see RedisSource#RedisSource(Vertx, String, String)
     * @param vertx the {@link Vertx} instance
     */
    public RedisSource(Vertx vertx) {
        this(vertx, "localhost");
    }

    /**
     * @see RedisSource#RedisSource(Vertx, String, String)
     */
    public RedisSource() {
        this(Vertx.vertx());
    }

    /**
     * Provide custom {@link RedisOptions}.
     * @param redisOptions the {@link RedisOptions} object
     * @return this for fluent use
     */
    public RedisSource redisOptions(RedisOptions redisOptions) {
        Objects.requireNonNull(redisOptions);
        this.redisOptions = redisOptions;
        return this;
    }

    /**
     * Connects to the Redis server.
     * @return this for fluent use
     */
    public RedisSource connect() {
        RedisOptions options = this.redisOptions != null ? new RedisOptions(redisOptions) : new RedisOptions();
        if (password != null)
            options.setAuth(password);
        options.setHost(host);
        redisClient = RedisClient.create(vertx, options);
        return this;
    }

    /**
     * Returns the {@link RedisClient} instance.
     * @return the {@link RedisClient} instance
     */
    public RedisClient client() {
        return redisClient;
    }
}
