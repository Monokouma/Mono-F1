package car

import car.config.CarsConfig
import car.entity.CarStateEntity
import f1.GetF1DataUseCase
import f1.entity.F1Entity
import f1.entity.SessionStatus
import hue.SubscribeServerSentEventUseCase
import hue.entity.HueLightStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class ManageCarUseCase(
    private val getF1DataUseCase: GetF1DataUseCase,
    private val subscribeServerSentEventUseCase: SubscribeServerSentEventUseCase
) {

    operator fun invoke(): Flow<Map<String, CarStateEntity>?> = combine(
        getF1DataUseCase(),
        subscribeServerSentEventUseCase()
    ) { f1Result, hueState ->
        calculateStates(hueState, f1Result.getOrNull())
    }.distinctUntilChanged()

    private fun calculateStates(
        hueState: HueLightStateEntity?,
        f1Data: F1Entity?
    ): Map<String, CarStateEntity>? {

        if (hueState == null) return null

        if (!hueState.isOn) {
            return CarsConfig.CARS.mapValues {
                CarStateEntity(isOn = false, color = CarsConfig.COLOR_OFF, brightness = 70)
            }
        }

        if (f1Data == null || f1Data.sessionStatus in listOf(SessionStatus.INACTIVE, SessionStatus.FINALISED)) {
            return CarsConfig.CARS.mapValues {
                CarStateEntity(isOn = true, color = CarsConfig.COLOR_WHITE, brightness = 70)
            }
        }

        val leaderTeam = f1Data.leader?.teamName
        val leaderColor = f1Data.leader?.teamColour

        return CarsConfig.CARS.mapValues { (_, teamConfig) ->
            val isLeader = teamConfig.teamName == leaderTeam
            CarStateEntity(
                isOn = isLeader,
                color = if (isLeader) (leaderColor ?: teamConfig.color) else CarsConfig.COLOR_OFF,
                brightness = 70
            )
        }
    }
}