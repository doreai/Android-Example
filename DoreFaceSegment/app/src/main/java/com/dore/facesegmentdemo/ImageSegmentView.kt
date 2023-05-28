package com.dore.facesegmentdemo

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dore.coreai.utils.FaceSegmentType
import com.dore.coreai.vision.DoreImage
import com.dore.facesegment.FaceSegmentManager
import kotlinx.android.synthetic.main.image_segment_view.*
import java.io.IOException


class ImageSegmentView : AppCompatActivity() {

    private val bEngine: FaceSegmentManager = FaceSegmentManager()
    private val GALLERY = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_segment_view)
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

            //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_FACE, 0.4f, Color.BLUE)  //For Face Segment
            val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_HAIR, 0.4f, Color.BLUE)  //For HAIR Segment
            //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_BACKGROUND, 0.4f, Color.BLUE)  //For background Segment
            //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_BODY, 0.4f, Color.BLUE)  //For Body Segment
            //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_SKIN, 0.4f, Color.BLUE)  //For Skin Segment

            val result_out = bEngine.clor_blend(bm,mask_0,PorterDuff.Mode.ADD)
            browse_img_preview.setImageBitmap(result_out)
            btnRun.isEnabled = false
        }


    }




}