package com.xtremepixel.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.utils.DEFAULT_ICONS

class MainActivity : AppCompatActivity() {

     private lateinit var movesText: TextView
     private lateinit var pairsText: TextView
     private lateinit var recyclerView:RecyclerView
     private var  boardsize : BoardSize = BoardSize.HARD
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movesText = findViewById(R.id.moves)
        pairsText = findViewById(R.id.pairs)
        recyclerView = findViewById(R.id.recyclerViewBoard)

        val chosenImage: List<Int> = DEFAULT_ICONS.shuffled().take(boardsize.getNumPairs())
        val ramdomizedImage : List<Int> = (chosenImage + chosenImage).shuffled()

        recyclerView.layoutManager = GridLayoutManager(this, boardsize.getWidth())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = MemoryGameAdapter(this, boardsize, ramdomizedImage)
    }
}