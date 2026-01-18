package f1.entity

data class F1Entity(
    val sessionStatus: SessionStatus,
    val leader: LeaderEntity?
)

data class LeaderEntity(
    val driverNumber: String,
    val driverName: String,
    val teamName: String,
    val teamColour: String
)

enum class SessionStatus {
    STARTED,
    FINALISED,
    INACTIVE
}