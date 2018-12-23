package org.discordlist.cloud.shared.models.discord.channel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.PermissionOverride
import org.discordlist.cloud.shared.models.CloudPayload
import org.discordlist.cloud.shared.models.SerializableModel

data class PermissionOverrideModel (
    val entityId: Long,
    val entityType: EntityType,
    val allowedRaw: Long,
    val inheritRaw: Long,
    val deniedRaw: Long,
    val allowed: List<Permission>,
    val inherit: List<Permission>,
    val denied: List<Permission>,
    val channelId: Long,
    val guildId: Long
): SerializableModel() {

    companion object {

        fun of(permissionOverride: PermissionOverride): PermissionOverrideModel {
            return PermissionOverrideModel(
                if (permissionOverride.isRoleOverride) permissionOverride.role.idLong else permissionOverride.member.user.idLong,
                if (permissionOverride.isRoleOverride) EntityType.ROLE else EntityType.MEMBER,
                permissionOverride.allowedRaw,
                permissionOverride.inheritRaw,
                permissionOverride.deniedRaw,
                permissionOverride.allowed,
                permissionOverride.inherit,
                permissionOverride.denied,
                permissionOverride.channel.idLong,
                permissionOverride.guild.idLong
            )
        }

        fun fromJSON(data: String): PermissionOverrideModel {
            return jacksonObjectMapper().readValue(data, PermissionOverrideModel::class.java)
        }
    }

    enum class EntityType {
        ROLE,
        MEMBER
    }
}