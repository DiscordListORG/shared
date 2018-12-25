package org.discordlist.cloud.shared.models.discord

import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import org.discordlist.cloud.shared.models.discord.guild.EmoteModel
import org.discordlist.cloud.shared.models.discord.guild.GuildMemberModel
import org.discordlist.cloud.shared.models.discord.guild.RoleModel
import org.discordlist.cloud.shared.models.discord.user.UserModel

data class MessageModel(
        val id: Long,
        val channelId: Long,
        val guildId: Long?,
        val author: UserModel,
        val member: GuildMemberModel?,
        val content: String,
        val timestamp: Long,
        val editedTimestamp: Long,
        val tts: Boolean,
        val mentionedEveryone: Boolean,
        val mentions: Array<UserModel>,
        val mentionRoles: Array<RoleModel>,
        val attachments: Array<AttachmentModel>,
        val embeds: Array<String>,
        val reactions: Array<ReactionModel>,
        val nonce: Long?,
        val pinned: Boolean,
        val type: Int
) {
    companion object {
        fun fromMessage(message: Message): MessageModel {
            return MessageModel(
                message.idLong,
                if (message.channelType == ChannelType.TEXT) message.textChannel.idLong else message.privateChannel.idLong,
                if (message.guild != null) message.guild.idLong else null,
                UserModel.of(message.author),
                if (message.member != null) GuildMemberModel.of(message.member) else null,
                message.contentRaw,
                message.creationTime.toInstant().toEpochMilli(),
                if(message.editedTime != null) message.editedTime.toInstant().toEpochMilli() else 0,
                message.isTTS,
                message.mentionsEveryone(),
                message.mentionedUsers.map { UserModel.of(it) }.toTypedArray(),
                message.mentionedRoles.map { RoleModel.of(it) }.toTypedArray(),
                message.attachments.map { AttachmentModel.of(it) }.toTypedArray(),
                message.embeds.map { it.toJSONObject().toString(2) }.toTypedArray(),
                message.reactions.map { ReactionModel.of(it) }.toTypedArray(),
                if(message.nonce != null) message.nonce.toLong() else 0,
                message.isPinned,
                message.type.id
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageModel

        if (id != other.id) return false
        if (channelId != other.channelId) return false
        if (guildId != other.guildId) return false
        if (author != other.author) return false
        if (member != other.member) return false
        if (content != other.content) return false
        if (timestamp != other.timestamp) return false
        if (editedTimestamp != other.editedTimestamp) return false
        if (tts != other.tts) return false
        if (mentionedEveryone != other.mentionedEveryone) return false
        if (!mentions.contentEquals(other.mentions)) return false
        if (!mentionRoles.contentEquals(other.mentionRoles)) return false
        if (!attachments.contentEquals(other.attachments)) return false
        if (!embeds.contentEquals(other.embeds)) return false
        if (!reactions.contentEquals(other.reactions)) return false
        if (nonce != other.nonce) return false
        if (pinned != other.pinned) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + channelId.hashCode()
        result = 31 * result + (guildId?.hashCode() ?: 0)
        result = 31 * result + author.hashCode()
        result = 31 * result + (member?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + editedTimestamp.hashCode()
        result = 31 * result + tts.hashCode()
        result = 31 * result + mentionedEveryone.hashCode()
        result = 31 * result + mentions.contentHashCode()
        result = 31 * result + mentionRoles.contentHashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + embeds.contentHashCode()
        result = 31 * result + reactions.contentHashCode()
        result = 31 * result + (nonce?.hashCode() ?: 0)
        result = 31 * result + pinned.hashCode()
        result = 31 * result + type
        return result
    }
}

data class ReactionModel(
    val count: Int,
    val me: Boolean,
    val emoji: ReactionEmoteModel
) {
    companion object {
        fun of(messageReaction: MessageReaction): ReactionModel {
            return ReactionModel(
                messageReaction.count,
                messageReaction.isSelf,
                ReactionEmoteModel.fromReactionEmote(messageReaction.reactionEmote)
            )
        }
    }
}

class ReactionEmoteModel(
    val id: Long?,
    val name: String?,
    val emote: EmoteModel?
) {
    companion object {
        fun fromReactionEmote(reactionEmote: MessageReaction.ReactionEmote): ReactionEmoteModel {
            return ReactionEmoteModel(
                reactionEmote.idLong,
                reactionEmote.name,
                if (reactionEmote.emote != null) EmoteModel.fromEmote(reactionEmote.emote) else null
            )
        }
    }
}

data class AttachmentModel(
    val id: Long,
    val filename: String,
    val size: Int,
    val url: String,
    val proxyUrl: String,
    val height: Int?,
    val width: Int?
) {
    companion object {
        fun of(attachment: Message.Attachment): AttachmentModel {
            return AttachmentModel(
                attachment.idLong,
                attachment.fileName,
                attachment.size,
                attachment.url,
                attachment.proxyUrl,
                attachment.height,
                attachment.width
            )
        }
    }
}