package hue.dto

import hue.entity.HueLightStateEntity
import kotlinx.serialization.Serializable

@Serializable
data class HueEventDto(
    val creationtime: String? = null,
    val data: List<HueEventDataDto>? = null,
    val id: String? = null,
    val type: String? = null
)

@Serializable
data class HueEventDataDto(
    val id: String? = null,
    val type: String? = null,
    val on: HueOnStateDto? = null,
    val owner: HueOwnerDto? = null
)

@Serializable
data class HueOnStateDto(
    val on: Boolean? = null
)

@Serializable
data class HueOwnerDto(
    val rid: String? = null,
    val rtype: String? = null
)

fun List<HueEventDto>.toEntity(): HueLightStateEntity? {
    val isOn = this
        .flatMap { it.data ?: emptyList() }
        .find { it.type == "grouped_light" && it.owner?.rtype == "bridge_home" }
        ?.on?.on ?: return null

    return HueLightStateEntity(isOn = isOn)
}