package hue

import hue.entity.HueLightStateEntity
import kotlinx.coroutines.flow.Flow

interface HueRepository {
    fun subscribeServerSentEvent(): Flow<HueLightStateEntity?>

}