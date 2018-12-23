package org.discordlist.cloud.shared.models.discord

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Role
import org.discordlist.cloud.shared.models.SerializableModel

data class RoleModel(
    val id: Long,
    val name: String,
    val mention: String,
    val color: Int,
    val position: Int,
    val positionRaw: Int,
    val permissionsRaw: Long,
    val permissions: List<Permission>,
    val managed: Boolean,
    val hoisted: Boolean,
    val mentionable: Boolean,
    val public: Boolean,
    val guildId: Long
): SerializableModel() {

    companion object {
        fun fromRole(role: Role): RoleModel {
            return RoleModel(
                role.idLong,
                role.name,
                role.asMention,
                role.colorRaw,
                role.position,
                role.positionRaw,
                role.permissionsRaw,
                role.permissions,
                role.isManaged,
                role.isHoisted,
                role.isMentionable,
                role.isPublicRole,
                role.guild.idLong
            )
        }

        fun fromJSON(data: String): RoleModel {
            return jacksonObjectMapper().readValue(data, RoleModel::class.java)
        }
    }
}