package com.yaroslavm87.minesweeper.ui.game

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.ViewConfiguration
import com.yaroslavm87.minesweeper.model.CellStateType
import com.yaroslavm87.minesweeper.ui.UiConst
import kotlinx.coroutines.*
import kotlin.properties.Delegates

class SurfaceViewMineField @JvmOverloads constructor (
    private val ctx: Context,
    attrs: AttributeSet? = null
) : SurfaceView(ctx, attrs)
{

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : this(
        ctx,
        attrs
    )

    constructor(
        fragment: FragmentMineField,
        screenWidth: Int,
        screenHeight: Int,
        columnsAmount: Int,
        rowsAmount: Int,
    ) : this(fragment.requireContext(), null) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        this.columnsAmount = columnsAmount
        this.rowsAmount = rowsAmount

        holder.addCallback(fragment)
        initGridVew()
    }

    private val className: String = "SurfaceViewMineField"
    private val loggingEnabled = true

    private lateinit var grid: Grid
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    lateinit var onSurfaceViewTouchEventListener: OnSurfaceViewTouchEventListener

    private var screenWidth by Delegates.notNull<Int>()
    private var screenHeight by Delegates.notNull<Int>()

    private var fieldWidth by Delegates.notNull<Int>()
    private var fieldHeight by Delegates.notNull<Int>()

    private var columnsAmount by Delegates.notNull<Int>()
    private var rowsAmount by Delegates.notNull<Int>()

    private var accumulatedNegativeOffsetX = 0
    private var accumulatedNegativeOffsetY = 0

    private var isXScrollable by Delegates.notNull<Boolean>()
    private var isYScrollable by Delegates.notNull<Boolean>()

    interface OnSurfaceViewTouchEventListener {
        fun onSurfaceViewMineFieldClick(cellTouchedIndex: Int) {}
        fun onSurfaceViewMineFieldLongClick(cellTouchedIndex: Int)
    }

    fun drawMineField(mineFieldValues: Array<CellStateType>) {
        drawInBackgroundThread(mineFieldValues, accumulatedNegativeOffsetX, accumulatedNegativeOffsetY)
    }

    internal fun onSurfaceDestroyed() {
        scope.cancel()
    }

    private fun initGridVew() {
        log("initGridVew()")
        grid = Grid(ctx, screenWidth, screenHeight, columnsAmount, rowsAmount)

        grid.getFieldWidth().also {
            fieldWidth = it
            isXScrollable = it > screenWidth
        }
        grid.getFieldHeight().also {
            fieldHeight = it
            isYScrollable = it > screenHeight
        }
    }

    private var actionDownMoment: Long? = null
    private var probableCellIndex: Int? = null
    private var previousX: Int?  = null
    private var previousY: Int?  = null
    private var scaledTouchSlop = ViewConfiguration.get(ctx).scaledTouchSlop
    private var isScrollIntention = false
    private var isLongClickPerformed = false

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                previousX = event.x.toInt()
                previousY  = event.y.toInt()
                actionDownMoment = System.currentTimeMillis()
                probableCellIndex = grid.getIndexForGivenCoords(
                    event.x.toInt() - accumulatedNegativeOffsetX,
                    event.y.toInt() - accumulatedNegativeOffsetY
                )
                log("onTouchEvent(): ACTION_DOWN, probableCellIndex = $probableCellIndex")
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (previousX != null && previousY != null) {

                    val prevX: Int = previousX as Int
                    val prevY: Int = previousY as Int
                    val movedX: Int = event.x.toInt()
                    val movedY: Int = event.y.toInt()
                    val touchRadius = scaledTouchSlop / 2

                    if (!isScrollIntention) {
                        isScrollIntention =
                            movedX !in (prevX - touchRadius)..(prevX + touchRadius)
                            || movedY !in (prevY - touchRadius)..(prevY + touchRadius)

                        val moment = System.currentTimeMillis()
                        if (
                            moment - actionDownMoment!! >= UiConst.longClickMinLength
                            && probableCellIndex != null
                            && !isLongClickPerformed
                        ) {
                            onSurfaceViewTouchEventListener.onSurfaceViewMineFieldLongClick(probableCellIndex!!)
                            isLongClickPerformed = true
                            log("onTouchEvent(): ACTION_UP, LongClick = ${moment - actionDownMoment!!} ms")
                        }

                    } else {

                        val isXScrolling = movedX !in (prevX - touchRadius)..(prevX + touchRadius)
                        log("onTouchEvent(): ACTION_MOVE, isXScrolling = $isXScrolling, isXScrollable = $isXScrollable")
                        if (isXScrollable && isXScrolling) {
                            accumulatedNegativeOffsetX += (movedX - prevX)
                            if (accumulatedNegativeOffsetX >= 0) accumulatedNegativeOffsetX = 0
                            else if (accumulatedNegativeOffsetX < screenWidth - fieldWidth) accumulatedNegativeOffsetX = screenWidth - fieldWidth
                            previousX = movedX
                            log("onTouchEvent(): ACTION_MOVE, previousX = $prevX, movedX = $movedX")
                        }

                        val isYScrolling = movedY !in (prevY - touchRadius)..(prevY + touchRadius)
                        log("onTouchEvent(): ACTION_MOVE, isYScrolling = $isYScrolling, isYScrollable = $isYScrollable")
                        if (isYScrollable && isYScrolling) {
                            accumulatedNegativeOffsetY += (movedY - prevY)
                            if (accumulatedNegativeOffsetY >= 0) accumulatedNegativeOffsetY = 0
                            else if (accumulatedNegativeOffsetY < screenHeight - fieldHeight) accumulatedNegativeOffsetY = screenHeight - fieldHeight
                            previousY  = movedY
                            log("onTouchEvent(): ACTION_MOVE, previousY = $prevY, movedY = $movedY")
                        }

                        if (isXScrolling || isYScrolling)
                            drawInBackgroundThread(null, accumulatedNegativeOffsetX, accumulatedNegativeOffsetY)
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isScrollIntention) {
                    val moment = System.currentTimeMillis()
                    if (moment - actionDownMoment!! < UiConst.longClickMinLength && probableCellIndex != null) {
                        onSurfaceViewTouchEventListener.onSurfaceViewMineFieldClick(probableCellIndex!!)
                        log("onTouchEvent(): ACTION_UP, ShortClick = ${moment - actionDownMoment!!} ms")
                    }
                    actionDownMoment = null
                    probableCellIndex = null
                }
                isScrollIntention = false
                isLongClickPerformed = false

                return true
            }

            else -> return false
        }
    }

    private fun drawInBackgroundThread(mineFieldValues: Array<CellStateType>?, offsetX: Int, offsetY: Int) {
        scope.launch {
            val canvas = holder.lockCanvas()
            try {
                grid.drawMineField(canvas, mineFieldValues, offsetX, offsetY)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(UiConst.LOG_TAG, "$className.$message")
    }
}
