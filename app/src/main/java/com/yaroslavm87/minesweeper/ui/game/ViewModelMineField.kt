package com.yaroslavm87.minesweeper.ui.game

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.yaroslavm87.minesweeper.eventBus.EventBus
import com.yaroslavm87.minesweeper.eventBus.Subscriber
import com.yaroslavm87.minesweeper.model.*
import com.yaroslavm87.minesweeper.ui.UiConst
import com.yaroslavm87.minesweeper.ui.LiveDataProvider
import com.yaroslavm87.minesweeper.ui.repository.SaveGameProgressRepository
import java.util.*

class ViewModelMineField : ViewModel(), Subscriber {

    private var _mineFieldValues = MutableList(
        LiveDataProvider.mineFieldColumnsAmount.value as Int *
                LiveDataProvider.mineFieldRowsAmount.value as Int
    ) {
        CellStateType.HIDDEN
    }
    var mineFieldValues: List<CellStateType> = Collections.unmodifiableList(_mineFieldValues)
    private val eventsToSubscribeFor = arrayOf(
        ModelEvent.MINE_FIELD_COLUMNS_AMOUNT_UPDATED,
        ModelEvent.MINE_FIELD_ROWS_AMOUNT_UPDATED,
        ModelEvent.MINE_FIELD_CELL_MARKED_AS_CONTAINING_MINE,
        ModelEvent.MINE_FIELD_CELL_UNMARKED_AS_CONTAINING_MINE,
        ModelEvent.MINE_FIELD_CELL_EXPLORED,
        ModelEvent.GAME_OVER_SUCCESS,
        ModelEvent.GAME_OVER_FAIL)
    private val className: String = "ViewModelMineField"
    private val loggingEnabled = true

    init {
        subscribeForEvent()
    }

    fun exploreCell(cellIndex: Int) {
        Model.exploreCell(cellIndex)
    }

    fun markCellContainsMine(cellIndex: Int) {
        log("markCellContainsMine() for cell index = $cellIndex")
        Model.markCellContainsMine(cellIndex)
    }

    override fun subscribeForEvent() {
        log("subscribeForEvent()")
        for (event in eventsToSubscribeFor) EventBus.subscribe(this, event)
    }

    override fun notifyOfEvent(event: EventBus.Event) {
        log("notifyOfEvent(): $event")
        if (event in eventsToSubscribeFor) fetchEventLinkedValueAndProceed(event)
    }

    override fun fetchEventLinkedValueAndProceed(event: EventBus.Event) {
        log("fetchEventLinkedValueAndProceed(): for $event")
        when (event) {

            ModelEvent.MINE_FIELD_COLUMNS_AMOUNT_UPDATED -> {
                val columnsUpdated = EventBus.getEventLinkedValue(this, event) as Int
                val rowsActual = LiveDataProvider.mineFieldRowsAmount.value?: UiConst.defaultRowsAmount
                if (columnsUpdated * rowsActual != _mineFieldValues.size) updateSizeOfMineFieldValues(columnsUpdated * rowsActual)
                LiveDataProvider.mineFieldColumnsAmount.value = columnsUpdated
            }

            ModelEvent.MINE_FIELD_ROWS_AMOUNT_UPDATED -> {
                val rowsUpdated = EventBus.getEventLinkedValue(this, event) as Int
                val columnsActual = LiveDataProvider.mineFieldColumnsAmount.value?: UiConst.defaultColumnsAmount
                if (rowsUpdated * columnsActual != _mineFieldValues.size) updateSizeOfMineFieldValues(rowsUpdated * columnsActual)
                LiveDataProvider.mineFieldRowsAmount.value = rowsUpdated
            }

            ModelEvent.MINE_FIELD_CELL_MARKED_AS_CONTAINING_MINE -> {
                (EventBus.getEventLinkedValue(this, event) as Array<*>).also { array ->
                    if (array.isNotEmpty() && array[0] is Int) {
                        val cellIndicesArray = IntArray(array.size)
                        array.indices.forEach { index ->
                            (array[index] as Int).also {
                                cellIndicesArray[index] = it
                                _mineFieldValues[it] = CellStateType.HIDDEN_MARKED
                            }
                        }
                        LiveDataProvider.viewModelMineFieldCommands.value =
                            ViewModelMineFieldCommands.REDRAW_MINE_FIELD
                    }
                }
            }

            ModelEvent.MINE_FIELD_CELL_UNMARKED_AS_CONTAINING_MINE -> {

                val cellIndex = EventBus.getEventLinkedValue(this, event) as Int
                _mineFieldValues[cellIndex] = CellStateType.HIDDEN

                LiveDataProvider.viewModelMineFieldCommands.value =
                    ViewModelMineFieldCommands.REDRAW_MINE_FIELD
            }

            ModelEvent.MINE_FIELD_CELL_EXPLORED -> {

                (EventBus.getEventLinkedValue(this, event) as Array<*>).forEach { container ->
                    if (container is Pair<*,*>) {
                        _mineFieldValues[container.first as Int] = container.second as CellStateType
                    }
                }
                LiveDataProvider.viewModelMineFieldCommands.value =
                    ViewModelMineFieldCommands.REDRAW_MINE_FIELD
            }

            ModelEvent.GAME_OVER_SUCCESS ->
                LiveDataProvider.viewModelMineFieldCommands.value =
                    ViewModelMineFieldCommands.FINISH_GAME_WITH_SUCCESS

            ModelEvent.GAME_OVER_FAIL -> {
                LiveDataProvider.viewModelMineFieldCommands.value =
                    ViewModelMineFieldCommands.FINISH_GAME_WITH_FAIL
            }
        }
    }

    private fun updateSizeOfMineFieldValues(updatedFieldSize: Int) {
        log("updateSizeOfMineFieldValues(): updatedFieldSize = $updatedFieldSize")
        when {
            updatedFieldSize == _mineFieldValues.size -> return
            updatedFieldSize < _mineFieldValues.size -> _mineFieldValues = _mineFieldValues.subList(0, updatedFieldSize)
            else -> _mineFieldValues.addAll(Array(updatedFieldSize - _mineFieldValues.size) { CellStateType.HIDDEN })
        }

        mineFieldValues = Collections.unmodifiableList(_mineFieldValues)

    }

    override fun unsubscribeFromEvent() {
        log("unsubscribeFromEvent()")
        for (event in eventsToSubscribeFor) EventBus.unsubscribe(this, event)
    }

    fun getGameState(): Model.State {
        return Model.getGameState()
    }

    internal fun saveGameState(context: Context) {
        Model.saveGameStateTo(SaveGameProgressRepository(context))
    }

    private fun invalidateData() {
        log("invalidateData()")
        Model.invalidateModelData()
        _mineFieldValues.indices.forEach { _mineFieldValues[it] = CellStateType.HIDDEN }
        LiveDataProvider.mineFieldColumnsAmount.value = null
        LiveDataProvider.mineFieldRowsAmount.value = null
        LiveDataProvider.viewModelMineFieldCommands.value = null
    }

    override fun onCleared() {
        super.onCleared()
        log("onCleared()")
        unsubscribeFromEvent()
        invalidateData()
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(UiConst.LOG_TAG, "$className.$message")
    }
}
