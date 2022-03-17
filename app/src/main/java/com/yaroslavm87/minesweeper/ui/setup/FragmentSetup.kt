package com.yaroslavm87.minesweeper.ui.setup

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yaroslavm87.minesweeper.R
import com.yaroslavm87.minesweeper.model.Model
import com.yaroslavm87.minesweeper.ui.UiConst
import java.lang.IllegalArgumentException
import java.util.*

class FragmentSetup :
    Fragment(),
    View.OnClickListener,
    DialogEditGameOptions.OnDialogEditGameOptionsClickListener
{

    private lateinit var viewModel: ViewModelSetup
    private lateinit var containerActivityAsListenerForLocalEvents: OnFragmentSetupEventListener
    private lateinit var buttonStartGame: FloatingActionButton
    private lateinit var recyclerViewGameOptions: RecyclerView
    private lateinit var dialogEditGameOptions: DialogEditGameOptions
    private val className: String = "FragmentSetup"
    private val loggingEnabled: Boolean = true

    interface OnFragmentSetupEventListener {
        fun onSetupFragmentEvent(event: SetupFragmentEvents) {}
    }

    enum class SetupFragmentEvents {
        START_GAME_CALL
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        log("onAttach()")
        context.also {
            if (it is OnFragmentSetupEventListener) containerActivityAsListenerForLocalEvents = it
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        log("onCreateView()")
        return inflater.inflate(R.layout.setup_fragment_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        log("onViewCreated()")
        super.onViewCreated(view, savedInstanceState)

        viewModel = initViewModel().also {
            setSetViewModelWithScreenParams(it)
        }

        initViewComponents(view)

        if (wasLastGameNotFinished()) {
            resetCurrentGameStateDefinition()
            restoreAndNavigateToUnfinishedGame()
        }
    }

    private fun initViewModel(): ViewModelSetup {
        return ViewModelProvider(this)[ViewModelSetup::class.java]
    }

    private fun wasLastGameNotFinished(): Boolean {
        log("lastGameWasNotFinished()")
       // return false
        return (requireActivity()
            .getPreferences(Context.MODE_PRIVATE)
            .getString("gameState", "") == Model.State.GAME_IS_ONGOING.toString())
    }

    private fun resetCurrentGameStateDefinition() {
        requireActivity().getPreferences(Context.MODE_PRIVATE).edit()?.apply {
            putString("gameState", "")
            apply()
        }
    }

    private fun restoreAndNavigateToUnfinishedGame() {
        log("restoreAndNavigateToUnfinishedGame()")
        viewModel.restoreGameState(requireContext())
        containerActivityAsListenerForLocalEvents.onSetupFragmentEvent(SetupFragmentEvents.START_GAME_CALL)
    }

    private fun initViewComponents(view: View) {
        log("initViewComponents()")

        buttonStartGame = view.findViewWithTag<FloatingActionButton>("setup_fragment_fab_start_game").also {
            it.isEnabled = false
            it.setOnClickListener(this)
        }

        initRecyclerViewForGameOptions(view)

        dialogEditGameOptions = DialogEditGameOptions(requireContext(), this)
    }

    private fun initRecyclerViewForGameOptions(view: View) {
        log("initRecyclerViewForGameOptions()")

        recyclerViewGameOptions = view.findViewWithTag("setup_fragment_recycler_view_game_options")

        recyclerViewGameOptions.apply {

            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )

            viewModel.restoreGameOptionsList(requireContext())

            adapter = AdapterRecyclerViewGameOptions(
                Collections.unmodifiableList(viewModel.gameOptionsList),
                R.layout.setup_fragment_game_options_item,
                R.layout.setup_fragment_game_options_add_item,
                object: AdapterRecyclerViewGameOptions.OnGameOptionsListItemClickListener {

                    private val unmarkedItem = R.color.game_options_item_list_unmarked
                    private val markedItem = R.color.game_options_item_list_marked
                    private var previouslyClicked: View? = null
                    private val newGameOptionItem = GameOptions(3, 3, 1)

                    override fun onItemClick(viewClicked: View, itemIndexInList: Int) {
                        log("AdapterRecyclerViewGameOptions.onItemClick()")
                        previouslyClicked?.setBackgroundResource(unmarkedItem)

                        if (previouslyClicked == viewClicked) {
                            viewClicked.setBackgroundResource(unmarkedItem)
                            buttonStartGame.isEnabled = false
                            previouslyClicked = null
                            viewModel.chosenGameOptionsIndex = null
                        }
                        else {
                            viewClicked.setBackgroundResource(markedItem)
                            buttonStartGame.isEnabled = true
                            previouslyClicked = viewClicked
                            viewModel.chosenGameOptionsIndex = itemIndexInList
                        }
                    }
                    override fun onAddItemClick() {
                        dialogEditGameOptions.showDialog(newGameOptionItem)
                    }
                    override fun onMenuItemClick(item: MenuItem, itemIndexInList: Int) {
                        when (item.title) {
                            resources.getString(R.string.edit) -> {
                                    dialogEditGameOptions.showDialog(viewModel.gameOptionsList[itemIndexInList])
                            }
                            resources.getString(R.string.delete) -> deleteGameOptionsItem(itemIndexInList)
                        }
                    }
                }
            )
        }
    }

    private fun setSetViewModelWithScreenParams(viewModel: ViewModelSetup) {
        log("setSetViewModelWithScreenParams()")
        val size = Point()
        val density = requireContext().resources.displayMetrics.density
        requireActivity().windowManager.defaultDisplay.getSize(size)

        val navBarHeightId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var y = size.y
        if (navBarHeightId > 0) {
            y -= (resources.getDimensionPixelSize(navBarHeightId) / density).toInt()
            log("--- height = $y")
        }

        val widthDp = resources.displayMetrics.run { widthPixels / density }
        val heightDp = resources.displayMetrics.run { heightPixels / density }
        log("--- widthDp = $widthDp, heightDp = $heightDp")

        viewModel.setScreenDimensions(size.x, y)
    }

    override fun onClick(v: View?) {
        log("onClick()")
        when (v?.tag) {
            "setup_fragment_fab_start_game" -> {
                viewModel.onSetupFragmentEvent(SetupFragmentEvents.START_GAME_CALL)
                containerActivityAsListenerForLocalEvents.onSetupFragmentEvent(SetupFragmentEvents.START_GAME_CALL)
            }

            "button_apply_changes" -> {
                val indexOfItemEdited = viewModel.gameOptionsList.indexOf(dialogEditGameOptions.gameOptions)
                if (indexOfItemEdited == -1) {
                    viewModel.gameOptionsList.add(dialogEditGameOptions.getResultAndDismiss())
                    recyclerViewGameOptions.adapter?.notifyItemInserted(viewModel.gameOptionsList.size - 1)
                } else {
                    viewModel.gameOptionsList[indexOfItemEdited] = dialogEditGameOptions.getResultAndDismiss()
                    recyclerViewGameOptions.adapter?.notifyItemChanged(indexOfItemEdited)
                }
            }

            "button_discard_changes" -> {
                dialogEditGameOptions.dismiss()
            }
        }
    }

    private fun deleteGameOptionsItem(itemIndexInList: Int) {
        viewModel.gameOptionsList.removeAt(itemIndexInList)
        recyclerViewGameOptions.adapter?.notifyItemRemoved(itemIndexInList)?: throw IllegalArgumentException("")
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
        viewModel.saveGameOptionsList(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy() call")
    }

    override fun onDetach() {
        super.onDetach()
        log("onDetach() call")
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(UiConst.LOG_TAG, "$className.$message")
    }

}