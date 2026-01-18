package car

import car.entity.CarStateEntity
import f1.GetF1DataUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import java.util.Collections.emptyMap

class ManageCarUseCase(
    private val getF1DataUseCase: GetF1DataUseCase,
) {

    private val mockHue = flowOf(true)

    operator fun invoke(

    ): Flow<Map<String, CarStateEntity>> = combine(
        getF1DataUseCase(),
        mockHue
    ) { f1Result, isHueOn ->
        emptyMap<String, CarStateEntity>()
    }.distinctUntilChanged()
}