package com.yaroslavm87.minesweeper.model
import kotlinx.serialization.Serializable
import java.lang.IllegalArgumentException
import java.util.*

@Serializable
class GameProgress {

    internal var state = Model.State.JUST_INITIALIZED

    internal var columnsAmount: Int = 0
        set(value) {
            if (value >= 0) field = value
            else throw IllegalArgumentException("$className.columnsAmount: unable to assign field to $value")
        }

    internal var rowsAmount: Int = 0
        set(value) {
            if (value >= 0) field = value
            else throw IllegalArgumentException("$className.rowsAmount: unable to assign field to $value")
        }

    internal var minesAmount: Int = 0
        set(value) {
            if (value in 0 until columnsAmount * rowsAmount) field = value
            else throw IllegalArgumentException("$className.minesAmount: unable to assign field to $value")
        }

    private val _mineIndices = mutableSetOf<Int>()

    @kotlinx.serialization.Transient
    internal val mineIndices = Collections.unmodifiableSet(_mineIndices)

    private val _markedCells = mutableSetOf<Int>()

    @kotlinx.serialization.Transient
    internal var markedCells = Collections.unmodifiableSet(_markedCells)

    private val _exploredCells = mutableSetOf<Int>()

    @kotlinx.serialization.Transient
    internal val exploredCells = Collections.unmodifiableSet(_exploredCells)

    @kotlinx.serialization.Transient
    internal var mineField: MineField? = null

    @kotlinx.serialization.Transient
    private val className: String = "GameProgress"

    internal fun putMineIndex(cellIndex: Int) {
        if (cellIndex in 0 until columnsAmount * rowsAmount && !_mineIndices.contains(cellIndex))
            _mineIndices.add(cellIndex)
        else {
            var s = ""
            _mineIndices.forEach { s = "$s$it, " }
            throw IllegalArgumentException("$className.putMineIndex(): unable to put index = $cellIndex " +
                "when _mineIndices = $s") }
    }

    internal fun putMarkedCell(cellIndex: Int) {
        if (cellIndex in 0 until columnsAmount * rowsAmount && !_markedCells.contains(cellIndex))
            _markedCells.add(cellIndex)
        else throw IllegalArgumentException("$className.putMarkedCell(): unable to put index = $cellIndex")
    }

    internal fun removeMarkedCell(cellIndex: Int) {
        if (_markedCells.contains(cellIndex)) _markedCells.remove(cellIndex)
        else throw IllegalArgumentException("$className.removeMarkedCell(): unable to remove index = $cellIndex")
    }

    internal fun saveExploredCellIndex(exploredCellIndex: Int) {
        if (exploredCellIndex in 0 until columnsAmount * rowsAmount
            && !_exploredCells.contains(exploredCellIndex)
        )
            _exploredCells.add(exploredCellIndex)
        else throw IllegalArgumentException("$className.saveExploredCellIndex(): unable to put index = $exploredCellIndex")
    }

    internal fun invalidateData() {
        state = Model.State.JUST_INITIALIZED
        mineField = null
        minesAmount = 0
        columnsAmount = 0
        rowsAmount = 0
        _mineIndices.clear()
        _markedCells.clear()
        _exploredCells.clear()
    }

}

//
//    internal var safeCellIndexForFirstMove: Int = -1
//        set(value) {
//            if (value in 0 until columnsAmount * rowsAmount) field = value
//            else throw IllegalArgumentException("$className.safeCellIndexForFirstMove: unable to assign value = $value")
//        }

//    private var hasGameHaveNotStartedYet = true
//    private var hasGameHaveNotFinishedYet = true
//
//    internal var isGameOngoing = false
//        set(value) {
//            if (hasGameHaveNotStartedYet && hasGameHaveNotFinishedYet && value) {
//                field = true
//                hasGameHaveNotStartedYet = false
//            } else if (!hasGameHaveNotStartedYet && hasGameHaveNotFinishedYet && !value) {
//                field = false
//                hasGameHaveNotFinishedYet = false
//            } else throw IllegalArgumentException("$className.isGameOngoing: unable to assign value to $value, " +
//                    "when hasGameHaveNotStartedYet = $hasGameHaveNotStartedYet " +
//                    "and hasGameHaveNotFinishedYet = $hasGameHaveNotFinishedYet")
//        }

//    internal var hasMineExploded = false
//        set(value) {
//            if (value) field = value
//            else throw IllegalArgumentException("$className.hasMineExploded: unable to assign value = $value")
//        }
