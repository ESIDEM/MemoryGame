package com.xtremepixel.memorygame

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.models.GameImageList
import com.xtremepixel.memorygame.models.MemoryGame
import com.xtremepixel.memorygame.utils.EXTRA_BOARD_SIZE
import com.xtremepixel.memorygame.utils.EXTRA_GAME_NAME

class MainActivity : AppCompatActivity() {

    companion object {

        private const val CREAT_REQUEST_CODE = 10
    }

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter:MemoryGameAdapter
    private lateinit var movesText: TextView
     private lateinit var pairsText: TextView
     private lateinit var recyclerView:RecyclerView
     private lateinit var clrRoot:ConstraintLayout
     private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages : List<String>?=null
     private var  boardsize : BoardSize = BoardSize.EASY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movesText = findViewById(R.id.moves)
        pairsText = findViewById(R.id.pairs)
        recyclerView = findViewById(R.id.recyclerViewBoard)
        clrRoot = findViewById(R.id.clRoot)

       setUpBoard()
    }

    private fun updateGameWithFlip(position: Int) {
        // Error Handling
        if (memoryGame.haveWonTheGame()){

            Snackbar.make(clrRoot, "You already won", Snackbar.LENGTH_LONG).show()
            return
        }

        if (memoryGame.isCardFaceUp(position)){
            Snackbar.make(clrRoot, "Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }
       if ( memoryGame.flipCard(position)){
           val color = ArgbEvaluator().evaluate(
               memoryGame.numPairsFound.toFloat()/boardsize.getNumPairs(),
               ContextCompat.getColor(this, R.color.color_progress_none),
               ContextCompat.getColor(this,R.color.color_progress_full)
           ) as Int
           pairsText.setTextColor(color)
           pairsText.text = "Pairs: ${memoryGame.numPairsFound} / ${boardsize.getNumPairs()}"
           if (memoryGame.haveWonTheGame()){
               CommonConfetti.rainingConfetti(clrRoot, intArrayOf(Color.RED,Color.CYAN,Color.MAGENTA)).oneShot()
               Snackbar.make(clrRoot, "You won Congratulations.", Snackbar.LENGTH_LONG).show()
           }
       }
        movesText.text = " Moves: ${memoryGame.getNumberOfMoves()}"
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.menu_refresh -> {
                if (memoryGame.getNumberOfMoves()>0 && !memoryGame.haveWonTheGame()){
                    showAlertDiolue("Quit your current game",null, View.OnClickListener {
                        setUpBoard()
                    })
                }else {
                    setUpBoard()

                }

                return true
            }

            R.id.menu_size -> {

                showNewSizeDialog()

                return true
            }

            R.id.memu_custom_game ->{
                showCreateCustomDialog()

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreateCustomDialog() {
        val boarsSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize:RadioGroup = boarsSizeView.findViewById(R.id.radioGroup)

        showAlertDiolue("Create your own memory board", boarsSizeView, View.OnClickListener {

            val desiredBoardsize:BoardSize = when(radioGroupSize.checkedRadioButtonId){

                R.id.radioButton_easy -> BoardSize.EASY
                R.id.radioButton_medium -> BoardSize.MEDIUM

                else -> BoardSize.HARD
            }

            val createIntent = Intent(this, CreateActivity::class.java)
            createIntent.putExtra(EXTRA_BOARD_SIZE,desiredBoardsize )
            startActivityForResult(createIntent,CREAT_REQUEST_CODE)

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREAT_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val gameName = data?.getStringExtra(EXTRA_GAME_NAME)

            if (gameName == null){
                Toast.makeText(this, "Something went wrong",Toast.LENGTH_SHORT).show()
                return
            }

            downloadGame(gameName)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun downloadGame(customName: String) {
        db.collection("games").document(customName).get().addOnSuccessListener {
            val gameImageList =  it.toObject(GameImageList::class.java)
            if (gameImageList?.images == null){
                Toast.makeText(this, "Invalid image data", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
        }
            val numCards = gameImageList.images.size * 2
            boardsize = BoardSize.getByValue(numCards)
            gameName = customName
            customGameImages = gameImageList.images
            setUpBoard()
        }.addOnFailureListener {

        }
    }

    private fun showNewSizeDialog() {
        val boarsSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize:RadioGroup = boarsSizeView.findViewById(R.id.radioGroup)
        when(boardsize){
            BoardSize.EASY -> radioGroupSize.check(R.id.radioButton_easy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.radioButton_medium)
            BoardSize.HARD -> radioGroupSize.check(R.id.radioButton_hard)
        }
        showAlertDiolue("Choose Board Size", boarsSizeView, View.OnClickListener {

            boardsize = when(radioGroupSize.checkedRadioButtonId){

                R.id.radioButton_easy -> BoardSize.EASY
                R.id.radioButton_medium -> BoardSize.MEDIUM

                else -> BoardSize.HARD
            }

            setUpBoard()

        })
    }

    private fun showAlertDiolue(title:String, view: View?,positiveClickLister:View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){_,_ ->
                positiveClickLister.onClick(null)

            }.show()
    }

    fun setUpBoard(){

        when(boardsize){
            BoardSize.EASY -> {
                movesText.text = " Easy: 4 x 2"
                pairsText.text = " Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                movesText.text = " Medium: 6 x 3"
                pairsText.text = " Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                movesText.text = " Hard: 6 x 4"
                pairsText.text = " Pairs: 0 / 12"
            }
        }
        memoryGame = MemoryGame(boardsize,customGameImages)

        recyclerView.layoutManager = GridLayoutManager(this, boardsize.getWidth())
        recyclerView.setHasFixedSize(true)
        adapter = MemoryGameAdapter(this, boardsize, memoryGame.cards, object : MemoryGameAdapter.CardClickedListener{
            override fun onCardClicked(position: Int) {

                updateGameWithFlip(position)
            }

        })

        recyclerView.adapter = adapter
        pairsText.text = "Pairs: ${memoryGame.numPairsFound} / ${boardsize.getNumPairs()}"
        pairsText.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
    }
}