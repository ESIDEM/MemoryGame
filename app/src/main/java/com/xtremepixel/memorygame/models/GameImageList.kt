package com.xtremepixel.memorygame.models

import com.google.firebase.firestore.PropertyName

data class GameImageList(
    @PropertyName("image") val images:List<String>? = null
)