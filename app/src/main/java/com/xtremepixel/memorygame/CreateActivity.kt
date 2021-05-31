package com.xtremepixel.memorygame

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xtremepixel.memorygame.models.BoardSize
import com.xtremepixel.memorygame.utils.*
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    companion object{
        private const val CHOOSER_COCE = 50
        private const val READ_PHOTO_CODE = 51
        private const val READ_PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MAX_GAME_NAME = 14
        private const val MIN_GAME_NAME = 4
    }

    private lateinit var boardSize: BoardSize
    private  var numOfImageRequired = -1
    private lateinit var image_picker_rv:RecyclerView
    private lateinit var game_name_edit:EditText
    private lateinit var saveBt:Button
    private lateinit var progressBar:ProgressBar
    private val chosenImageUri = mutableListOf<Uri>()
    private lateinit var imagePickerAdapter: ImagePickerAdapter
    private val storage = Firebase.storage
    private val db = Firebase.firestore
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
        progressBar = findViewById(R.id.uploadProgress)

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

        saveBt.setOnClickListener {
            saveDataToFirebase()
        }
        game_name_edit.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME))
//        game_name_edit.addTextChangedListener(object : TextWatcher{
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                game_name_edit.isEnabled = shouldEnableSaveButton()
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//
//            }
//
//        })
    }

    private fun saveDataToFirebase() {
        saveBt.isEnabled = false
        val game_name = game_name_edit.text.toString()
        db.collection("games").document(game_name).get().addOnSuccessListener {
            if (it != null && it.data != null){
                AlertDialog.Builder(this)
                    .setTitle("Name Taken")
                    .setMessage("A game already exit with the name $game_name Please choose another name")
                    .show()
                saveBt.isEnabled = true
            }else{
                handleImageUploading(game_name)
            }
        }.addOnFailureListener {
                saveBt.isEnabled = true
        }

    }

    private fun handleImageUploading(game_name:String) {
        progressBar.visibility = View.VISIBLE
        var didEncounterError = false
        var uploadedImageUrl = mutableListOf<String>()
        for ((index , photoUri) in chosenImageUri.withIndex()){

            val imageByteArray = getImageByteArray(photoUri)
            val filePath = "image/$game_name/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference = storage.reference.child(filePath)
            photoReference.putBytes(imageByteArray)
                .continueWithTask {
                    photoReference.downloadUrl
                }.addOnCompleteListener {
                    if (!it.isSuccessful){
                        Toast.makeText(this, "There was an error uploading your photos",Toast.LENGTH_SHORT).show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }

                    if (didEncounterError){
                        progressBar.visibility = View.GONE
                        return@addOnCompleteListener
                    }

                    val downloadUrl = it.result.toString()
                    uploadedImageUrl.add(downloadUrl)
                    progressBar.progress = uploadedImageUrl.size * 100 / chosenImageUri.size
                    if (uploadedImageUrl.size==chosenImageUri.size){
                        handleUploadedImage(game_name,uploadedImageUrl)
                    }
                }

        }
    }

    private fun handleUploadedImage(gameName: String, imageUrl: MutableList<String>) {
        db.collection("games").document(gameName)
            .set(mapOf("image" to imageUrl))
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                if (!it.isSuccessful){
                    Toast.makeText(this,"An error occoured",Toast.LENGTH_SHORT).show()
                }else {

                    Toast.makeText(this, "$gameName Game created", Toast.LENGTH_SHORT).show()
                    AlertDialog.Builder(this)
                        .setTitle("Upload completed!")
                        .setPositiveButton("Ok"){ _,_ ->
                            val resultData = Intent()
                            resultData.putExtra(EXTRA_GAME_NAME, gameName)
                            setResult(Activity.RESULT_OK,resultData)
                            finish()
                        }.show()
                }
            }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {

        val originalBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val sourse = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(sourse)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
        }
        val scaleBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        val outputStream = ByteArrayOutputStream()
            scaleBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
        return outputStream.toByteArray()

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

        if (chosenImageUri.size != numOfImageRequired){
            return false
        }

        if (game_name_edit.text.isBlank() || game_name_edit.text.length < MIN_GAME_NAME){
            return false
        }

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