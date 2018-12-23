package org.discordlist.cloud.shared.models.discord

import net.dv8tion.jda.core.entities.Role

data class RoleModel(
    val id: Long,
    val name: String,
    val mention: String,
    val color: Int,
    val hoist: Boolean,
    val position: Int,
    val permissions: Long,
    val managed: Boolean,
    val mentionable: Boolean
) {

    companion object {
        fun fromRole(role: Role): RoleModel {
            return RoleModel(
                role.idLong,
                role.name,
                role.colorRaw,
                role.isHoisted,
                role.position,
                role.permissionsRaw,
                role.isManaged,
                role.isMentionable
            )
        }
    }
}