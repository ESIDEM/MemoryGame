package com.xtremepixel.memorygame

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.models.MemoryCard
import kotlin.math.min

class MemoryGameAdapter(
    private var context: Context,
    private var cardCount: BoardSize,
    private var card: List<MemoryCard>,
    private var onCardClickedListener: CardClickedListener
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
            val memoryCard = card[position]
            if (memoryCard.isFace){
                if (memoryCard.imageUrl!=null){
                    Picasso.get().load(memoryCard.imageUrl).into(imageButton)
                }else{
                    imageButton.setImageResource(memoryCard.identifier)
                }
            }else{
                imageButton.setImageResource( R.drawable.ic_launcher_background)
            }

            imageButton.alpha = if (memoryCard.isMatched) .4f else 1.0f

            val colorStateList : ColorStateList? = if (memoryCard.isMatched) ContextCompat.getColorStateList(context,R.color.color_gray) else null
            ViewCompat.setBackgroundTintList(imageButton,colorStateList)
           imageButton.setOnClickListener {
               Log.i(TAG, "Clicked on position $position")
               onCardClickedListener.onCardClicked(position)
           }
        }
    }

    companion object{

        private const val MARGIN = 10
        private const val  TAG = "Memory Game Adapter"
    }

    interface  CardClickedListener{

        fun onCardClicked(position: Int)
    }
}
