package com.dore.emotionrecognitiondemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.image_emotion_view.*
import java.io.IOException
import android.graphics.drawable.BitmapDrawable
import com.dore.emotionrecognition.EmotionRecognitionManager




class EmotionImageView : AppCompatActivity() {

    private val bEngine: EmotionRecognitionManager = EmotionRecognitionManager()
    private val GALLERY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_emotion_view)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        init_button_event()

        val lickeycode = getString(R.string.lic_key)
        bEngine.init_data(this,lickeycode)


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun init_button_event() {

        btnRun.isEnabled = false


        btnBrowse.setOnClickListener {

            choosePhotoFromGallary()
        }

        btnRun.setOnClickListener {

            run_segment()
        }

    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/
        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                    browse_img_preview.setImageBitmap(getResizedBitmap(bitmap,640,720))
                    btnRun.isEnabled = true

                }
                catch (e: IOException) {
                    e.printStackTrace()

                }

            }

        }

    }

    private fun run_segment(){

        runOnUiThread {
            lbImgResultTxt.text = ""
        }


        val bm = (browse_img_preview.getDrawable() as BitmapDrawable).bitmap

        val rlist = bEngine.detectFace(bm)

        if(rlist.count() > 0){
            val croppedBmp = bEngine.cropImage(bm,rlist[0])
            val bwImg = bEngine.convertToGrayScale(croppedBmp)
            val dimage = DoreImage.fromBitmap(bwImg)
            var result = bEngine.run(dimage)

            runOnUiThread {
                lbImgResultTxt.text = result?.getClsName()
            }
        }else{
            runOnUiThread{
                lbImgResultTxt.text = "Face Not Found"
            }
        }



    }

    private  fun getResizedBitmap(bm: Bitmap, newWidth:Int, newHeight:Int):Bitmap {
        val width = bm.getWidth()
        val height = bm.getHeight()
        val scaleWidth = (newWidth.toFloat()) / width
        val scaleHeight = (newHeight.toFloat()) / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)
        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }



}