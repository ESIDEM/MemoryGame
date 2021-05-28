package com.xtremepixel.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.models.MemoryCard
import com.xtremepixel.memorygame.models.MemoryGame
import com.xtremepixel.memorygame.utils.DEFAULT_ICONS

class MainActivity : AppCompatActivity() {

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter:MemoryGameAdapter
    private lateinit var movesText: TextView
     private lateinit var pairsText: TextView
     private lateinit var recyclerView:RecyclerView
     private lateinit var clrRoot:ConstraintLayout
     private var  boardsize : BoardSize = BoardSize.EASY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movesText = findViewById(R.id.moves)
        pairsText = findViewById(R.id.pairs)
        recyclerView = findViewById(R.id.recyclerViewBoard)
        clrRoot = findViewById(R.id.clRoot)

        memoryGame = MemoryGame(boardsize)

        recyclerView.layoutManager = GridLayoutManager(this, boardsize.getWidth())
        recyclerView.setHasFixedSize(true)
        adapter = MemoryGameAdapter(this, boardsize, memoryGame.cards, object : MemoryGameAdapter.CardClickedListener{
            override fun onCardClicked(position: Int) {

                updateGameWithFlip(position)
            }

        })

        recyclerView.adapter = adapter
    }

    private fun updateGameWithFlip(position: Int) {
        // Error Handling
        if (memoryGame.haveWonTheGame()){

            Snackbar.make(clrRoot, "You already won", Snackbar.LENGTH_LONG).show()
            return
        }

        if (memoryGame.isCardFaceUp(position)){
            Snackbar.make(clrRoot, "Invalid move", Snackbar.LENGTH_LONG).show()
            return
        }
        memoryGame.flipCard(position)
        adapter.notifyDataSetChanged()
    }
}