package org.discordlist.cloud.shared.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

abstract class SerializableModel {

    fun toJSON(): String {
        return jacksonObjectMapper().writeValueAsString(this)
    }
}