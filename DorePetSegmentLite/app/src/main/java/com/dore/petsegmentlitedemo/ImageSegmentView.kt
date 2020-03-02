package com.dore.petsegmentlitedemo

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dore.coreai.vision.DoreImage
import com.dore.petsegmentlite.PetSegmentLiteManager
import kotlinx.android.synthetic.main.image_segment_view.*
import java.io.IOException
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.dore.petsegmentlite.PetSegmentLiteListener


class ImageSegmentView : AppCompatActivity() {

    private val bEngine: PetSegmentLiteManager = PetSegmentLiteManager()
    private val GALLERY = 1

    private var isLibLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_segment_view)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        init_button_event()

        val lickeycode = getString(R.string.lic_key)

        bEngine.setPetSegmentLiteListener(object : PetSegmentLiteListener {

            override fun onSuccess(info: String) {

                isLibLoaded  = true
            }

            override fun onFailure(error: String) {

            }

            override fun onProgressUpdate(progress: String) {
                Log.d("Download Library : ", progress)
            }


        })
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
                    browse_img_preview.setImageBitmap(bitmap)
                    btnRun.isEnabled = true

                }
                catch (e: IOException) {
                    e.printStackTrace()

                }

            }

        }

    }

    private fun run_segment(){
        //if library loaded (wait for lib download and load @ first time, second time it will load from local )
        if(isLibLoaded) {

            val bm = (browse_img_preview.getDrawable() as BitmapDrawable).bitmap
            val dimage = DoreImage.fromBitmap(bm)
            var result = bEngine.run(dimage)

            runOnUiThread {
                browse_img_preview.setImageBitmap(result?.transparentImage)
                btnRun.isEnabled = false
            }

        }


    }



}