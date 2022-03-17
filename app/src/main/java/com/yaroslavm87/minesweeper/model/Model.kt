package com.yaroslavm87.minesweeper.model

import android.util.Log
import com.yaroslavm87.minesweeper.eventBus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.random.Random

object Model {

    enum class State {
        JUST_INITIALIZED,
        INITIAL_PARAMETERS_ARE_SET,
        GAME_IS_ONGOING,
        GAME_HAS_FINISHED_WITH_SUCCESS,
        GAME_HAS_FINISHED_WITH_FAIL,
    }

    private val scope = CoroutineScope(Job())
    private var gameProgress = GameProgress()
    private const val cellValue_mineExploded = -2
    private const val cellValue_mineInside = -1
    private const val cellValue_hasNoMinesAround = 0
    private const val className: String = "Model"
    private const val loggingEnabled = true


    fun getGameState(): State {
        return gameProgress.state
    }

    fun setColumnsAmount(amount: Int) {
        log("setColumnsAmount()")
        if (amount > 0) {
            gameProgress.columnsAmount = amount
            EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_COLUMNS_AMOUNT_UPDATED, amount)
            if (areInitialParametersSet()) changeState(State.INITIAL_PARAMETERS_ARE_SET)
        }
        else throw IllegalArgumentException("$className.setColumnsAmount(): unable to assign value to $amount")
    }

    fun setRowsAmount(amount: Int) {
        log("setRowsAmount()")
        if (amount > 0) {
            gameProgress.rowsAmount = amount
            EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_ROWS_AMOUNT_UPDATED, amount)
            if (areInitialParametersSet()) changeState(State.INITIAL_PARAMETERS_ARE_SET)
        }
        else throw IllegalArgumentException("$className.setColumnsAmount(): unable to assign value to $amount")
    }

    fun setMinesAmount(amount: Int) {
        log("setMinesAmount()")
        if (amount > 0) {
            gameProgress.minesAmount = amount
            if (areInitialParametersSet()) changeState(State.INITIAL_PARAMETERS_ARE_SET)
        }
        else throw IllegalArgumentException("$className.setColumnsAmount(): unable to assign value to $amount")
    }

    fun exploreCell(cellIndex: Int) {
        scope.launch(Dispatchers.Main) {
            log("exploreCell(): cellIndex = $cellIndex, start executing in ${Thread.currentThread()}")

            if (gameProgress.state == State.INITIAL_PARAMETERS_ARE_SET) {

                val buildFieldJob = scope.launch(Dispatchers.Default) {
                    log("exploreCell(): start building field in ${Thread.currentThread()}")
                    val mineIndices = generateMineIndices(
                        gameProgress.minesAmount,
                        cellIndex,
                        gameProgress.columnsAmount * gameProgress.rowsAmount
                    )
                    gameProgress.mineField = buildMineField(mineIndices)
                }

                log("exploreCell(): waiting for building field in ${Thread.currentThread()}")
                buildFieldJob.join()
                changeState(State.GAME_IS_ONGOING)
            } else {
                log("--- state = ${gameProgress.state}, minesAmount = ${gameProgress.minesAmount}")
            }

            /*
            * this is used to distribute non null mineField ref to consequent method calls
            * obtaining atomic nature of action represented by exploreCell() method
            */
            val localRefMineField = gameProgress.mineField
                ?: throw IllegalStateException("$className.explore(): mineField cannot be null")

            if (!isCannotContinueAlsoWith(cellIndex)
                && !gameProgress.markedCells.contains(cellIndex)) {

                val exploreCellJob = scope.launch(Dispatchers.Default) {
                    log("exploreCell(): start exploring cell in ${Thread.currentThread()}")
                    explore(cellIndex, localRefMineField.field)
                }

                log("exploreCell(): waiting for exploring cell in ${Thread.currentThread()}")
                exploreCellJob.join()

                if (areAllSafeCellsWereExplored())
                    finishGameWithSuccess(localRefMineField.field)
            }
        }
    }

    fun markCellContainsMine(cellIndex: Int) {
        log("markCellContainsMine(): cellIndex = $cellIndex")

        if (isCannotContinueAlsoWith(cellIndex)) return

        /*
        * this is used to distribute non null mineField ref to consequent method calls
        * obtaining atomic nature of action represented by markCellContainsMine() method
        */
        val localRefMineField = gameProgress.mineField
            ?: throw IllegalStateException("$className.markCellContainsMine(): mineField cannot be null")

        mark(cellIndex)

        if (areAllMinesWereMarked()) finishGameWithSuccess(localRefMineField.field)
    }

    fun invalidateModelData() {
        log("invalidateModelData(): mineField.field.size = ${gameProgress.mineField?.field?.size}")
        gameProgress.invalidateData()
        gameProgress.mineField = null
        log("invalidateModelData(): mineField.field.size = ${gameProgress.mineField?.field?.size}")
    }

    fun saveGameStateTo(saveRepository: ModelSaveRepository<GameProgress>) {
        saveRepository.save(gameProgress)
    }

    fun restoreGameStateFrom(restoreRepository: ModelRestoreRepository<GameProgress?>) {
        restoreRepository.restore()?.also { restored ->
            gameProgress.apply {

                state = restored.state

                columnsAmount = restored.columnsAmount.also { amount ->
                    EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_COLUMNS_AMOUNT_UPDATED, amount)
                    if (areInitialParametersSet()) changeState(State.INITIAL_PARAMETERS_ARE_SET)
                }

                rowsAmount = restored.rowsAmount.also { amount ->
                    EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_ROWS_AMOUNT_UPDATED, amount)
                    if (areInitialParametersSet()) changeState(State.INITIAL_PARAMETERS_ARE_SET)
                }

                minesAmount = restored.minesAmount.also {
                    if (areInitialParametersSet()) changeState(State.INITIAL_PARAMETERS_ARE_SET)
                }

                if (state == State.GAME_IS_ONGOING
                    || state == State.GAME_HAS_FINISHED_WITH_SUCCESS
                    || state == State.GAME_HAS_FINISHED_WITH_FAIL) {

                    restored.exploredCells.forEach { saveExploredCellIndex(it) }

                    restored.markedCells.forEach { putMarkedCell(it) }

                    mineField = buildMineField(restored.mineIndices.toIntArray())
                        .also { mineField ->
                            notifySubscribersWithExploredCells(
                                exploredCells.toList().mapToIndexStatePair(mineField.field)
                            )
                        }

                    EventBus.notifySubscribersWithValue(
                        ModelEvent.MINE_FIELD_CELL_MARKED_AS_CONTAINING_MINE,
                        markedCells.toTypedArray())
                }
            }
       }
    }

    private fun areInitialParametersSet() : Boolean {
        return gameProgress.columnsAmount in 1..ModelConst.columnsMaxAmount
                && gameProgress.rowsAmount in 1..ModelConst.rowsMaxAmount
                && gameProgress.minesAmount in 1..ModelConst.minesMaxAmount
    }

    private fun changeState(state: State) {
        when (state) {
            State.JUST_INITIALIZED -> gameProgress.state = state
            State.INITIAL_PARAMETERS_ARE_SET -> gameProgress.state = state
            State.GAME_IS_ONGOING -> { gameProgress.state = state }
            State.GAME_HAS_FINISHED_WITH_SUCCESS -> gameProgress.state = state
            State.GAME_HAS_FINISHED_WITH_FAIL -> gameProgress.state = state
        }
    }

    private fun isCannotContinueAlsoWith(cellIndex: Int) : Boolean {
        var result = false

        if (gameProgress.state != State.GAME_IS_ONGOING) {
            result = true
            when (gameProgress.state) {
                State.JUST_INITIALIZED ->
                    log("isCannotContinueWith(): cannot perform; game cannot be started as initial parameters have not been provided")
                State.INITIAL_PARAMETERS_ARE_SET ->
                    log("isCannotContinueWith(): cannot perform; mineField has not been built yet")
                State.GAME_HAS_FINISHED_WITH_SUCCESS,
                State.GAME_HAS_FINISHED_WITH_FAIL ->
                    log("isCannotContinueWith(): cannot perform; game has finished")
                else -> {}
            }
        }

        if (cellIndex !in 0 until gameProgress.columnsAmount * gameProgress.rowsAmount) {
            log("ifCannotContinueWith(): cellIndex is out of mine field range")
            result = true
        }

        if (gameProgress.exploredCells.contains(cellIndex)) {
            log("exploreCell(): cell has been already explored")
            result = true
        }

        return result
    }

    private fun buildMineField(mineIndices: IntArray): MineField {
        val s = System.currentTimeMillis()
        log("buildMineField()")

        mineIndices.forEach { gameProgress.putMineIndex(it) }

        return MineField.build(
            gameProgress.columnsAmount,
            gameProgress.rowsAmount,
            gameProgress.mineIndices.toIntArray()
        ).also {
            val f = System.currentTimeMillis()
            log("--- mineField size = ${it.field.size}, built in ${f-s} ms")
        }
    }

    private fun generateMineIndices(
        minesAmount: Int,
        safeCellIndexForFirstMove: Int,
        cellsAmount: Int
    ): IntArray {

        val safeCellNeighbours = FieldCell
            .pickAppropriatePositionType(
                safeCellIndexForFirstMove,
                gameProgress.columnsAmount * gameProgress.rowsAmount,
                gameProgress.columnsAmount)
            .calculateNeighbours(safeCellIndexForFirstMove, gameProgress.columnsAmount, gameProgress.rowsAmount)

        var freeIndicesTmp = (0 until cellsAmount)
            .filter { it != safeCellIndexForFirstMove }

        if (cellsAmount - 1 - safeCellNeighbours.size - minesAmount >= 0)
            freeIndicesTmp = freeIndicesTmp.filter { it !in safeCellNeighbours }

        val freeIndicesFinal = LinkedList(freeIndicesTmp)

        val mineIndices = mutableSetOf<Int>()
        var uniqueIndex: Int

        repeat(minesAmount) {
            do {
                uniqueIndex = freeIndicesFinal[Random.nextInt(freeIndicesFinal.size)]
            }
            while (mineIndices.contains(uniqueIndex))

            uniqueIndex.also {
                mineIndices.add(it)
                freeIndicesFinal.remove(it)
            }
        }

        return mineIndices.toIntArray()
    }

    private fun explore(cellIndex: Int, mineField: Array<FieldCell>) {

        val exploredCellIndices = mutableListOf<Int>()

        fun exploreRecursively(indexOfCellToExplore: Int) {

            val cellToExplore = mineField[indexOfCellToExplore]

            gameProgress.saveExploredCellIndex(indexOfCellToExplore)
            exploredCellIndices.add(indexOfCellToExplore)
            log("explore(): add index = $indexOfCellToExplore to exploredCellIndices")

            when (cellToExplore.valueType) {

                FieldCell.CellValueType.SAFE_NO_MINES_AROUND -> {

                    cellToExplore.getNeighbourIndices(
                        indexOfCellToExplore,
                        gameProgress.columnsAmount,
                        gameProgress.rowsAmount
                    ).forEach { index ->
                            val neighbourCell = mineField[index]
                            if (!gameProgress.exploredCells.contains(index)
                                && neighbourCell.valueType != FieldCell.CellValueType.HAS_MINE_INSIDE
                            ) exploreRecursively(index)
                        }
                }

                FieldCell.CellValueType.SAFE_HAS_MINES_AROUND -> {}

                FieldCell.CellValueType.HAS_MINE_INSIDE -> {
                    scope.launch(Dispatchers.Main) { finishGameWithFail(indexOfCellToExplore, mineField) }
                }
            }
        }

        exploreRecursively(cellIndex)

        if (mineField[cellIndex].valueType != FieldCell.CellValueType.HAS_MINE_INSIDE)
            scope.launch(Dispatchers.Main) {
                notifySubscribersWithExploredCells(exploredCellIndices.mapToIndexStatePair(mineField))
                mineField.print()
            }
    }

    private fun mark(index: Int) {
        if (gameProgress.markedCells.contains(index)) {
            gameProgress.removeMarkedCell(index)
            EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_CELL_UNMARKED_AS_CONTAINING_MINE, index)
        }
        else {
            gameProgress.putMarkedCell(index)
            EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_CELL_MARKED_AS_CONTAINING_MINE, Array(1) {index})
        }
    }

    private fun areAllSafeCellsWereExplored(): Boolean {
        return (gameProgress.columnsAmount * gameProgress.rowsAmount
        - gameProgress.exploredCells.size == gameProgress.minesAmount)
    }

    private fun areAllMinesWereMarked(): Boolean {
        return gameProgress.mineIndices
            .map { it in gameProgress.markedCells }
            .fold(true) { previous: Boolean, next: Boolean -> previous && next }
    }

    private fun finishGameWithSuccess(mineField: Array<FieldCell>) {
        changeState(State.GAME_HAS_FINISHED_WITH_SUCCESS)
        EventBus.notifySubscribers(ModelEvent.GAME_OVER_SUCCESS)
        notifySubscribersWithExploredCells(
            mineField.getUnexploredFieldCellIndices().markAsExplored().mapToIndexStatePair(mineField)
        )
    }

    private fun finishGameWithFail(cellIndexWithMineExploded: Int, mineField: Array<FieldCell>) {
        log("finishGameWithFail()")

        changeState(State.GAME_HAS_FINISHED_WITH_FAIL)

        EventBus.notifySubscribers(ModelEvent.GAME_OVER_FAIL)

        notifySubscribersWithExploredCells(
            mineField.getUnexploredFieldCellIndices().markAsExplored().mapToIndexStatePair(mineField)
        )

        notifySubscribersWithExploredCells(
            Array(1) {
                Pair(
                    cellIndexWithMineExploded,
                    pickAppropriateCellStateType(
                        cellValue_mineExploded,
                        cellIndexWithMineExploded
                    )
                )
            }
        )
    }

    private fun notifySubscribersWithExploredCells(array: Array<Pair<Int, CellStateType>>) {
        if (array.isNotEmpty())
            log("notifySubscribersWithExploredCells()")
            EventBus.notifySubscribersWithValue(ModelEvent.MINE_FIELD_CELL_EXPLORED, array)
    }

    private fun Array<FieldCell>.getUnexploredFieldCellIndices() : List<Int> {
        log("getUnexploredFieldCells()")
        return this.indices.filter { it !in gameProgress.exploredCells }
    }

    private fun List<Int>.markAsExplored() : List<Int> {
            log("markAsExplored()")
            return this.onEach {
                gameProgress.saveExploredCellIndex(it)
            }
        }

    private fun List<Int>.mapToIndexStatePair(mineField: Array<FieldCell>) : Array<Pair<Int, CellStateType>> {
        log("mapToIndexValuePair(): unexplored field cells amount = ${this.size}")
        return this.map { cellIndex ->
            Pair(
                cellIndex,
                pickAppropriateCellStateType(
                    mineField[cellIndex].contentValue,
                    cellIndex
                )
            )
        }.toTypedArray()
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }

    private fun Array<FieldCell>.print() {
        var string = ""
        indices.forEach {
            if (
                it + 1 < gameProgress.columnsAmount
                || (it + 1 > gameProgress.columnsAmount && (it + 1) % gameProgress.columnsAmount != 0)
            ) {
                string += "${ this[it].contentValue }, "

            } else {
                string += "${ this[it].contentValue }, "
                log(string)
                string = ""
            }
        }
    }

    private fun pickAppropriateCellStateType(cellContentValue: Int, cellIndex: Int): CellStateType {

        return gameProgress.run {

            if (cellIndex !in exploredCells) {

                if (cellIndex in markedCells) CellStateType.HIDDEN_MARKED
                else CellStateType.HIDDEN

            } else {

                when (cellContentValue) {

                    cellValue_mineInside ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_MINE_NOT_EXPLODED_MARKED
                        else CellStateType.REVEALED_MINE_NOT_EXPLODED

                    cellValue_hasNoMinesAround ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_0_MARKED
                        else CellStateType.REVEALED_0

                    1 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_1_MARKED
                        else CellStateType.REVEALED_1

                    2 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_2_MARKED
                        else CellStateType.REVEALED_2

                    3 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_3_MARKED
                        else CellStateType.REVEALED_3

                    4 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_4_MARKED
                        else CellStateType.REVEALED_4

                    5 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_5_MARKED
                        else CellStateType.REVEALED_5

                    6 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_6_MARKED
                        else CellStateType.REVEALED_6

                    7 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_7_MARKED
                        else CellStateType.REVEALED_7

                    8 ->
                        if (cellIndex in markedCells) CellStateType.REVEALED_8_MARKED
                        else CellStateType.REVEALED_8

                    else ->
                        CellStateType.REVEALED_MINE_EXPLODED
                }
            }
        }
    }

}