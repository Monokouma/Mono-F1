package car.entity

import kotlinx.serialization.Serializable

@Serializable
data class CarStateEntity(
    val isOn: Boolean,
    val color: String,
    val brightness: Int = 70
)
