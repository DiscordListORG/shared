package org.discordlist.cloud.shared.models.discord.guild

import net.dv8tion.jda.core.entities.Guild
import org.discordlist.cloud.shared.models.discord.GuildVoiceStateModel
import org.discordlist.cloud.shared.models.discord.VoiceStateModel
import org.discordlist.cloud.shared.models.discord.channel.ChannelModel

data class GuildModel(
        val id: Long,
        val name: String,
        val iconHash: String?,
        val splashHash: String?,
        val ownerId: Long,
        val region: String,
        val afkChannelId: Long?,
        val afkTimeout: Int,
        val verificationLevel: Int,
        val explicitContentFilter: Int,
        val roles: Array<RoleModel>,
        val emotes: Array<EmoteModel>,
        val features: Array<String>,
        val mfaLevel: Int,
        val systemMessageChannel: Long?,
        val joinedAt: Long,
        val unavailable: Boolean,
        val memberCount: Int,
        val voiceStates: Array<VoiceStateModel>,
        val members: Array<GuildMemberModel>,
        val channels: Array<ChannelModel>
) {
    companion object {
        fun of(guild: Guild): GuildModel {
            return GuildModel(
                guild.idLong,
                guild.name,
                guild.iconId,
                guild.splashId,
                guild.ownerIdLong,
                guild.regionRaw,
                if (guild.afkChannel == null) null else guild.afkChannel.idLong,
                guild.afkTimeout.seconds,
                guild.verificationLevel.key,
                guild.explicitContentLevel.key,
                guild.roles.map { RoleModel.of(it) }.toTypedArray(),
                guild.emotes.map { EmoteModel.fromEmote(it) }.toTypedArray(),
                guild.features.toTypedArray(),
                guild.requiredMFALevel.key,
                guild.systemChannel.idLong,
                guild.selfMember.joinDate.toInstant().toEpochMilli(),
                !guild.isAvailable,
                guild.memberCache.size().toInt(),
                guild.voiceStates.map { GuildVoiceStateModel.of(it) }.toTypedArray(),
                guild.members.map { GuildMemberModel.of(it) }.toTypedArray(),
                guild.channels.map { ChannelModel.of(it) }.toTypedArray()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GuildModel

        if (id != other.id) return false
        if (name != other.name) return false
        if (iconHash != other.iconHash) return false
        if (splashHash != other.splashHash) return false
        if (ownerId != other.ownerId) return false
        if (region != other.region) return false
        if (afkChannelId != other.afkChannelId) return false
        if (afkTimeout != other.afkTimeout) return false
        if (verificationLevel != other.verificationLevel) return false
        if (explicitContentFilter != other.explicitContentFilter) return false
        if (!roles.contentEquals(other.roles)) return false
        if (!emotes.contentEquals(other.emotes)) return false
        if (!features.contentEquals(other.features)) return false
        if (mfaLevel != other.mfaLevel) return false
        if (systemMessageChannel != other.systemMessageChannel) return false
        if (joinedAt != other.joinedAt) return false
        if (unavailable != other.unavailable) return false
        if (memberCount != other.memberCount) return false
        if (!voiceStates.contentEquals(other.voiceStates)) return false
        if (!members.contentEquals(other.members)) return false
        if (channels != null) {
            if (other.channels == null) return false
            if (!channels.contentEquals(other.channels)) return false
        } else if (other.channels != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + iconHash.hashCode()
        result = 31 * result + splashHash.hashCode()
        result = 31 * result + ownerId.hashCode()
        result = 31 * result + region.hashCode()
        result = 31 * result + afkChannelId.hashCode()
        result = 31 * result + afkTimeout
        result = 31 * result + verificationLevel
        result = 31 * result + explicitContentFilter
        result = 31 * result + roles.contentHashCode()
        result = 31 * result + emotes.contentHashCode()
        result = 31 * result + features.contentHashCode()
        result = 31 * result + mfaLevel
        result = 31 * result + (systemMessageChannel?.hashCode() ?: 0)
        result = 31 * result + joinedAt.hashCode()
        result = 31 * result + unavailable.hashCode()
        result = 31 * result + memberCount
        result = 31 * result + voiceStates.contentHashCode()
        result = 31 * result + members.contentHashCode()
        result = 31 * result + (channels?.contentHashCode() ?: 0)
        return result
    }
}