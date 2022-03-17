package com.yaroslavm87.minesweeper.ui.setup

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yaroslavm87.minesweeper.R

class AdapterRecyclerViewGameOptions(
    private val itemList: List<GameOptions>,
    private val itemLayoutId: Int,
    private val addItemLayoutId: Int,
    private val onGameOptionsListItemClickListener: OnGameOptionsListItemClickListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnGameOptionsListItemClickListener {
        fun onItemClick(viewClicked: View, itemIndexInList: Int)
        fun onAddItemClick()
        fun onMenuItemClick(item: MenuItem, itemIndexInList: Int)
    }

    inner class GameOptionsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener
    {
        val columnsAmount: TextView = itemView.findViewWithTag("game_options_item_columns_amount")
        val rowsAmount: TextView = itemView.findViewWithTag("game_options_item_rows_amount")
        val minesAmount: TextView = itemView.findViewWithTag("game_options_item_mines_amount")
        val cellsAmount: TextView = itemView.findViewWithTag("game_options_item_field_size")
        val minesPercentage: TextView = itemView.findViewWithTag("game_options_item_mines_percentage")

        init {
            itemView.findViewWithTag<CardView>("setup_fragment_game_options_item").setOnClickListener(this)
            itemView.findViewWithTag<ImageButton>("game_options_item_button_more").setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v != null) {
                when (v.tag) {

                    "game_options_item_button_more" -> onMoreButtonClick(v, adapterPosition)

                    "setup_fragment_game_options_item" -> onGameOptionsListItemClickListener.onItemClick(v, adapterPosition)
                }
            }
        }
    }

    inner class AddItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener
    {

        init {
            itemView.findViewWithTag<FloatingActionButton>("game_options_add_item").setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v != null) {
                when (v.tag) {
                    "game_options_add_item" -> onGameOptionsListItemClickListener.onAddItemClick()
                }
            }
        }
    }

    private lateinit var context: Context
    private val viewGameOptions = 1
    private val viewAddNew = 0


    override fun getItemViewType(position: Int): Int {
        return if (position == itemList.size) viewAddNew else viewGameOptions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        return if (viewType == viewGameOptions) {
            val view: View = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
            GameOptionsViewHolder(view)

        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(addItemLayoutId, parent, false)
            AddItemViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GameOptionsViewHolder) {
            itemList[position].apply {
                holder.columnsAmount.text = columnsAmount.toString()
                holder.rowsAmount.text = rowsAmount.toString()
                holder.minesAmount.text = minesAmount.toString()
                holder.cellsAmount.text = cellsAmount.toString()
                holder.minesPercentage.apply {
                    val s = "${ minesPercentage }%"
                    text = s
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size + 1
    }

    private fun onMoreButtonClick(view: View, itemIndex: Int) {
        PopupMenu(context, view).apply {
            setOnMenuItemClickListener { item ->
                onGameOptionsListItemClickListener.onMenuItemClick(item, itemIndex)
                true
            }
            inflate(R.menu.game_options_item_menu)
            show()
        }
    }

}