package com.xtremepixel.memorygame.models

data class MemoryCard (

    val identifier:Int,
    var imageUrl : String? =null,
            var isFace : Boolean = false,
            var isMatched : Boolean = false
)