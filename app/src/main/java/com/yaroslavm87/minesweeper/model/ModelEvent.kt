package com.yaroslavm87.minesweeper.model

import com.yaroslavm87.minesweeper.eventBus.EventBus

enum class ModelEvent : EventBus.Event {

    MINE_FIELD_COLUMNS_AMOUNT_UPDATED,
    MINE_FIELD_ROWS_AMOUNT_UPDATED,
    MINE_FIELD_CELL_MARKED_AS_CONTAINING_MINE,
    MINE_FIELD_CELL_UNMARKED_AS_CONTAINING_MINE,
    MINE_FIELD_CELL_EXPLORED,
    GAME_OVER_SUCCESS,
    GAME_OVER_FAIL,
}