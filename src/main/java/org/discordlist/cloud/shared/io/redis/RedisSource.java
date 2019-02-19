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

import java.util.Objects;
import java.util.function.Consumer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Wrapper class for Redis connections
 */
public class RedisSource {

  private final String host;
  private final String password;
  private JedisPool jedis;

  /**
   * Constructor for RedisSource
   *
   * @param host The host of the redis instance
   * @param password The password for authentication
   */
  public RedisSource(String host, String password) {
    Objects.requireNonNull(host);
    this.host = host;
    this.password = password;
  }

  /**
   * Constructor for RedisSource without authentication
   *
   * @param host The host of the redis instance
   * @see this#RedisSource(String, String)
   */
  public RedisSource(String host) {
    this(host, null);
  }

  /**
   * Constructor for RedisSource without authentication and localhost as host
   *
   * @see this#RedisSource(String)
   */
  public RedisSource() {
    this("localhost");
  }

  /**
   * Connects to Redis server
   *
   * @param onSuccess Handler that is called after connection was successfully
   * @return The current RedisSource instance
   */
  public RedisSource connect(Consumer<JedisPool> onSuccess) {
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(15);
    config.setMaxIdle(15);
    jedis = new JedisPool(config, host);
    // Force connect to print errors
    jedis();
    onSuccess.accept(jedis);
    return this;
  }

  /**
   * Fetches a new {@link Jedis} connection from the {@link JedisPool}.
   *
   * @return the {@link Jedis} connection
   */
  public Jedis jedis() {
    Jedis res = jedis.getResource();
    if (password != null) {
      res.auth(password);
    }
    return res;
  }

  /**
   * Get a new Redis instance from pool and authenticates the connection
   *
   * @return The Jedis connection
   * @deprecated Use {@link RedisSource#jedis()} instead
   */
  @Deprecated
  public Jedis getJedis() {
    return jedis();
  }
}
