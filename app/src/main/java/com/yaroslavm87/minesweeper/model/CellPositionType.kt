package com.yaroslavm87.minesweeper.model

import java.lang.IllegalArgumentException

enum class CellPositionType {

    FIRST_ROW_FIRST {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(3).apply {
                set(0, cellIndex + 1)
                set(1, cellIndex + columnsAmount)
                set(2, cellIndex + columnsAmount + 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 1.1"
        }
    },

    FIRST_ROW_MIDDLE {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(5).apply {
                set(0, cellIndex - 1)
                set(1, cellIndex + 1)
                set(2, cellIndex + columnsAmount - 1)
                set(3, cellIndex + columnsAmount)
                set(4, cellIndex + columnsAmount + 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 1.2"
        }
    },

    FIRST_ROW_LAST {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(3).apply {
                set(0, cellIndex - 1)
                set(1, cellIndex + columnsAmount - 1)
                set(2, cellIndex + columnsAmount)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 1.3"
        }
    },

    MIDDLE_ROW_FIRST {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(5).apply {
                set(0, cellIndex - columnsAmount)
                set(1, cellIndex - columnsAmount + 1)
                set(2, cellIndex + 1)
                set(3, cellIndex + columnsAmount)
                set(4, cellIndex + columnsAmount + 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 2.1"
        }
    },

    MIDDLE_ROW_MIDDLE {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(8).apply {
                set(0, cellIndex - columnsAmount - 1)
                set(1, cellIndex - columnsAmount)
                set(2, cellIndex - columnsAmount + 1)
                set(3, cellIndex - 1)
                set(4, cellIndex + 1)
                set(5, cellIndex + columnsAmount - 1)
                set(6, cellIndex + columnsAmount)
                set(7, cellIndex + columnsAmount + 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 2.2"
        }
    },

    MIDDLE_ROW_LAST {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(5).apply {
                set(0, cellIndex - columnsAmount - 1)
                set(1, cellIndex - columnsAmount)
                set(2, cellIndex - 1)
                set(3, cellIndex + columnsAmount - 1)
                set(4, cellIndex + columnsAmount)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 2.3"
        }
    },

    LAST_ROW_FIRST {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(3).apply {
                set(0, cellIndex - columnsAmount)
                set(1, cellIndex - columnsAmount + 1)
                set(2, cellIndex + 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 3.1"
        }
    },

    LAST_ROW_MIDDLE {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(5).apply {
                set(0, cellIndex - columnsAmount - 1)
                set(1, cellIndex - columnsAmount)
                set(2, cellIndex - columnsAmount + 1)
                set(3, cellIndex - 1)
                set(4, cellIndex + 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 3.2"
        }
    },

    LAST_ROW_LAST {
        override fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray {
            val neighbours = IntArray(3).apply {
                set(0, cellIndex - columnsAmount - 1)
                set(1, cellIndex - columnsAmount)
                set(2, cellIndex - 1)
                forEach { neighbourIndex ->
                    throwIfOutOfMineFieldRange(neighbourIndex, columnsAmount * rowsAmount)
                }
            }
            return neighbours
        }

        override fun toString(): String {
            return "cell type = 3.3"
        }
    };

    internal abstract fun calculateNeighbours(cellIndex: Int, columnsAmount: Int, rowsAmount: Int): IntArray

    protected fun throwIfOutOfMineFieldRange(neighbourIndex: Int, mineFieldSize: Int) {
        if (neighbourIndex < 0 || neighbourIndex >= mineFieldSize)
            throw IllegalArgumentException("CellPositionType.calculateNeighbours(): neighbour index = $neighbourIndex" +
                    "is out of bounds 0..${ mineFieldSize - 1 }")
    }

}