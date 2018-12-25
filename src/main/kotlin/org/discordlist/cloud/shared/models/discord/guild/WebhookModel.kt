package org.discordlist.cloud.shared.models.discord.guild

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.entities.IFakeable
import net.dv8tion.jda.core.entities.ISnowflake
import net.dv8tion.jda.core.entities.Webhook
import org.discordlist.cloud.shared.models.discord.channel.TextChannelModel
import org.discordlist.cloud.shared.models.discord.user.UserModel

data class WebhookModel(
        val id: Long,
        val fake: Boolean,
        val guild: GuildModel?,
        val channelId: TextChannelModel,
        val owner: GuildMemberModel?,
        val user: UserModel,
        val name: String?,
        val url: String,
        val avatar: String?,
        val token: String?
): ISnowflake, IFakeable {
    override fun getIdLong(): Long {
        return id
    }

    override fun isFake(): Boolean {
        return fake
    }

    companion object {
        fun fromWebhook(webhook: Webhook): WebhookModel {
            return WebhookModel(
                    webhook.idLong,
                    webhook.isFake,
                    if (webhook.guild == null) null else GuildModel.of(webhook.guild),
                    TextChannelModel.of(webhook.channel),
                    if (webhook.owner != null) GuildMemberModel.of(webhook.owner!!) else null,
                    UserModel.of(webhook.defaultUser),
                    webhook.defaultUser.name,
                    webhook.url,
                    webhook.defaultUser.avatarId,
                    webhook.token
            )
        }

        fun fromJSON(data: String): WebhookModel {
            return jacksonObjectMapper().readValue(data, WebhookModel::class.java)
        }
    }

}