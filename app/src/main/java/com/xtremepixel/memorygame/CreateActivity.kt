package com.xtremepixel.memorygame

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.utils.EXTRA_BOARD_SIZE
import com.xtremepixel.memorygame.utils.isPersissionGranted
import com.xtremepixel.memorygame.utils.requestPermision

class CreateActivity : AppCompatActivity() {

    companion object{
        private const val CHOOSER_COCE = 50
        private const val READ_PHOTO_CODE = 51
        private const val READ_PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var boardSize: BoardSize
    private  var numOfImageRequired = -1
    private lateinit var image_picker_rv:RecyclerView
    private lateinit var game_name_edit:EditText
    private lateinit var saveBt:Button
    private val chosenImageUri = mutableListOf<Uri>()
    private lateinit var imagePickerAdapter: ImagePickerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

         boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        numOfImageRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numOfImageRequired)"
        image_picker_rv = findViewById(R.id.rv_image_picker)
        game_name_edit = findViewById(R.id.edit_text_game_name)
        saveBt = findViewById(R.id.button_save)

        imagePickerAdapter = ImagePickerAdapter(this,chosenImageUri, boardSize,object : ImagePickerAdapter.ImageClickListener{
            override fun onPlaceHolderClicked() {

                if (isPersissionGranted(this@CreateActivity,READ_PHOTO_PERMISSION)){
                    launchPhotoIntent()
                }else{
                    requestPermision(this@CreateActivity, READ_PHOTO_PERMISSION, READ_PHOTO_CODE)
                }

            }



        })
        image_picker_rv.adapter = imagePickerAdapter
        image_picker_rv.setHasFixedSize(true)
        image_picker_rv.layoutManager = GridLayoutManager(this, boardSize.getWidth())

    }

    private fun launchPhotoIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Choose Photo"),CHOOSER_COCE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != CHOOSER_COCE || resultCode != Activity.RESULT_OK || data == null){
            Toast.makeText(this, "No data received, Please make sure you selected an image",Toast.LENGTH_LONG).show()
            return
        }

        val selected_uri = data.data
        val clipData = data.clipData
        if (clipData !=null){
            Toast.makeText(this, "${clipData.itemCount}",Toast.LENGTH_SHORT).show()
            for (i in 0 until clipData.itemCount){
                val clipItem = clipData.getItemAt(i)
                if (chosenImageUri.size<numOfImageRequired){
                    chosenImageUri.add(clipItem.uri)
                }
            }
        } else{
            if (selected_uri!=null){
                chosenImageUri.add(selected_uri)
            }
        }
        imagePickerAdapter.notifyDataSetChanged()
        supportActionBar?.title = " Choose pic (${chosenImageUri.size} / $numOfImageRequired)"
        saveBt.isEnabled = shouldEnableSaveButton()
    }

    private fun shouldEnableSaveButton(): Boolean {

        return true

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_PHOTO_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                launchPhotoIntent()
            }else{
                Toast.makeText(this,"This App needs permission to select photo from your device",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){

            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}