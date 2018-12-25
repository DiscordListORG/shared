package org.discordlist.cloud.shared.models.discord

import net.dv8tion.jda.core.entities.Invite

data class InviteModel(
        val code: String,
        val guild: InviteGuildModel?,
        val channel: InviteChannelModel,
        val approximatePresenceCount: Int?,
        val approximateMemberCount: Int?
) {
    companion object {
        fun fromInvite(invite: Invite): InviteModel {
            return InviteModel(
                invite.code,
                InviteGuildModel.fromGuild(invite.guild),
                InviteChannelModel.fromChannel(invite.channel),
                invite.maxAge,
                invite.maxUses
            )
        }
    }
}

data class InviteChannelModel(
    val id: Long,
    val name: String,
    val type: Int
) {
    companion object {
        fun fromChannel(channel: Invite.Channel): InviteChannelModel {
            return InviteChannelModel(
                channel.idLong,
                channel.name,
                channel.type.id
            )

        }
    }
}

data class InviteGuildModel(
    val id: Long,
    val iconHash: String,
    val splashHash: String?,
    val verificationLevel: Int,
    val onlineCount: Int,
    val memberCount: Int,
    val features: Array<String>

) {

    companion object {
        fun fromGuild(guild: Invite.Guild): InviteGuildModel {
            return InviteGuildModel(
                guild.idLong,
                guild.iconId,
                guild.splashId,
                guild.verificationLevel.key,
                guild.onlineCount,
                guild.memberCount,
                guild.features.toTypedArray()
            )
        }

    }
}