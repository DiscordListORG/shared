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

package org.discordlist.cloud.shared.cache;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCache;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.cache.view.CacheView;
import com.mewna.catnip.cache.view.DefaultCacheView;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.channel.*;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.shard.DiscordEvent;
import com.mewna.catnip.shard.DiscordEvent.Raw;
import com.mewna.catnip.util.SafeVertxCompletableFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.discordlist.cloud.shared.cache.impl.GuildSpecificRedisCacheImpl;
import org.discordlist.cloud.shared.cache.impl.GuildSpecificSnowflakeRedisCache;
import org.discordlist.cloud.shared.cache.impl.SnowflakeRedisCache;
import org.discordlist.cloud.shared.io.redis.RedisSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * RedisCache implementation of {@link com.mewna.catnip.cache.EntityCacheWorker} may contains some code of {@link com.mewna.catnip.cache.MemoryEntityCache}
 */
public class RedisCacheWorker implements EntityCacheWorker {

    private final Logger log = LoggerFactory.getLogger(RedisCacheWorker.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private Catnip catnip;
    private EntityBuilder entityBuilder;
    private final GuildSpecificSnowflakeRedisCache<Role> rolesCache;
    private final SnowflakeRedisCache<Guild> guildCache;
    private final SnowflakeRedisCache<UserDMChannel> dmChannelCache;
    private final GuildSpecificSnowflakeRedisCache<Member> memberCache;
    private final SnowflakeRedisCache<User> userCache;
    private final GuildSpecificSnowflakeRedisCache<Emoji.CustomEmoji> emojiCache;
    private final GuildSpecificSnowflakeRedisCache<GuildChannel> channelCache;
    private final GuildSpecificRedisCache<String, VoiceState> voiceStateCache;
    private final AtomicReference<User> selfUser = new AtomicReference<>(null);
    private final RedisSource redisSource;

    /**
     * Constructor for RedisCacheWorker
     *
     * @param redisSource the redis source {@link RedisSource} that is used for connecting to redis
     */
    public RedisCacheWorker(RedisSource redisSource) {
        this.redisSource = redisSource;
        this.rolesCache = new GuildSpecificSnowflakeRedisCache<>(
                Role::guildIdAsLong,
                RedisCache.ROLES,
                Role.class,
                redisSource,
                Object::toString
        );
        this.guildCache = new SnowflakeRedisCache<>(
                RedisCache.GUILDS,
                Guild.class,
                redisSource
        );
        this.dmChannelCache = new SnowflakeRedisCache<>(
                RedisCache.DM_CHANNELS,
                UserDMChannel.class,
                redisSource
        );
        this.memberCache = new GuildSpecificSnowflakeRedisCache<>(
                Member::guildIdAsLong,
                RedisCache.MEMBERS,
                Member.class,
                redisSource,
                Object::toString
        );
        this.userCache = new SnowflakeRedisCache<>(
                RedisCache.USERS,
                User.class,
                redisSource
        );
        this.emojiCache = new GuildSpecificSnowflakeRedisCache<>(
                Emoji.CustomEmoji::guildIdAsLong,
                RedisCache.EMOJIS,
                Emoji.CustomEmoji.class,
                redisSource,
                Object::toString
        );
        this.channelCache = new GuildSpecificSnowflakeRedisCache<>(
                GuildChannel::guildIdAsLong,
                RedisCache.CHANNELS,
                GuildChannel.class,
                redisSource,
                Object::toString,
                (json, __) -> {
                    var type = Channel.ChannelType.byKey(json.getInteger("type"));
                    json.remove("type");
                    switch (type) {
                        case TEXT:
                            return Entity.fromJson(catnip, TextChannel.class, json);
                        case VOICE:
                            return Entity.fromJson(catnip, VoiceChannel.class, json);
                        case CATEGORY:
                            return Entity.fromJson(catnip, Category.class, json);
                        default:
                            throw new UnsupportedOperationException("Unsupported channel type " + type);
                    }
                },
                (channel) -> channel.toJson().put("type", channel.type().getKey()).encode().getBytes(StandardCharsets.UTF_8)
        );
        this.voiceStateCache = new GuildSpecificRedisCacheImpl<>(
                (state) -> new GuildSpecificRedisCache.GuildInformationContainer<>(state.guildIdAsLong(), state.userId()),
                GuildSpecificRedisCache.GuildInformationContainer::getEntityIdentifier,
                RedisCache.VOICE_STATES,
                VoiceState.class,
                redisSource,
                Object::toString,
                VoiceState::guildIdAsLong
        );
    }

    @Nonnull
    @Override
    public Future<java.lang.Void> updateCache(@Nonnull String eventType, int shardId, @Nonnull JsonObject payload) {
        switch (eventType) {
            // Lifecycle
            case Raw.READY: {
                var future = CompletableFuture.supplyAsync(() -> {
                    try (Jedis redis = redisSource.jedis()) {
                        redis.set("selfUser", entityBuilder.createUser(payload.getJsonObject("user")).toJson().encode());
                    }
                    return null;
                }, executor);
                return future(future);
            }
            // Channels
            case Raw.CHANNEL_CREATE:
            case Raw.CHANNEL_UPDATE: {
                final var channel = entityBuilder.createChannel(payload);
                if (channel.isGuild())
                    return future(channelCache.cache(shardId, ((GuildChannel) channel)));
                else if (channel.isUserDM())
                    return future(dmChannelCache.cache(shardId, ((UserDMChannel) channel)));
                else
                    log.warn("I don't know how to cache channel {}: isCategory={}, isDM={}, isGroupDM={}," +
                                    "isGuild={}, isText={}, isUserDM={}, isVoice={}",
                            channel.idAsLong(), channel.isCategory(), channel.isDM(), channel.isGroupDM(), channel.isGuild(),
                            channel.isText(), channel.isUserDM(), channel.isVoice());
                break;
            }
            case Raw.CHANNEL_DELETE: {
                final var channel = entityBuilder.createChannel(payload);
                if (channel.isGuild())
                    return future(channelCache.delete(shardId, channel.asGuildChannel().guildIdAsLong(), channel.idAsLong()));
                else if (channel.isUserDM())
                    return future(dmChannelCache.delete(shardId, channel.idAsLong()));
                else
                    log.warn("I don't know how to delete non-guild channel {}!", channel.idAsLong());

                break;
            }

            // Guilds
            case Raw.GUILD_UPDATE:
            case Raw.GUILD_CREATE: {
                final Future<Void> future = Future.future();
                catnip.vertx().executeBlocking(f -> {
                    final Guild guild = entityBuilder.createAndCacheGuild(shardId, payload);
                    guildCache.cache(shardId, guild);
                    f.complete(null);
                }, __ -> future.complete(null));
                return future;
            }
            case Raw.GUILD_DELETE: {
                final long guildId = Long.parseUnsignedLong(payload.getString("id"));
                return future(guildCache.delete(shardId, guildId),
                        memberCache.delete(shardId, guildId),
                        rolesCache.delete(shardId, guildId),
                        channelCache.delete(shardId, guildId),
                        emojiCache.delete(shardId, guildId),
                        voiceStateCache.delete(shardId, guildId)
                );
            }
            case Raw.GUILD_ROLE_UPDATE:
            case Raw.GUILD_ROLE_CREATE: {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getJsonObject("role");
                final Role role = entityBuilder.createRole(guild, json);
                return future(rolesCache.cache(shardId, role));
            }
            case Raw.GUILD_ROLE_DELETE: {
                final long guild = payload.getLong("guild_id");
                final long role = payload.getLong("role_id");
                return future(rolesCache.delete(shardId, guild, role));
            }
            case Raw.GUILD_MEMBER_UPDATE: {
                // This doesn't send an object like all the other events, so we build a fake
                // payload object and create an entity from that
                final JsonObject user = payload.getJsonObject("user");
                final String id = user.getString("id");
                final String guild = payload.getString("guild_id");
                final Member old = member(guild, id);
                if (old != null) {
                    @SuppressWarnings("ConstantConditions") final JsonObject data = new JsonObject()
                            .put("user", user)
                            .put("roles", payload.getJsonArray("roles"))
                            .put("nick", payload.getString("nick"))
                            .put("deaf", old.deaf())
                            .put("mute", old.mute())
                            .put("joined_at", old.joinedAt()
                                    // If we have an old member cached, this shouldn't be an issue
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    final Member member = entityBuilder.createMember(guild, data);
                    return future(memberCache.cache(Long.parseLong(guild), shardId, member));
                } else {
                    log.warn("Got GUILD_MEMBER_UPDATE for {} in {}, but we don't have them cached?!", id, guild);
                }
                break;
            }
            case Raw.GUILD_MEMBER_REMOVE: {
                final var guild = payload.getString("guild_id");
                final var user = payload.getJsonObject("user").getLong("id");
                return future(memberCache.delete(shardId, Long.parseLong(guild), user));
            }
            // Add member on join
            case Raw.GUILD_MEMBER_ADD : {
                var guild = payload.getString("guild_id");
                var member = entityBuilder.createMember(guild, payload);
                var user = entityBuilder.createUser(payload.getJsonObject("user"));
                memberCache.cache(shardId, member);
                userCache.cache(shardId, user);
            }
            case Raw.GUILD_MEMBERS_CHUNK: {
                final long guild = payload.getLong("guild_id");
                final JsonArray members = payload.getJsonArray("members");
                return future(members.stream().map(e -> memberCache.cache(guild, shardId, entityBuilder.createMember(String.valueOf(guild), (JsonObject) e))).collect(Collectors.toList()).toArray(CompletableFuture[]::new));
            }

            case Raw.GUILD_EMOJIS_UPDATE: {
                if (!catnip.cacheFlags().contains(CacheFlag.DROP_EMOJI)) {
                    final Long guild = payload.getLong("guild_id");
                    final JsonArray emojis = payload.getJsonArray("emojis");
                    return future(emojis.stream().map(e -> emojiCache.cache(guild, shardId, entityBuilder.createCustomEmoji(String.valueOf(guild), (JsonObject) e))).collect(Collectors.toList()).toArray(CompletableFuture[]::new));
                }
                break;
            }
            case Raw.VOICE_STATE_UPDATE: {
                if (!catnip.cacheFlags().contains(CacheFlag.DROP_VOICE_STATES)) {
                    final VoiceState state = entityBuilder.createVoiceState(payload);
                    return future(voiceStateCache.cache(state.guildIdAsLong(), shardId, state));
                }
                break;
            }
            case Raw.USER_UPDATE:
                var user = entityBuilder.createUser(payload);
                var future = CompletableFuture.supplyAsync(() -> {
                    if (user.equals(selfUser.get()))
                        selfUser.set(user);
                    try (Jedis redis = redisSource.jedis()) {
                        redis.set("selfUser", entityBuilder.createUser(payload.getJsonObject("user")).toJson().encode());
                    }
                    return null;
                }, executor);
                return future(catnip, future, userCache.cache(shardId, user));

        }
        return Future.succeededFuture();
    }

    @Override
    public void bulkCacheUsers(int shardId, @Nonnull Collection<User> users) {
        userCache.bulkCache(shardId, users);
    }

    @Override
    public void bulkCacheChannels(int shardId, @Nonnull Collection<GuildChannel> channels) {
        channelCache.bulkCache(shardId, channels);
    }

    @Override
    public void bulkCacheRoles(int shardId, @Nonnull Collection<Role> roles) {
        rolesCache.bulkCache(shardId, roles);
    }

    @Override
    public void bulkCacheMembers(int shardId, @Nonnull Collection<Member> members) {
        memberCache.bulkCache(shardId, members);
    }

    @Override
    public void bulkCacheEmoji(int shardId, @Nonnull Collection<Emoji.CustomEmoji> emoji) {
        emojiCache.bulkCache(shardId, emoji);
    }

    /**
     * Not implemented because not needed
     * @param shardId shard id
     * @param presences presence list
     */
    @Override
    public void bulkCachePresences(int shardId, @Nonnull Map<String, Presence> presences) {

    }

    @Override
    public void bulkCacheVoiceStates(int shardId, @Nonnull Collection<VoiceState> voiceStates) {
        voiceStateCache.bulkCache(shardId, voiceStates);
    }

    @Override
    public void invalidateShard(int id) {
        rolesCache.invalidate(id);
        guildCache.invalidate(id);
        dmChannelCache.invalidate(id);
        memberCache.invalidate(id);
        userCache.invalidate(id);
        emojiCache.invalidate(id);
    }

    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull Catnip catnip) {
        this.catnip = catnip;
        this.entityBuilder = new EntityBuilder(catnip);
        rolesCache.catnip(catnip);
        guildCache.catnip(catnip);
        dmChannelCache.catnip(catnip);
        memberCache.catnip(catnip);
        userCache.catnip(catnip);
        emojiCache.catnip(catnip);
        channelCache.catnip(catnip);
        voiceStateCache.catnip(catnip);
        return this;
    }

    @Nonnull
    @Override
    public CompletableFuture<Guild> guildAsync(long l) {
        return guildCache.getAsync(l);
    }

    @Nonnull
    @Override
    public NamedCacheView<Guild> guilds() {
        return guildCache.asNamedCacheView(Guild::name);
    }

    @Nonnull
    @Override
    public CompletableFuture<User> userAsync(long l) {
        return userCache.getAsync(l);
    }

    @Nonnull
    @Override
    public NamedCacheView<User> users() {
        return userCache.asNamedCacheView(User::username);
    }

    @Nullable
    @Override
    public Presence presence(long id) {
        return null;
    }

    @Nonnull
    @Override
    public CompletableFuture<Presence> presenceAsync(long l) {
        return SafeVertxCompletableFuture.completedFuture(Presence.of(Presence.OnlineStatus.INVISIBLE));
    }

    @Nonnull
    @Override
    public CacheView<Presence> presences() {
        return new DefaultCacheView<>();
    }

    @Nonnull
    @Override
    public CompletableFuture<Member> memberAsync(long l, long l1) {
        return memberCache.getAsync(l, l1);
    }

    @Nonnull
    @Override
    public NamedCacheView<Member> members(long guildId) {
        return memberCache.asNamedCacheView(guildId, (member) -> member.nick() == null ? "null" : member.nick());
    }

    @Nonnull
    @Override
    public NamedCacheView<Member> members() {
        return memberCache.asNamedCacheView((member) -> member.nick() == null ? "null" : member.nick());
    }

    @Nonnull
    @Override
    public CompletableFuture<Role> roleAsync(long l, long l1) {
        return rolesCache.getAsync(l, l1);
    }

    @Nonnull
    @Override
    public NamedCacheView<Role> roles(long guildId) {
        return rolesCache.asNamedCacheView(guildId, Role::name);
    }

    @Nonnull
    @Override
    public NamedCacheView<Role> roles() {
        return rolesCache.asNamedCacheView(Role::name);
    }

    @Nonnull
    @Override
    public CompletableFuture<GuildChannel> channelAsync(long l, long l1) {
        return channelCache.getAsync(l, l1);
    }

    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels(long guildId) {
        return channelCache.asNamedCacheView(guildId, GuildChannel::name);
    }

    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels() {
        return channelCache.asNamedCacheView(GuildChannel::name);
    }

    @Nonnull
    @Override
    public CompletableFuture<UserDMChannel> dmChannelAsync(long l) {
        return dmChannelCache.getAsync(l);
    }

    @Nonnull
    @Override
    public CacheView<UserDMChannel> dmChannels() {
        return dmChannelCache.asCacheView();
    }

    @Nonnull
    @Override
    public CompletableFuture<Emoji.CustomEmoji> emojiAsync(long l, long l1) {
        return emojiCache.getAsync(l, l1);
    }

    @Nonnull
    @Override
    public NamedCacheView<Emoji.CustomEmoji> emojis(long guildId) {
        return emojiCache.asNamedCacheView(guildId, Emoji.CustomEmoji::name);
    }

    @Nonnull
    @Override
    public NamedCacheView<Emoji.CustomEmoji> emojis() {
        return emojiCache.asNamedCacheView(Emoji.CustomEmoji::name);
    }

    @Nonnull
    @Override
    public CompletableFuture<VoiceState> voiceStateAsync(long l, long l1) {
        return voiceStateCache.getAsync(l, String.valueOf(l1));
    }

    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates(long guildId) {
        return voiceStateCache.asCacheView(guildId);
    }

    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates() {
        return voiceStateCache.asCacheView();
    }

    @Nonnull
    @Override
    public CompletableFuture<User> selfUserAsync() {
        return SafeVertxCompletableFuture.completedFuture(selfUser.get());
    }

    private Future<Void> future(CompletableFuture<?>... futures) {
        return future(catnip, futures);
    }

    private Future<Void> future(Catnip catnip, CompletableFuture<?>... futures) {
        Future<Void> vertxFuture = Future.future();
        SafeVertxCompletableFuture<Void> future = SafeVertxCompletableFuture.allOf(catnip, futures);
        future.thenAccept((__) -> vertxFuture.complete());
        return vertxFuture;
    }

}
