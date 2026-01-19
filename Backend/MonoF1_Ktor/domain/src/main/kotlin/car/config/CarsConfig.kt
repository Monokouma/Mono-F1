package car.config

object CarsConfig {
    val CARS = mapOf(
        "redbull" to TeamConfig("Red Bull Racing", "3671C6"),
        "ferrari" to TeamConfig("Ferrari", "E8002D"),
        "mercedes" to TeamConfig("Mercedes", "27F4D2"),
        "mclaren" to TeamConfig("McLaren", "FF8000")
    )

    const val COLOR_WHITE = "FAFAFA"
    const val COLOR_OFF = "000000"
}

data class TeamConfig(
    val teamName: String,
    val color: String
)