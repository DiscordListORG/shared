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

package org.discordlist.cloud.shared.cache

import com.mewna.catnip.cache.view.CacheView
import com.mewna.catnip.cache.view.DefaultCacheView
import com.mewna.catnip.cache.view.DefaultNamedCacheView
import com.mewna.catnip.cache.view.NamedCacheView
import com.mewna.catnip.entity.Entity
import com.mewna.catnip.entity.RequiresCatnip

/**
 * Generic Redis cache for all type of [Entity]
 * @param K the type of the cache keys
 * @param V the type of the cache
 * @property cacheType the type of the Redis cache see [RedisCache.Companion]
 * @property identify the method that is used to identify entities
 * @property stringify the method that is used to stringify the identifier
 */
interface RedisCache<K, V : Entity> : RequiresCatnip {

    /**
     * Names of all hashes
     */
    companion object {
        const val ROLES = "ROLES"
        const val GUILDS = "GUILDS"
        const val DM_CHANNELS = "DM_CHANNELS"
        const val MEMBERS = "MEMBERS"
        const val USERS = "USERS"
        const val CHANNELS = "CHANNELS"
        const val EMOJIS = "EMOJIS"
        const val VOICE_STATES = "VOICE_STATES"
    }

    /**
     * Method that is used to identify entities
     */
    val identify: (entity: V) -> K

    /**
     * Method that is used to stringify the identifier
     */
    val stringify: (identifier: K) -> String

    /**
     * The type of the Redis cache see [RedisCache.Companion]
     */
    val cacheType: String

    /**
     * Method that formats the identifier for the Redis hash correctly
     * @param shardId the id of the shard the needed cache is for
     * @return the formatted identifier
     */
    fun formatHashIdentifier(shardId: Int): ByteArray {
        return "$shardId-$cacheType".toByteArray()
    }

    /**
     * Method that caches an entity
     * @param shardId the id of the shard the entity is on
     * @param key the key of the entity
     * @param entity the entity itself
     */
    fun cache(shardId: Int, key: K, entity: V)

    /**
     * Method that caches an entity by using the [identify] method
     * @param shardId The id of the shard the entity is on
     * @param entity The entity itself
     */
    fun cache(shardId: Int, entity: V) {
        cache(shardId, identify(entity), entity)
    }

    /**
     * Method that caches an entity
     * @param shardId the id of the shard the entity is on
     * @param key the key of the entity
     * @param entity the entity itself
     */
    operator fun set(shardId: Int, key: K, entity: V) {
        cache(shardId, key, entity)
    }

    /**
     * Method that caches an entity
     * @param shardId the id of the shard the entity is on
     * @param entity the entity itself
     */
    operator fun set(shardId: Int, entity: V) {
        cache(shardId, entity)
    }

    /**
     * Method that caches an entity
     * @param shardId The id of the shard the entity is on
     * @param entities The entities itself
     */
    fun bulkCache(shardId: Int, entities: Collection<V>)

    /**
     * Method that caches an entity
     * @param shardId the id of the shard the entity is on
     * @param entities the entity itself
     */
    operator fun set(shardId: Int, entities: Collection<V>) {
        bulkCache(shardId, entities)
    }

    /**
     * Gets an entity from cache
     * @param entityId The entities id
     * @return The entity
     */
    fun get(entityId: K): V

    /**
     * Method that deletes an entity from cache
     * @param shardId the shard the entity is on
     * @param entityId the id of the entity
     */
    fun delete(shardId: Int, entityId: K)

    /**
     * Returns the whole cache
     * @return all cached entities in a [List]
     */
    fun getAll(): Collection<V>

    /**
     * Returns the whole cache
     * @see DefaultCacheView
     * @see CacheView
     * @return all cached entities in a [CacheView]
     */
    fun asCacheView(): CacheView<V> {
        return getAll().asCacheView()
    }

    /**
     * Returns the whole cache
     * @param nameFunction function then returns the name by the entity
     * @see NamedCacheView
     * @see DefaultNamedCacheView
     * @return all cached entities in a [NamedCacheView]
     */
    fun asNamedCacheView(nameFunction: (entity: V) -> String): NamedCacheView<V> {
        return getAll().asNamedCacheView(nameFunction)
    }

    /**
     * Extension for [Collection] that is used vor [asCacheView]
     * @return all cached entities in a [CacheView]
     */
    fun Collection<V>.asCacheView(): CacheView<V> {
        return fillCacheView(DefaultCacheView(), this)
    }

    /**
     * Extension for [Collection] that is used vor [asNamedCacheView]
     * @param nameFunction function then returns the name by the entity
     * @return all cached entities in a [NamedCacheView]
     */
    fun Collection<V>.asNamedCacheView(nameFunction: (entity: V) -> String): NamedCacheView<V> {
        return fillCacheView(DefaultNamedCacheView<V>(nameFunction), this) as NamedCacheView<V>
    }

    private fun fillCacheView(cacheView: DefaultCacheView<V>, collection: Collection<V>): CacheView<V> {
        collection.forEach { cacheView.put(it.signifiedIdentifier().toLong(), it) }
        return cacheView
    }

    private fun V.signifiedIdentifier(): String {
        return stringify(identify(this))
    }

    /**
     * Invalidates all entities of one shard
     */
    fun invalidate(shardId: Int)

    /**
     * Method that is invoked when the entity got updated at Discord
     * @param shardId the id of the shard the entity is on
     * @param identifier the iddentifier of the entity
     * @param entity the entity
     */
    fun update(shardId: Int, identifier: K, entity: V) {
        cache(shardId, identifier, entity)
    }

}

/**
 * Extension of [RedisCache] for guild specific entities
 * @param K the type of the cache keys
 * @param V the type of the cache
 * @property stringifyKey method that is used to stringify keys
 * @property identifyGuild method that is used to get the guild id by an entity's instance
 * @see RedisCache
 */
interface GuildSpecificRedisCache<K, V : Entity> : RedisCache<GuildSpecificRedisCache.GuildInformationContainer<K>, V> {

    /**
     * Method that is used to stringify keys
     */
    val stringifyKey: (key: K) -> String

    /**
     * Method that is used to get the guild id by an entity's instance
     */
    val identifyGuild: (entity: V) -> Long

    /**
     * Method that formats the identifier for the Redis hash correctly
     * @param shardId the id of the shard the needed cache is for
     * @return the formatted identifier
     */
    fun formatHashIdentifier(shardId: Int, guildId: Long): ByteArray {
        return "$shardId-$guildId-$cacheType".toByteArray()
    }

    /**
     * Methods that caches an entity
     * @param guildId the id of the Guild the entity is on
     * @param shardId the id of the Shard the entity is on
     * @param entity the entity itself
     */
    fun cache(guildId: Long, shardId: Int, entity: V)

    /**
     * Method that uses [identifyGuild] to invoke [cache]
     * @param shardId the id of the Shard the entity is on
     * @param entity the entity itself
     * @see GuildSpecificRedisCache.cache
     */
    override fun cache(shardId: Int, entity: V) {
        cache(identifyGuild(entity), shardId, entity)
    }

    /**
     * Methods that caches an entity
     * @param guildId the id of the Guild the entity is on
     * @param shardId the id of the Shard the entity is on
     * @param entities the [Collection] of entities
     */
    fun bulkCache(guildId: Long, shardId: Int, entities: Collection<V>)

    /**
     * Method that uses [identifyGuild] to invoke [bulkCache]
     * @param shardId the id of the Shard the entity is on
     * @param entities the [Collection] of entities
     * @see GuildSpecificRedisCache.cache
     */
    override fun bulkCache(shardId: Int, entities: Collection<V>) {
        if (!entities.isEmpty())
            bulkCache(identifyGuild(entities.first()), shardId, entities)
    }

    /**
     * Method that gets an entity from the cache
     * @param guildId the id of the guild the entity is on
     * @param entityId the id of the entity
     * @return the entity
     */
    fun get(guildId: Long, entityId: K): V {
        return get(GuildInformationContainer(guildId, entityId))
    }

    /**
     * Method that deletes an entity from cache
     * @param shardId the id of the shard the entity is on
     * @param guildId the id of the guild the entity is on
     * @param entityId the id of the entity
     */
    fun delete(shardId: Int, guildId: Long, entityId: K)

    /**
     * Method that deletes all entities of a guild from cache
     * @param shardId the id of the shard the entity is on
     * @param guildId the id of the guild the entity is on
     */
    fun delete(shardId: Int, guildId: Long)

    /**
     * Equivalent of [RedisCache.getAll] for just getting entities of the specified guild
     * @param guildId the id of the guild
     * @see RedisCache.getAll
     * @return all cached entities in a [Collection]
     */
    fun getAll(guildId: Long): Collection<V>

    /**
     * Equivalent of [RedisCache.asCacheView] for just getting entities of the specified guild
     * @param guildId the id of the guild
     * @see RedisCache.asCacheView
     * @return all cached entities in a [CacheView]
     */
    fun asCacheView(guildId: Long): CacheView<V> {
        return getAll(guildId).asCacheView()
    }

    /**
     * Equivalent of [RedisCache.asNamedCacheView] for just getting entities of the specified guild
     * @param guildId the id of the guild
     * @see RedisCache.asNamedCacheView
     * @return all cached entities in a [NamedCacheView]
     */
    fun asNamedCacheView(guildId: Long, nameFunction: (entity: V) -> String): NamedCacheView<V> {
        return getAll(guildId).asNamedCacheView(nameFunction)
    }

    override fun formatHashIdentifier(shardId: Int): ByteArray {
        throw UnsupportedOperationException("That operation is not supported by guild specific caches")
    }

    override fun update(shardId: Int, identifier: GuildInformationContainer<K>, entity: V) {
        throw UnsupportedOperationException("That operation is not supported by guild specific caches")
    }

    /**
     * Data class for providing guild entity information
     * @property guildId the id of the guild
     * @property entityIdentifier the identifier of the entity
     * @param K the type of the identifier
     */
    data class GuildInformationContainer<K>(val guildId: Long, val entityIdentifier: K)
}