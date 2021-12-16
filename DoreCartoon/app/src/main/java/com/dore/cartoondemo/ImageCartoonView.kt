package com.dore.cartoondemo


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.image_cartoon_view.*
import java.io.IOException
import android.graphics.drawable.BitmapDrawable
import android.view.View
import com.dore.cartoon.CartoonManager
import com.dore.cartoon.CartoonVariant
import com.dore.cartoon.DoreCartoonListener
import kotlin.concurrent.thread


class ImageCartoonView : AppCompatActivity() {

    private val bEngine: CartoonManager = CartoonManager()
    private val GALLERY = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_cartoon_view)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        init_button_event()

        progressBar.visibility = View.INVISIBLE

        val lickeycode = getString(R.string.lic_key)
        bEngine.init_data(this,lickeycode)


        bEngine.setDoreCartoonListener(object : DoreCartoonListener {

            override  fun onSuccess(info: String) {
                runOnUiThread {
                   progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onFailure(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onProgressUpdate(progress: String) {
                if(progress == "Started") {
                    runOnUiThread {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        })



    }


    override fun onResume() {
        super.onResume()




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

            run_cartoon_covert()
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

    private fun run_cartoon_covert(){

        val bm = (browse_img_preview.getDrawable() as BitmapDrawable).bitmap

        btnBrowse.isEnabled = false
        btnRun.isEnabled = false

        thread() {
             val result = bEngine.run(bm, CartoonVariant.Standard)!!
             update_result(result!!)
        }


    }

    fun update_result(outputImage:Bitmap) {
        runOnUiThread {
            browse_img_preview.setImageBitmap(outputImage)
            btnRun.isEnabled = false
            btnBrowse.isEnabled = true
        }

    }



}