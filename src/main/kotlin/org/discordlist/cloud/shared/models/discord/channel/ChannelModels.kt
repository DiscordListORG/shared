package org.discordlist.cloud.shared.models.discord.channel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.entities.*

abstract class ChannelModel(
        val id: Long,
        val type: Int,
        val guildId: Long?,
        val position: Int?,
        val permissionOverwrites: Array<OverwriteModel>?,
        val name: String?,
        val nsfw: Boolean?,
        val parentId: Long?
) {
    companion object {
        fun of(channel: Channel): ChannelModel {
            when (channel) {
                is TextChannel -> return TextChannelModel.of(channel)
                is Category -> return CategoryModel.of(channel)
                is VoiceChannel -> return VoiceChannelModel.of(channel)
                is PrivateChannel -> return PrivateChannelModel.of(channel)
                else -> return UnknownChannelModel(
                        channel.idLong,
                        channel.type.id,
                        if (channel.guild == null) null else channel.guild.idLong,
                        channel.positionRaw,
                        channel.permissionOverrides.map { OverwriteModel.of(it) }.toTypedArray(),
                        channel.name,
                        null,
                        if (channel.parent == null) null else channel.parent.idLong
                )
            }
        }

    }

}

class UnknownChannelModel(
        id: Long,
        type: Int,
        guildId: Long?,
        position: Int?,
        permissionOverwrites: Array<OverwriteModel>?,
        name: String?,
        nsfw: Boolean?,
        parentId: Long?
) : ChannelModel(id, type, guildId, position, permissionOverwrites, name, nsfw, parentId) {

}

open class MessageChannelModel(
        id: Long,
        type: Int,
        guildId: Long?,
        position: Int?,
        permissionOverwrites: Array<OverwriteModel>?,
        name: String?,
        nsfw: Boolean?,
        parentId: Long?,
        val lastMessageId: Long?
) : ChannelModel(id, type, guildId, position, permissionOverwrites, name, nsfw, parentId) {
    companion object {
        fun of(messageChannel: MessageChannel): MessageChannelModel {
            return MessageChannelModel(
                    messageChannel.idLong,
                    messageChannel.type.id,
                    null,
                    null,
                    null,
                    messageChannel.name,
                    null,
                    null,
                    messageChannel.latestMessageIdLong
            )
        }
    }
}


class TextChannelModel(
        id: Long,
        guildId: Long,
        name: String,
        position: Int,
        permissionOverwrites: Array<OverwriteModel>,
        val rateLimitPerUser: Int,
        nsfw: Boolean,
        val topic: String?,
        lastMessageId: Long?,
        parentId: Long?
) : MessageChannelModel(
    id = id,
    guildId = guildId,
    name = name,
    position = position,
    permissionOverwrites = permissionOverwrites,
    nsfw = nsfw,
    lastMessageId = lastMessageId,
    parentId = parentId,
    type = 0
) {
    companion object {
        fun of(textChannel: TextChannel): TextChannelModel {
            return TextChannelModel(
                    textChannel.idLong,
                    textChannel.guild.idLong,
                    textChannel.name,
                    textChannel.positionRaw,
                    textChannel.permissionOverrides.map { OverwriteModel.of(it) }.toTypedArray(),
                    2,
                    textChannel.isNSFW,
                    textChannel.topic,
                    if (textChannel.hasLatestMessage()) textChannel.latestMessageIdLong else null,
                    if (textChannel.parent == null) null else textChannel.parent.idLong
            )
        }

        fun fromJSON(data: String): TextChannelModel {
            return jacksonObjectMapper().readValue(data, TextChannelModel::class.java)
        }
    }
}

class PrivateChannelModel(
    lastMessageId: Long?,
    id: Long
) : MessageChannelModel(
    id,
    1,
    null,
    null,
    null,
    null,
    false,
    null,
    lastMessageId
) {
    companion object {
        fun of(privateChannel: PrivateChannel): PrivateChannelModel {
            return PrivateChannelModel(
                    privateChannel.latestMessageIdLong,
                    privateChannel.idLong
            )
        }
    }
}

class VoiceChannelModel(
        id: Long,
        guildId: Long,
        name: String,
        position: Int,
        permissionOverwrites: Array<OverwriteModel>,
        val bitrate: Int,
        val userLimit: Int,
        parentId: Long?
) : ChannelModel(
    id = id,
    guildId = guildId,
    name = name,
    position = position,
    permissionOverwrites = permissionOverwrites,
    parentId = parentId,
    nsfw = false,
    type = 2
) {
    companion object {
        fun of(voiceChannel: VoiceChannel): VoiceChannelModel {
            return VoiceChannelModel(
                    voiceChannel.idLong,
                    voiceChannel.guild.idLong,
                    voiceChannel.name,
                    voiceChannel.positionRaw,
                    voiceChannel.permissionOverrides.map { OverwriteModel.of(it) }.toTypedArray(),
                    voiceChannel.bitrate,
                    voiceChannel.userLimit,
                    if (voiceChannel.parent == null) null else voiceChannel.parent.idLong
            )
        }
    }

}

class CategoryModel(permissionOverwrites: Array<OverwriteModel>, name: String, position: Int, guildId: Long, id: Long) :
    ChannelModel(
        permissionOverwrites = permissionOverwrites,
        name = name,
        nsfw = false,
        position = position,
        guildId = guildId,
        id = id,
        type = 4,
        parentId = null
    ) {
    companion object {
        fun of(category: Category): CategoryModel {
            return CategoryModel(
                    category.permissionOverrides.map { OverwriteModel.of(it) }.toTypedArray(),
                    category.name,
                    category.positionRaw,
                    category.guild.idLong,
                    category.idLong
            )
        }
    }
}

data class OverwriteModel(
    val id: Long,
    val type: String,
    val allow: Long,
    val deny: Long
) {
    companion object {
        fun of(permissionOverride: PermissionOverride): OverwriteModel {
            return OverwriteModel(
                    if (permissionOverride.isRoleOverride) permissionOverride.role.idLong else permissionOverride.member.user.idLong,
                    if (permissionOverride.isRoleOverride) "role" else "member",
                    permissionOverride.allowedRaw,
                    permissionOverride.deniedRaw
            )
        }
    }
}