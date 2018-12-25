package org.discordlist.cloud.shared.models.discord.user

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.entities.User
import org.discordlist.cloud.shared.models.SerializableModel

data class UserModel (
    val id: Long,
    val name: String,
    val discriminator: String,
    val mention: String,
    val avatarId: String?,
    val avatarUrl: String?,
    val defaultAvatarId: String,
    val defaultAvatarUrl: String,
    val fake: Boolean,
    val bot: Boolean,
    val creationTime: Long
): SerializableModel() {

    companion object {
        fun of(user: User): UserModel {
            return UserModel(
                    user.idLong,
                    user.name,
                    user.discriminator,
                    user.asMention,
                    user.avatarId,
                    user.avatarUrl,
                    user.defaultAvatarId,
                    user.defaultAvatarUrl,
                    user.isFake,
                    user.isBot,
                    user.creationTime.toEpochSecond()
            )
        }

        fun fromJSON(data: String): UserModel {
            return jacksonObjectMapper().readValue(data, UserModel::class.java)
        }
    }
}