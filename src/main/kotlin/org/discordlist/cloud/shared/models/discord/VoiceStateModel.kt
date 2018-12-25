package org.discordlist.cloud.shared.models.discord

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.entities.GuildVoiceState
import net.dv8tion.jda.core.entities.VoiceState
import org.discordlist.cloud.shared.models.SerializableModel
import org.discordlist.cloud.shared.models.discord.channel.VoiceChannelModel
import org.discordlist.cloud.shared.models.discord.guild.GuildMemberModel
import org.discordlist.cloud.shared.models.discord.guild.GuildModel

open class VoiceStateModel(
        val channelId: Long?,
        val sessionId: String?,
        val selfDeaf: Boolean,
        val selfMute: Boolean
) : SerializableModel() {
    companion object {
        fun of(voiceState: VoiceState): VoiceStateModel {
            return VoiceStateModel(
                    if (voiceState.audioChannel == null) null else voiceState.audioChannel.idLong,
                    if (voiceState.sessionId == null) null else voiceState.sessionId,
                    voiceState.isSelfDeafened,
                    voiceState.isSelfMuted
            )
        }

        fun fromJSON(data: String): VoiceStateModel {
            return jacksonObjectMapper().readValue(data, VoiceStateModel::class.java)
        }
    }
}

open class GuildVoiceStateModel(
        val isMuted: Boolean,
        val isDeafened: Boolean,
        val isGuildMuted: Boolean,
        val isGuildDeafened: Boolean,
        val suppressed: Boolean,
        val voiceChannel: VoiceChannelModel,
        val guild: GuildModel,
        val member: GuildMemberModel,
        sessionId: String
) : VoiceStateModel(voiceChannel.id, sessionId, isMuted, isDeafened) {
    companion object {
        fun of(voiceState: GuildVoiceState): GuildVoiceStateModel {
            return GuildVoiceStateModel(
                    voiceState.isMuted,
                    voiceState.isDeafened,
                    voiceState.isGuildMuted,
                    voiceState.isGuildDeafened,
                    voiceState.isSuppressed,
                    VoiceChannelModel.of(voiceState.channel),
                    GuildModel.of(voiceState.guild),
                    GuildMemberModel.of(voiceState.member),
                    voiceState.sessionId
            )
        }

        fun fromJSON(data: String): VoiceStateModel {
            return jacksonObjectMapper().readValue(data, GuildVoiceStateModel::class.java)
        }
    }
}