package com.yaroslavm87.minesweeper.ui.setup

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import com.yaroslavm87.minesweeper.R
import com.yaroslavm87.minesweeper.model.ModelConst

class DialogEditGameOptions(
    private val context: Context,
    private val clickListener: OnDialogEditGameOptionsClickListener
) : View.OnClickListener {

    interface OnDialogEditGameOptionsClickListener {
        fun onClick(v: View?)
    }

    internal var gameOptions: GameOptions? = null
    private lateinit var dialog: Dialog
    private var currentFieldSize = 0
    private var currentColumnsAmount = 0
    private var currentRowsAmount = 0
    private var maxMinesAmount = 0
    private var prevProportionMinesActualMax = 0F
    private var actualMinesAmount = 0

    internal fun showDialog(options: GameOptions) {

        dialog = Dialog(context).apply {

            gameOptions = options

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_edit_game_options)
            setCancelable(true)

            val layoutParams = WindowManager.LayoutParams().apply {
                if (window != null) {
                    copyFrom(window?.attributes)
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                }
            }

            val textViewFieldSize = findViewById<TextView>(R.id.text_view_field_size)
            val textViewMinesPercentage = findViewById<TextView>(R.id.text_view_mines_amount_percentage)
            val textViewColumnsAmount = findViewById<TextView>(R.id.text_view_columns_amount)
            val textViewRowsAmount = findViewById<TextView>(R.id.text_view_rows_amount)
            val textViewMinesAmount = findViewById<TextView>(R.id.text_view_mines_amount_value)

            currentColumnsAmount = options.columnsAmount
            currentRowsAmount = options.rowsAmount
            actualMinesAmount = options.minesAmount

            val seekbarColumnsAmount = findViewById<SeekBar>(R.id.seekbar_columns_amount)
                .apply {
                    max = ModelConst.columnsMaxAmount
                    progress = options.columnsAmount
                }
                .also {
                    textViewColumnsAmount.text = it.progress.toString()
                }

            val seekbarRowsAmount = findViewById<SeekBar>(R.id.seekbar_rows_amount)
                .apply {
                    max = ModelConst.rowsMaxAmount
                    progress = options.rowsAmount
                }
                .also {
                    textViewRowsAmount.text = it.progress.toString()
                }

            currentFieldSize = calcCurrentFieldSize(seekbarColumnsAmount.progress, seekbarRowsAmount.progress).also {
                textViewFieldSize.text = it.toString()
            }

            val seekbarMinesAmount = findViewById<SeekBar>(R.id.seekbar_mines_amount)
                .apply {
                    maxMinesAmount = calcMaxMinesAmount(currentFieldSize)
                        .also {
                            max = it
                        }
                    progress = options.minesAmount
                        .also {
                            textViewMinesAmount.text = it.toString()
                            textViewMinesPercentage.text = minesPercentageAsString(it, currentFieldSize)
                        }
                }

            seekbarColumnsAmount.apply {
                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            prevProportionMinesActualMax = calcPrevProportionMinesActualMax(
                                seekbarMinesAmount.progress,
                                seekbarMinesAmount.max
                            )
                        }

                        override fun onProgressChanged(seekBar: SeekBar?, updatedProgress: Int, fromUser: Boolean) {

                            if (updatedProgress <= ModelConst.columnsMinAmount) progress = ModelConst.columnsMinAmount

                            textViewColumnsAmount.text = progress.toString()

                            currentFieldSize = calcCurrentFieldSize(progress, seekbarRowsAmount.progress)
                                .also {
                                    textViewFieldSize.text = it.toString()
                                }

                            maxMinesAmount = calcMaxMinesAmount(currentFieldSize)

                            actualMinesAmount = calcActualMinesAmount(maxMinesAmount, prevProportionMinesActualMax)
                                .also {
                                    textViewMinesAmount.text = it.toString()
                                }

                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            seekbarMinesAmount.max = maxMinesAmount
                            actualMinesAmount.also {
                                seekbarMinesAmount.progress = it
                                textViewMinesPercentage.text = minesPercentageAsString(it, currentFieldSize)
                            }
                            currentColumnsAmount = progress
                        }
                    }
                )
            }

            seekbarRowsAmount.apply {
                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            prevProportionMinesActualMax = calcPrevProportionMinesActualMax(
                                seekbarMinesAmount.progress,
                                seekbarMinesAmount.max
                            )
                        }

                        override fun onProgressChanged(seekBar: SeekBar?, updatedProgress: Int, fromUser: Boolean) {

                            if (updatedProgress <= ModelConst.rowsMinAmount) progress = ModelConst.rowsMinAmount

                            textViewRowsAmount.text = progress.toString()

                            currentFieldSize = calcCurrentFieldSize(progress, seekbarColumnsAmount.progress)
                                .also {
                                    textViewFieldSize.text = it.toString()
                                }

                            maxMinesAmount = calcMaxMinesAmount(currentFieldSize)

                            actualMinesAmount = calcActualMinesAmount(maxMinesAmount, prevProportionMinesActualMax)
                                .also {
                                    textViewMinesAmount.text = it.toString()
                                }
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            seekbarMinesAmount.max = maxMinesAmount
                            actualMinesAmount.also {
                                seekbarMinesAmount.progress = it
                                textViewMinesPercentage.text = minesPercentageAsString(it, currentFieldSize)
                            }
                            currentRowsAmount = progress
                        }
                    }
                )
            }

            seekbarMinesAmount.apply {
                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {

                        override fun onStartTrackingTouch(seekBar: SeekBar?) { }

                        override fun onProgressChanged(seekBar: SeekBar?, updatedProgress: Int, fromUser: Boolean) {

                            if (updatedProgress <= ModelConst.minesMinAmount) progress = ModelConst.minesMinAmount

                            progress.also {
                                textViewMinesAmount.text = it.toString()
                                textViewMinesPercentage.text = minesPercentageAsString(it, currentFieldSize)
                            }
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            actualMinesAmount = progress
                        }
                    }
                )
            }

            actualMinesAmount

            show()
            window?.attributes = layoutParams
        }.also {
            it.findViewById<View>(R.id.button_apply_changes).setOnClickListener(this)
            it.findViewById<View>(R.id.button_discard_changes).setOnClickListener(this)
        }
    }

    internal fun getResultAndDismiss(): GameOptions {
        val result = GameOptions(
            currentColumnsAmount,
            currentRowsAmount,
            actualMinesAmount
        )
        dismiss()
        return result
    }

    fun dismiss() {
        dialog.dismiss()
        gameOptions = null
        currentColumnsAmount = 0
        currentRowsAmount = 0
        actualMinesAmount = 0
    }

    private fun calcPrevProportionMinesActualMax(actualMinesAmount: Int, maxMinesAmount: Int) =
        actualMinesAmount.toFloat() / maxMinesAmount.toFloat()

    private fun calcCurrentFieldSize(columns: Int, rows: Int) = columns * rows

    private fun calcMaxMinesAmount(fieldSize: Int) = (fieldSize * ModelConst.minesMaxProportion).toInt()

    private fun calcActualMinesAmount(maxMinesAmount: Int, prevProportion: Float) = (maxMinesAmount * prevProportion).toInt()

    private fun minesPercentageAsString(actualMinesAmount: Int, fieldSize: Int): String {
        fun Float.format(digits: Int) = "%.${digits}f".format(this)
        return "${(actualMinesAmount.toFloat() / fieldSize.toFloat() * 100).format(2)}%"
    }

    override fun onClick(v: View?) {
        clickListener.onClick(v)
    }
}