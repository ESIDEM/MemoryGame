package com.xtremepixel.memorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.xtremepixel.memorygame.models.BoardSize
import kotlin.math.min

class MemoryGameAdapter(
    private var context: Context,
    private var cardCount: BoardSize,
    private var cardImage: List<Int>
) :
    RecyclerView.Adapter<MemoryGameAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var cardWidth = parent.width/cardCount.getWidth() - (2* MARGIN)
        var cardHeight = parent.height/cardCount.getHeight() - (2* MARGIN)
        var cardSideLength = min(cardWidth,cardHeight)
       var view = LayoutInflater.from(context).inflate(R.layout.card_layout,parent,false)

        var layoutParm = view.findViewById<View>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParm.width = cardSideLength
        layoutParm.height = cardSideLength
        layoutParm.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)

    return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cardCount.numCard

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val  imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)
        fun bind(position: Int) {
            imageButton.setImageResource(cardImage[position])
           imageButton.setOnClickListener {
               Log.i(TAG, "Clicked on position $position")
           }
        }
    }

    companion object{

        private const val MARGIN = 10
        private const val  TAG = "Memory Game Adapter"
    }
}
