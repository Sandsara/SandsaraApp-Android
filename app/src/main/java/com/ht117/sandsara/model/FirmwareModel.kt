package com.ht117.sandsara.model

import kotlinx.serialization.Serializable

@Serializable
data class FirmwareModel(val version: String = "Unknown",
                         val file: List<SandFile> = emptyList())

@Serializable
data class FirmwareResponse(val fields: FirmwareModel = FirmwareModel()
)

@Serializable
data class BaseFirmwareResponse(val records: List<FirmwareResponse>)