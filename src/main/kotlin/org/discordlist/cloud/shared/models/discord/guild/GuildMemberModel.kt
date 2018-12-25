package org.discordlist.cloud.shared.models.discord.guild

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.utils.PermissionUtil
import org.discordlist.cloud.shared.models.SerializableModel
import org.discordlist.cloud.shared.models.discord.GuildVoiceStateModel
import org.discordlist.cloud.shared.models.discord.IPermissionHolder
import org.discordlist.cloud.shared.models.discord.user.UserModel
import java.awt.Color
import java.time.OffsetDateTime

data class GuildMemberModel(
        val user: UserModel,
        val nick: String?,
        val roles: Array<RoleModel>,
        val joinedAt: OffsetDateTime,
        val mention: String,
        val guildVoiceState: GuildVoiceStateModel,
        val game: GameModel,
        val onlineStatus: OnlineStatus,
        val effectiveName: String,
        val color: Color,
        val colorRaw: Int,
        override val guildId: Long,
        override val permissions: List<Permission>,
        override val permissionsRaw: Long
) : SerializableModel(), IMentionable, IPermissionHolder {

    override fun getAsMention(): String {
        return mention
    }

    companion object {
        fun of(member: Member): GuildMemberModel {
            return GuildMemberModel(
                    UserModel.of(member.user),
                    member.nickname,
                    member.roles.map { RoleModel.of(it) }.toTypedArray(),
                    member.joinDate,
                    member.asMention,
                    GuildVoiceStateModel.of(member.voiceState),
                    GameModel.of(member.game),
                    member.onlineStatus,
                    member.effectiveName,
                    member.color,
                    member.colorRaw,
                    member.guild.idLong,
                    member.permissions,
                    PermissionUtil.getEffectivePermission(member)
            )
        }

        fun fromJSON(data: String): GuildMemberModel {
            return jacksonObjectMapper().readValue(data, GuildMemberModel::class.java)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GuildMemberModel

        if (user != other.user) return false
        if (nick != other.nick) return false
        if (!roles.contentEquals(other.roles)) return false
        if (joinedAt != other.joinedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + (nick?.hashCode() ?: 0)
        result = 31 * result + roles.contentHashCode()
        result = 31 * result + joinedAt.hashCode()
        return result
    }
}

open class GameModel(
        val name: String,
        val url: String,
        val type: Game.GameType,
        val timestamps: TimestampsModel?
): SerializableModel() {
    companion object {
        fun of(game: Game): GameModel {
            return GameModel(
                    game.name,
                    game.url,
                    game.type,
                    if (game.timestamps != null) TimestampsModel.of(game.timestamps!!) else null
            )
        }

        fun fromJSON(data: String): TimestampsModel {
            return jacksonObjectMapper().readValue(data, TimestampsModel::class.java)
        }
    }
}

open class TimestampsModel(
        val start: Long,
        val end: Long

) {
    companion object {
        fun of(timestamps: RichPresence.Timestamps): TimestampsModel {
            return TimestampsModel(
                    timestamps.start,
                    timestamps.end
            )
        }

        fun fromJSON(data: String): TimestampsModel {
            return jacksonObjectMapper().readValue(data, TimestampsModel::class.java)
        }
    }
}