package com.dore.hairsegmentdemo

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dore.coreai.vision.DoreImage
import com.dore.coreai.vision.DoreVisionSegmentResult
import com.dore.hairsegment.HairSegmentManager
import kotlinx.android.synthetic.main.haircolor_change_view.*
import kotlinx.android.synthetic.main.image_segment_view.*
import java.io.IOException


class ImageSegmentView : AppCompatActivity() {

    private val bEngine: HairSegmentManager = HairSegmentManager()
    private val GALLERY = 1

    private val minThreshold = 0.4f  //change min Threshold From 0.1f to 0.6f for M1 model,  change min Threshold From 0.9 to 2.0f for M2 model
    private val maxThreshold = 1.01f //Only for M2 model - change max Threshold From 1.0f to 2.5f for M2 model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_segment_view)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        init_button_event()

        val lickeycode = getString(R.string.lic_key)
        bEngine.init_data(this,lickeycode, minThreshold, "M1")   //model_code : M1|M2


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

        val bm = (browse_img_preview.getDrawable() as BitmapDrawable).bitmap
        val dimage =  DoreImage.fromBitmap(bm)
        var result = bEngine.run(dimage)

        runOnUiThread {
            val mask_0 = result!!.getMask(0.4f, Color.BLUE)  //For M2 model
            //val mask_0 = result!!.getHairSegmentMask(0.4f, Color.BLUE, maxThreshold)  //For M2 model
            val result_out = bEngine.clor_blend(bm,mask_0,PorterDuff.Mode.ADD)
            browse_img_preview.setImageBitmap(result_out)
            btnRun.isEnabled = false
        }


    }




}