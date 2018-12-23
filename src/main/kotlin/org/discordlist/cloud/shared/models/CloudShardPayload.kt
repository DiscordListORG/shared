package org.discordlist.cloud.shared.models

class CloudShardPayload(val shardId: Int, op: CloudPayload.OpCode, data: Any): CloudPayload(op, data)