package com.xtremepixel.memorygame.models

import com.xtremepixel.memorygame.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize) {

    private var indexOfSingleSelected : Int? = null


    fun flipCard(position: Int):Boolean {
        val cards = cards[position]
        var foundMatch = false
        numCardFlip++

        if( indexOfSingleSelected ==null){

            restoreCards()
            indexOfSingleSelected = position
        }else {
            foundMatch = checkForMatch(indexOfSingleSelected!!,position)
            indexOfSingleSelected = null
        }
        cards.isFace = !cards.isFace

        return foundMatch
    }

    private fun checkForMatch(postition1: Int, position2: Int): Boolean {

        if (cards[postition1].identifier != cards[position2].identifier){

            return false
        }
        cards[postition1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCards() {
        for (card:MemoryCard in cards){

            if (!card.isMatched){
                card.isFace = false
            }
        }
    }

    fun haveWonTheGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(postition1: Int): Boolean {

        return cards[postition1].isFace

    }

    fun getNumberOfMoves(): Int {
        return  numCardFlip/2
    }

    val cards: List<MemoryCard>
    var numPairsFound = 0
    private var numCardFlip =0

    init {
        val chosenImage: List<Int> = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val ramdomizedImage : List<Int> = (chosenImage + chosenImage).shuffled()
        cards = ramdomizedImage.map { MemoryCard(it) }
    }
}