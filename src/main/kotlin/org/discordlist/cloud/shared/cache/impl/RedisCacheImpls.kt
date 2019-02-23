/*
 * Shared files of the DLO Project
 *
 * Copyright (C) 2019  Yannick Seeger & Michael Rittmeister
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

@file:Suppress("PlatformExtensionReceiverOfInline")

package org.discordlist.cloud.shared.cache.impl

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.Entity
import com.mewna.catnip.entity.Snowflake
import io.vertx.core.json.JsonObject
import org.discordlist.cloud.shared.cache.GuildSpecificRedisCache
import org.discordlist.cloud.shared.cache.RedisCache
import org.discordlist.cloud.shared.io.redis.RedisSource
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier

/**
 * Default implementation of [RedisCache]
 * @param K the type of the cache keys
 * @param V the type of the cached entities
 * @property identify method that it used to get the key by an entity's instance
 * @property stringify method that is used to stringify keys [K]
 * @property cacheType the type of the cache see [RedisCache]
 * @property clazz the class type of the entities
 * @property pool the [RedisSource]
 * @property builder method that is used to construct entities by json
 * @property serializer method that is used to convert entities into [ByteArray]
 * @see RedisCache
 * @constructor Creates a new Redis cache
 */
@Suppress("unused")
open class RedisCacheImpl<K, V : Entity>(
        override val identify: (entity: V) -> K,
        override val stringify: (identifier: K) -> String,
        override val cacheType: String,
        private val clazz: Class<V>,
        protected val pool: RedisSource,
        protected val builder: (json: JsonObject, cache: RedisCacheImpl<K, V>) -> V,
        protected val serializer: (entity: V) -> ByteArray
) : RedisCache<K, V> {

    protected val executor = Executors.newCachedThreadPool()!!

    constructor(identify: (entity: V) -> K,
                stringify: (identifier: K) -> String,
                cacheType: String,
                clazz: Class<V>,
                pool: RedisSource) : this(identify, stringify, cacheType, clazz, pool, { json, cache -> cache.decodeEntity(json.encode().toByteArray()) }, { it.toJson().encode().toByteArray() })

    private lateinit var catnip: Catnip

    protected val log = LoggerFactory.getLogger(RedisCacheImpl::class.java)!!

    fun V.toByteArray(): ByteArray {
        return serializer(this)
    }

    fun decodeEntity(byteArray: ByteArray): V {
        return Entity.fromJson(catnip(), clazz, JsonObject(String(byteArray)))
    }

    fun entityFromByteArray(byteArray: ByteArray?): V {
        return if (byteArray != null)
            builder(JsonObject(String(byteArray)), this)
        else {
            Entity.fromJson(catnip(), clazz, JsonObject("{\"null\":\"null\"}"))
        }

    }

    protected open fun Jedis.cacheEntity(shardId: Int, entity: V) {
        log.debug("[Cached] Successfully cached $entity with ID ${stringify(identify(entity))}")
        this.hset(formatHashIdentifier(shardId), stringify(identify(entity)).toByteArray(), entity.toByteArray())

    }

    override fun cache(shardId: Int, key: K, entity: V): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use {
                it.cacheEntity(shardId, entity)
            }
        }, executor)
    }

    override fun delete(shardId: Int, entityId: K): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use {
                it.hdel(formatHashIdentifier(shardId), stringify(entityId).toByteArray())
            }
        }, executor)
    }


    override fun bulkCache(shardId: Int, entities: Collection<V>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use { connection ->
                entities.forEach {
                    connection.cacheEntity(shardId, it)
                }
            }
        }, executor)
    }

    private fun getFromRedis(entityId: K): V {
        pool.jedis().use { connection ->
            connection.keys("*-$cacheType").forEach { key ->
                val result = connection.hget(key.toByteArray(), stringify(entityId).toByteArray())
                if (result != null)
                    if (!result.contentEquals("nil".toByteArray()))
                        return entityFromByteArray(result)
            }
        }
        log.error("Cannot find ${clazz.name} by ${stringify(entityId)}")
        return entityFromByteArray(null)
    }

    override fun getAsync(entityId: K): CompletableFuture<V> {
        return CompletableFuture.supplyAsync(Supplier {
            getFromRedis(entityId)
        }, executor)
    }

    override fun getAllAsync(): CompletableFuture<Collection<V>> {
        return CompletableFuture.supplyAsync(Supplier {
            val list = mutableListOf<V>()
            pool.jedis().use { connection ->
                connection.keys("*-$cacheType").forEach { key ->
                    connection.hgetAll(key.toByteArray()).forEach {
                        list.add(entityFromByteArray(it.value))
                    }
                }
            }
            list as Collection<V>
        }, executor)
    }

    override fun invalidate(shardId: Int): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use {
                it.del(formatHashIdentifier(shardId))
            }
        }, executor)
    }

    override fun catnip(catnip: Catnip) {
        this.catnip = catnip
    }

    override fun catnip(): Catnip {
        return catnip
    }
}

/**
 * Default implementation of [RedisCache] that is use for snowflake entities
 * @param T the type of the cached entities
 * @property identify method that it used to get the key by an entity's instance
 * @property stringify method that is used to stringify keys [Long]
 * @property cacheType the type of the cache see [RedisCache.Companion]
 * @property clazz the class type of the entities
 * @property pool the [RedisSource]
 * @property builder method that is used to construct entities by json
 * @property serializer method that is used to convert entities into [ByteArray]
 * @see RedisCacheImpl
 * @see RedisCache
 * @constructor Creates a new Redis cache
 */
class SnowflakeRedisCache<T : Snowflake>(cacheType: String, clazz: Class<T>, pool: RedisSource,
                                         builder: (json: JsonObject, cache: RedisCacheImpl<Long, T>) -> T,
                                         serializer: (entity: T) -> ByteArray
) : RedisCacheImpl<Long, T>({ it.idAsLong() }, { it.toString() }, cacheType, clazz, pool, builder, serializer) {
    constructor(cacheType: String, clazz: Class<T>, pool: RedisSource) : this(cacheType, clazz, pool, { json, cache -> cache.decodeEntity(json.encode().toByteArray()) }, { it.toJson().encode().toByteArray() })
}

/**
 * Default implementation of [GuildSpecificRedisCache]
 * @param K the type of the cache keys
 * @param V the type of the cached entities
 * @property identify method that it used to get the key by an entity's instance
 * @property stringify method that is used to stringify keys [Long]
 * @property cacheType the type of the cache see [RedisCache.Companion]
 * @property clazz the class type of the entities
 * @property pool the [RedisSource]
 * @property stringifyKey method that is used to stringify keys [K]
 * @property identifyGuild method that is used to identify the guild id by an entity's instance
 * @property builder method that is used to construct entities by json
 * @property serializer method that is used to convert entities into [ByteArray]
 * @see GuildSpecificRedisCache
 * @constructor Creates a new Redis cache
 */
@Suppress("PlatformExtensionReceiverOfInline")
open class GuildSpecificRedisCacheImpl<K, V : Entity>(
        identify: (entity: V) -> GuildSpecificRedisCache.GuildInformationContainer<K>,
        stringify: (identifier: GuildSpecificRedisCache.GuildInformationContainer<K>) -> String,
        cacheType: String,
        clazz: Class<V>,
        pool: RedisSource,
        override val stringifyKey: (key: K) -> String,
        override val identifyGuild: (entity: V) -> Long,
        builder: (json: JsonObject, cache: RedisCacheImpl<*, V>) -> V,
        serializer: (entity: V) -> ByteArray
) : RedisCacheImpl<GuildSpecificRedisCache.GuildInformationContainer<K>, V>(identify, stringify, cacheType, clazz, pool, builder, serializer), GuildSpecificRedisCache<K, V> {

    constructor(identify: (entity: V) -> GuildSpecificRedisCache.GuildInformationContainer<K>,
                stringify: (identifier: GuildSpecificRedisCache.GuildInformationContainer<K>) -> String,
                cacheType: String,
                clazz: Class<V>,
                pool: RedisSource,
                stringifyKey: (key: K) -> String,
                identifyGuild: (entity: V) -> Long) : this(identify, stringify, cacheType, clazz, pool, stringifyKey, identifyGuild, { json, cache -> cache.decodeEntity(json.encode().toByteArray()) }, { it.toJson().encode().toByteArray() })

    private fun Jedis.cacheEntity(shardId: Int, guildId: Long, entity: V) {
        val identifier = identify(entity)
        log.debug("[Cached] Successfully cached $entity with ID ${stringifyKey(identifier.entityIdentifier)} on guild ${identifier.guildId}")
        this.hset(formatHashIdentifier(shardId, guildId), stringifyKey(identifier.entityIdentifier).toByteArray(), entity.toByteArray())
    }


    override fun cache(guildId: Long, shardId: Int, entity: V): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use {
                it.cacheEntity(shardId, guildId, entity)
            }
        }, executor)
    }

    override fun bulkCache(guildId: Long, shardId: Int, entities: Collection<V>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use { connection ->
                entities.forEach {
                    connection.cacheEntity(shardId, guildId, it)
                }
            }
        }, executor)
    }

    override fun delete(shardId: Int, guildId: Long, entityId: K): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use {
                it.hdel(formatHashIdentifier(shardId, guildId), stringifyKey(entityId).toByteArray())
            }
        }, executor)
    }

    override fun delete(shardId: Int, guildId: Long): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use {
                it.del("-$guildId-$cacheType")
            }
        }, executor)
    }

    override fun getAllAsync(guildId: Long): CompletableFuture<Collection<V>> {
        return CompletableFuture.supplyAsync(Supplier {
            val list = mutableListOf<V>()
            pool.jedis().use { connection ->
                connection.keys("*-$guildId-$cacheType".toByteArray()).forEach { key ->
                    connection.hgetAll(key).forEach {
                        list.add(entityFromByteArray(it.value))
                    }
                }
            }
            list as Collection<V>
        }, executor)

    }

    override fun invalidate(shardId: Int): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            pool.jedis().use { jedis ->
                jedis.keys("$shardId-*-$cacheType").forEach {
                    jedis.del(it)
                }
            }
        }, executor)
    }

    private fun getFromRedis(entityId: GuildSpecificRedisCache.GuildInformationContainer<K>): V {
        pool.jedis().use { connection ->
            connection.keys("*-${entityId.guildId}-$cacheType").forEach { key ->
                val result = connection.hget(key.toByteArray(), stringifyKey(entityId.entityIdentifier).toByteArray())
                if (result != null)
                    if (!result.contentEquals("nil".toByteArray()))
                        return entityFromByteArray(result)
            }
        }
        return entityFromByteArray("nil".toByteArray())
    }

    override fun get(entityId: GuildSpecificRedisCache.GuildInformationContainer<K>): V {
        return getFromRedis(entityId)
    }

    override fun cache(shardId: Int, entity: V): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            cache(identifyGuild(entity), shardId, entity)
        }, executor)
    }

    override fun bulkCache(shardId: Int, entities: Collection<V>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync(Supplier {
            if (!entities.isEmpty())
                bulkCache(identifyGuild(entities.first()), shardId, entities)
        }, executor)
    }

}

/**
 * Default implementation of [GuildSpecificRedisCache] for snowflake entities
 * @param T the type of the cached entities
 * @property identifyGuild method that is used to identify the guild id by an entity's instance
 * @property cacheType the type of the cache see [RedisCache.Companion]
 * @property clazz the class type of the entities
 * @property pool the [RedisSource]
 * @property stringifyKey method that is used to stringify keys [Long]
 * @property builder method that is used to construct entities by json
 * @property serializer method that is used to convert entities into [ByteArray]
 * @see GuildSpecificRedisCacheImpl
 * @see GuildSpecificRedisCache
 * @constructor Creates a new Redis cache
 */
class GuildSpecificSnowflakeRedisCache<T : Snowflake>(
        identifyGuild: (entity: T) -> Long,
        cacheType: String,
        clazz: Class<T>,
        pool: RedisSource,
        stringifyKey: (key: Long) -> String,
        builder: (json: JsonObject, cache: RedisCacheImpl<*, T>) -> T,
        serializer: (entity: T) -> ByteArray
) : GuildSpecificRedisCacheImpl<Long, T>({ GuildSpecificRedisCache.GuildInformationContainer(identifyGuild(it), it.idAsLong()) }, { stringifyKey(it.entityIdentifier) }, cacheType, clazz, pool, stringifyKey, identifyGuild, builder, serializer) {
    constructor(identifyGuild: (entity: T) -> Long,
                cacheType: String,
                clazz: Class<T>,
                pool: RedisSource,
                stringifyKey: (key: Long) -> String) : this(identifyGuild, cacheType, clazz, pool, stringifyKey, { json, cache -> cache.decodeEntity(json.encode().toByteArray()) }, { it.toJson().encode().toByteArray() })
}
