package org.discordlist.cloud.shared.models.discord

import net.dv8tion.jda.core.Permission

interface IPermissionHolder {
    val guildId: Long
    val permissions: List<Permission>
    val permissionsRaw: Long
}