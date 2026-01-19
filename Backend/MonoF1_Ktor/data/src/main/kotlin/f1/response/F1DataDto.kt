package f1.response

import f1.entity.F1Entity
import f1.entity.LeaderEntity
import f1.entity.SessionStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class F1DataDto(
    @SerialName("R") val data: F1ContentDto? = null,
    @SerialName("M") val messages: List<F1MessageDto>? = null
)

@Serializable
data class F1ContentDto(
    @SerialName("TimingData") val timingData: TimingDataDto?,
    @SerialName("SessionInfo") val sessionInfo: SessionInfoDto?,
    @SerialName("DriverList") val driverList: Map<String, DriverDto>?
)

@Serializable
data class TimingDataDto(
    @SerialName("Lines") val lines: Map<String, DriverTimingDto>?
)

@Serializable
data class DriverTimingDto(
    @SerialName("Position") val position: String?,
    @SerialName("RacingNumber") val racingNumber: String?
)

@Serializable
data class SessionInfoDto(
    @SerialName("SessionStatus") val sessionStatus: String?,
    @SerialName("Type") val type: String?,
    @SerialName("Name") val name: String?
)

@kotlinx.serialization.Serializable
data class DriverDto(
    @SerialName("RacingNumber") val racingNumber: String?,
    @SerialName("TeamName") val teamName: String?,
    @SerialName("TeamColour") val teamColour: String?,
    @SerialName("FullName") val fullName: String?
)

@Serializable
data class F1MessageDto(
    @SerialName("H") val hub: String?,
    @SerialName("M") val method: String?,
    @SerialName("A") val arguments: List<JsonElement>? = null
)

fun F1DataDto.toEntity(): F1Entity {
    val sessionStatus = when (data?.sessionInfo?.sessionStatus) {
        "Started" -> SessionStatus.STARTED
        "Finalised" -> SessionStatus.FINALISED
        else -> SessionStatus.INACTIVE
    }

    val leader = findLeader()

    return F1Entity(
        sessionStatus = sessionStatus,
        leader = leader
    )
}

private fun F1DataDto.findLeader(): LeaderEntity? {
    val lines = data?.timingData?.lines ?: return null
    val drivers = data.driverList ?: return null

    val leaderEntry = lines.entries.find { it.value.position == "1" } ?: return null
    val leaderNumber = leaderEntry.key
    val driver = drivers[leaderNumber] ?: return null

    return LeaderEntity(
        driverNumber = leaderNumber,
        driverName = driver.fullName ?: "",
        teamName = driver.teamName ?: "",
        teamColour = driver.teamColour ?: ""
    )
}