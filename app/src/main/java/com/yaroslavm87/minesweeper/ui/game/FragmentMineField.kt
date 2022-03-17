package com.yaroslavm87.minesweeper.ui.game

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import com.yaroslavm87.minesweeper.model.ModelConst
import com.yaroslavm87.minesweeper.model.Model
import com.yaroslavm87.minesweeper.ui.LiveDataProvider

class FragmentMineField :
    Fragment(),
    SurfaceViewMineField.OnSurfaceViewTouchEventListener,
    SurfaceHolder.Callback
{

    interface OnMineFieldFragmentEventListener {
        fun onMineFieldFragmentEvent(event: FragmentMineFieldEvents) {}
    }

    enum class FragmentMineFieldEvents {
        STOP_GAME_CALL
    }

    private lateinit var viewModel: ViewModelMineField
    private lateinit var listenerForLocalEvents: OnMineFieldFragmentEventListener
    private lateinit var surfaceView: SurfaceViewMineField
    private val className: String = "FragmentMineField"
    private val loggingEnabled = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        log("onAttach()")
        if (context is OnMineFieldFragmentEventListener) context.also { listenerForLocalEvents = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate()")
        viewModel = ViewModelProvider(this)[ViewModelMineField::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        log("onCreateView()")

        val columnsAmount = LiveDataProvider.mineFieldColumnsAmount.value as Int
        val rowsAmount = LiveDataProvider.mineFieldRowsAmount.value as Int

        log("onCreateView(): columnsAmount = $columnsAmount, rowsAmount = $rowsAmount")

        surfaceView = SurfaceViewMineField(
            this,
            LiveDataProvider.screenWidth.value as Int,
            LiveDataProvider.screenHeight.value as Int,
            columnsAmount,
            rowsAmount
        ).also { sv ->
            sv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            sv.onSurfaceViewTouchEventListener = this
        }

        return surfaceView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("onViewCreated()")
    }

    override fun onSurfaceViewMineFieldClick(cellTouchedIndex: Int) {
        viewModel.exploreCell(cellTouchedIndex)
    }

    override fun onSurfaceViewMineFieldLongClick(cellTouchedIndex: Int) {
        viewModel.markCellContainsMine(cellTouchedIndex)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        log("surfaceCreated(): draw mine field for first time")
        surfaceView.drawMineField(viewModel.mineFieldValues.toTypedArray())
        subscribeUi()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceView.onSurfaceDestroyed()
    }

    private fun subscribeUi() {
        LiveDataProvider.viewModelMineFieldCommands.observe(viewLifecycleOwner) { viewModelMineFieldCommand ->
            log("subscribeUi(): view model command = $viewModelMineFieldCommand")
            if (viewModelMineFieldCommand != null)
                executeAppropriateAction(viewModelMineFieldCommand)
        }
    }

    private fun executeAppropriateAction(viewModelMineFieldCommand: ViewModelMineFieldCommands) {
        log("executeAppropriateAction()")

        when (viewModelMineFieldCommand) {

            ViewModelMineFieldCommands.REDRAW_MINE_FIELD -> {
                log("--- redraw surface view")
                surfaceView.drawMineField(viewModel.mineFieldValues.toTypedArray())
            }

            ViewModelMineFieldCommands.FINISH_GAME_WITH_SUCCESS -> {
                log("--- finish with success")
                Toast.makeText(requireContext(), "Congratulations!", Toast.LENGTH_LONG).show()
            }

            ViewModelMineFieldCommands.FINISH_GAME_WITH_FAIL -> {
                log("--- finish with fail")
                Toast.makeText(requireContext(), "You missed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun unsubscribeUi() {
        with(LiveDataProvider) {
            viewModelMineFieldCommands.removeObservers(viewLifecycleOwner)
        }
    }

    override fun onStart() {
        super.onStart()
        log("onStart()")

    }

    override fun onResume() {
        super.onResume()
        log("onResume()")
    }

    override fun onPause() {
        super.onPause()
        log("onPause()")
    }

    override fun onStop() {
        super.onStop()
        log("onStop()")
        unsubscribeUi()
        saveCurrentGameStateDefinition()
        if (isCurrentGameNotFinished()) { saveCurrentGameStateValues() }
    }

    private fun isCurrentGameNotFinished(): Boolean {
        log("isCurrentGameNotFinished()")
        return viewModel.getGameState() == Model.State.GAME_IS_ONGOING
    }

    private fun saveCurrentGameStateDefinition() {
        requireActivity().getPreferences(Context.MODE_PRIVATE).edit()?.apply {
            putString("gameState", viewModel.getGameState().toString())
            apply()
        }
    }

    private fun saveCurrentGameStateValues() {
        viewModel.saveGameState(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy()")
    }

    override fun onDetach() {
        super.onDetach()
        log("onDetach()")
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }

}
