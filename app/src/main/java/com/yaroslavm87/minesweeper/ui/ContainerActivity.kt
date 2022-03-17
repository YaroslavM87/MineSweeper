package com.yaroslavm87.minesweeper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.yaroslavm87.minesweeper.R
import com.yaroslavm87.minesweeper.ui.game.FragmentMineField
import com.yaroslavm87.minesweeper.ui.setup.FragmentSetup

class ContainerActivity :
    AppCompatActivity(),
    FragmentSetup.OnFragmentSetupEventListener,
    FragmentMineField.OnMineFieldFragmentEventListener
{

    private lateinit var navController: NavController
    private val className: String = "ContainerActivity"
    private val loggingEnabled: Boolean = true
    private var wasBackButtonPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)
        if (savedInstanceState == null) {
            initVariables()
            setToolbar()
        }
    }

    override fun onSetupFragmentEvent(event: FragmentSetup.SetupFragmentEvents) {
        log("onSetupFragmentEvent(): incoming event is $event")
        when(event) {
            FragmentSetup.SetupFragmentEvents.START_GAME_CALL -> {

                if (!wasBackButtonPressed) {
                    log("--- wasBackButtonPressed = $wasBackButtonPressed, navigate to next fragment")

                    navController.navigate(
                        R.id.action_setupFragment_to_mineFieldFragment,
                        null,
                        navOptions {
                            anim {
                                enter = android.R.animator.fade_in
                                exit = android.R.animator.fade_out
                            }
                        }
                    )
                }
                else {
                    log("--- wasBackButtonPressed = $wasBackButtonPressed, reset value to false")
                    wasBackButtonPressed = false
                }
            }
        }
    }

    override fun onMineFieldFragmentEvent(event: FragmentMineField.FragmentMineFieldEvents) {
        when (event) {
            FragmentMineField.FragmentMineFieldEvents.STOP_GAME_CALL ->
                navController.navigate(R.id.action_mineFieldFragment_to_setupFragment,
                    null,
                    navOptions {
                        anim {
                            enter = android.R.animator.fade_in
                            exit = android.R.animator.fade_out
                        }
                    }
                )
        }
    }

    private fun initVariables() {
        navController = (supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController
    }

    private fun setToolbar() {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = resources.getColor(R.color.grey_80)
        window.navigationBarColor = resources.getColor(R.color.grey_80)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        wasBackButtonPressed = true
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(UiConst.LOG_TAG, "$className.$message")
    }

}