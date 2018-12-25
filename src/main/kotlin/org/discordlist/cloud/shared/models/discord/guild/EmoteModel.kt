package org.discordlist.cloud.shared.models.discord.guild

import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.IFakeable
import net.dv8tion.jda.core.entities.IMentionable
import net.dv8tion.jda.core.entities.ISnowflake
import org.discordlist.cloud.shared.models.SerializableModel

@Suppress("MemberVisibilityCanBePrivate")
open class EmoteModel(
        val id: Long,
        val name: String,
        val roles: Array<RoleModel>?,
        val managed: Boolean?,
        val animated: Boolean?,
        val fake: Boolean,
        val mention: String

) : SerializableModel(), ISnowflake, IFakeable, IMentionable {

    override fun getIdLong(): Long {
        return id
    }

    override fun isFake(): Boolean {
        return fake
    }

    override fun getAsMention(): String {
        return mention
    }

    companion object {
        fun fromEmote(emote: Emote): EmoteModel {
            return EmoteModel(
                    emote.idLong,
                    emote.name,
                    emote.roles.map { RoleModel.of(it) }.toTypedArray(),
                    emote.isManaged,
                    emote.isAnimated,
                    emote.isFake,
                    emote.asMention
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmoteModel

        if (id != other.id) return false
        if (name != other.name) return false
        if (roles != null) {
            if (other.roles == null) return false
            if (!roles.contentEquals(other.roles)) return false
        } else if (other.roles != null) return false
        if (managed != other.managed) return false
        if (animated != other.animated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (roles?.contentHashCode() ?: 0)
        result = 31 * result + (managed?.hashCode() ?: 0)
        result = 31 * result + (animated?.hashCode() ?: 0)
        return result
    }


}