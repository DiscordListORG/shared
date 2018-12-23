package org.discordlist.cloud.shared.models.discord

import net.dv8tion.jda.core.entities.VoiceState

open class VoiceStateModel(
    val channelId: Long?,
    val sessionId: String?,
    val selfDeaf: Boolean,
    val selfMute: Boolean
) {
    companion object {
        fun fromVoiceState(voiceState: VoiceState): VoiceStateModel {
                return VoiceStateModel(
                    if (voiceState.audioChannel == null) null else voiceState.audioChannel.idLong,
                    if (voiceState.sessionId == null) null else voiceState.sessionId,
                    voiceState.isSelfDeafened,
                    voiceState.isSelfMuted
                )
        }
    }
}

/*data class GuildVoiceStateModel(
    val guildId: Long,
    val member: GuildMemberModel
    ) : VoiceStateModel {

}*/