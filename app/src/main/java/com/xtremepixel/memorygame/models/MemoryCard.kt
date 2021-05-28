package com.xtremepixel.memorygame.models

data class MemoryCard (

    val identifier:Int,
            var isFace : Boolean = false,
            var isMatched : Boolean = false
)