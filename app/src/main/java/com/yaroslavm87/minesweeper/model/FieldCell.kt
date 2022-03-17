package com.yaroslavm87.minesweeper.model

class FieldCell (
    private val positionType: CellPositionType,
    val contentValue: Int,
    val valueType: CellValueType,
) {

    fun getNeighbourIndices(cellIndex: Int, columnsAmount: Int, rowsAmount: Int) : IntArray {
        return positionType.calculateNeighbours(cellIndex, columnsAmount, rowsAmount)
    }

    override fun toString(): String {
        return "FieldCell(positionType=$positionType, " +
                "cellValueType=$valueType=" +
                "cellContentValue=${contentValue})"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is FieldCell) return false
        return (
            other.contentValue == contentValue
            &&other.positionType == positionType
        )
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = (prime * result + positionType.hashCode() + contentValue)
        return result
    }

    enum class CellValueType {
        SAFE_NO_MINES_AROUND,
        SAFE_HAS_MINES_AROUND,
        HAS_MINE_INSIDE,
    }

    companion object Builder {

        private var amountOfCellsInField = 0
        private var amountOfColumnsInField = 0
        private var cellIndex = -1
        private var mineCellIndices = IntArray(0) { -1 }
        private val fieldCellInstances = HashMap<Int, FieldCell>()
        private const val className: String = "FieldCell.Builder"

        internal fun amountOfCellsInField(value: Int): Builder {
            amountOfCellsInField = value
            return this
        }

        internal fun fieldColumnsAmount(value: Int): Builder {
            amountOfColumnsInField = value
            return this
        }

        internal fun cellIndex(value: Int): Builder {
            cellIndex = value
            return this
        }

        internal fun mineCellIndices(value: IntArray): Builder {
            mineCellIndices = value
            return this
        }

        internal fun build(): FieldCell {

            val cellsAmount =  amountOfCellsInField / amountOfColumnsInField * amountOfColumnsInField

            if (
                amountOfCellsInField % amountOfColumnsInField == 0
                && cellIndex in 0 until cellsAmount
                && mineCellIndices.isNotEmpty()
            ) {
                val positionType = pickAppropriatePositionType(cellIndex, amountOfCellsInField, amountOfColumnsInField)
                val contentValue = calculateCellContentValue(cellIndex)
                val valueType = setCellValueType(contentValue)
                val probableInstance: FieldCell?
                val key = positionType.hashCode() + contentValue

                fieldCellInstances.run {
                    probableInstance =  get(key)
                    if (cellIndex == cellsAmount - 1) clear()
                }

                return if (fieldCellInstances.containsKey(key) && probableInstance != null) probableInstance
                else {
                    val newInstance =  FieldCell(
                        positionType,
                        contentValue,
                        valueType,
                    )
                    fieldCellInstances[key] = newInstance
                    newInstance
                }

            } else throw IllegalArgumentException("$className.build(): illegal initial parameters")
        }

        internal fun pickAppropriatePositionType(targetIndex: Int, amountOfCells: Int, amountOfColumns: Int): CellPositionType {

            fun calculateMiddleRowFirstIndices(amountOfCellsInField: Int, columnsAmount: Int): IntArray {
                return IntArray(
                    amountOfCellsInField / columnsAmount - 2,
                    init = { (it + 1) * columnsAmount }
                )
            }

            fun ifIndexIsFirstInMiddleRow(): Int {
                return if (targetIndex in calculateMiddleRowFirstIndices(amountOfCells, amountOfColumns)) targetIndex
                else -1
            }

            fun calculateMiddleRowLastIndices(): IntArray {
                return IntArray(
                    amountOfCells / amountOfColumns - 2,
                    init = { (it + 1) * amountOfColumns + amountOfColumns - 1 }
                )
            }

            fun ifIndexIsLastInMiddleRow(): Int {
                return if (targetIndex in calculateMiddleRowLastIndices()) targetIndex
                else -1
            }

            return when(targetIndex) {

                0 ->
                    CellPositionType.FIRST_ROW_FIRST

                amountOfColumns - 1 ->
                    CellPositionType.FIRST_ROW_LAST

                in 1 until (amountOfColumns - 1) ->
                    CellPositionType.FIRST_ROW_MIDDLE

                amountOfCells - amountOfColumns ->
                    CellPositionType.LAST_ROW_FIRST

                amountOfCells - 1 ->
                    CellPositionType.LAST_ROW_LAST

                in (amountOfCells - amountOfColumns + 1) until (amountOfCells - 1) ->
                    CellPositionType.LAST_ROW_MIDDLE

                ifIndexIsFirstInMiddleRow() ->
                    CellPositionType.MIDDLE_ROW_FIRST

                ifIndexIsLastInMiddleRow() ->
                    CellPositionType.MIDDLE_ROW_LAST

                else ->
                    CellPositionType.MIDDLE_ROW_MIDDLE
            }
        }

        private fun calculateCellContentValue(targetCellIndex: Int): Int {
            return if (targetCellIndex in mineCellIndices) {
                -1
            } else {
                var result = 0

                mineCellIndices.forEach { mineIndex ->
                    pickAppropriatePositionType(mineIndex, amountOfCellsInField, amountOfColumnsInField)
                        .calculateNeighbours(mineIndex, amountOfColumnsInField, amountOfCellsInField / amountOfColumnsInField)
                        .onEach { neighbourIndex -> if (neighbourIndex == targetCellIndex) ++result }
                }
                result
            }
        }

        private fun setCellValueType(cellContentValue: Int): CellValueType {
            return when (cellContentValue) {
                -1 -> CellValueType.HAS_MINE_INSIDE
                0 -> CellValueType.SAFE_NO_MINES_AROUND
                else -> CellValueType.SAFE_HAS_MINES_AROUND
            }
        }
    }
}