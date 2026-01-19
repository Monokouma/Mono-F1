package hue

import hue.entity.HueLightStateEntity
import kotlinx.coroutines.flow.Flow

class SubscribeServerSentEventUseCase(
    private val hueRepository: HueRepository
) {
    operator fun invoke(): Flow<HueLightStateEntity?> = hueRepository
        .subscribeServerSentEvent()
}