package com.xtremepixel.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.utils.EXTRA_BOARD_SIZE

class CreateActivity : AppCompatActivity() {

    private lateinit var boardSize: BoardSize
    private  var numOfImageRequired = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

         boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        numOfImageRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numOfImageRequired)"

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){

            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}