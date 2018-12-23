package org.discordlist.cloud.shared.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

open class CloudPayload(val op: OpCode, val data: Any) {

    val time: Long = Date().time

    fun toJSON(): String {
        return jacksonObjectMapper().writeValueAsString(this)
    }

    enum class OpCode(val code: Int) {

        SYSTEM(0),
        DISPATCH(1)
    }
}

