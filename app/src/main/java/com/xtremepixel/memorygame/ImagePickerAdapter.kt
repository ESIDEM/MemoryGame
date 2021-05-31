package com.xtremepixel.memorygame

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.xtremepixel.memorygame.models.BoardSize
import kotlin.math.min

class ImagePickerAdapter(private val context: Context, private val chosenImageUri: List<Uri>,
                         private val boardSize: BoardSize, private val imageClickListener: ImageClickListener)
    : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {


    interface ImageClickListener{
        fun onPlaceHolderClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image,parent,false)
        val cardWith = parent.width / boardSize.getWidth()
        val cardHight = parent.height / boardSize.getHeight()
        val cardSideLength = min(cardWith, cardHight)
        val layoutParams = view.findViewById<ImageView>(R.id.custom_image).layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength

        return  ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position<chosenImageUri.size){

            holder.bind(chosenImageUri[position])
        }else{
            holder.bind()
        }
    }

    override fun getItemCount() = boardSize.getNumPairs()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val customImage = itemView.findViewById<ImageView>(R.id.custom_image)
        fun bind() {
            customImage.setOnClickListener {
                imageClickListener.onPlaceHolderClicked()
            }
        }
        fun bind(uri:Uri) {
            customImage.setImageURI(uri)
            customImage.setOnClickListener(null)
        }

    }
}
