package com.dore.hairsegmentlitedemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dore.coreai.vision.DoreImage
import com.dore.hairsegmentlite.HairSegmentLiteListener
import kotlinx.android.synthetic.main.haircolor_change_view.*
import com.dore.hairsegmentlite.HairSegmentLiteManager
import java.util.*
import java.util.concurrent.Executors


class HairColorChangeView : AppCompatActivity() ,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val REQUEST_CAMERA_PERMISSION = 0
    private var imageCapture: ImageCapture? = null
    private val bEngine: HairSegmentLiteManager = HairSegmentLiteManager()
    private var cur_color: Int = Color.GREEN

    private var isLibLoaded = false





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        val lickeycode = getString(R.string.lic_key)

        bEngine.setHairSegmentLiteListener(object : HairSegmentLiteListener {

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


   private fun btnEvent() {
        btnChangeColor.setOnClickListener {

            val rnd = Random()
            cur_color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        }

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

    /**
     * Check if the app has all permissions
     */
    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request all permissions
     */
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {


        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    runOnUiThread {
                        bindCamera()
                    }
                } else {
                    onBackPressed()

                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /**
     * Bind the Camera to the lifecycle
     */
    private fun bindCamera(){
        setContentView(R.layout.haircolor_change_view)
        btnEvent()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder1.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer (Executors.newSingleThreadExecutor(), ImageAnalysis.Analyzer { image ->

                       runOnUiThread {
                            val bitmap: Bitmap = viewFinder1.bitmap!!
                            if (isLibLoaded) {
                                val dimage = DoreImage.fromBitmap(bitmap)
                                var result = bEngine.run(dimage)
                                val mask_0 = result?.getMask(0.4f, cur_color)
                                val result_out =
                                    bEngine.clor_blend(bitmap, mask_0, PorterDuff.Mode.DARKEN)

                                bg_outImg.setImageBitmap(result_out)
                                image.close();
                            }
                            bitmap?.recycle()
                       }

                    })
                }



            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()


                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(this))


    }

    private class frameAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            Log.d("Sandbox", "### Would analyze the image here ...")
        }
    }

    override fun onStart() {
        super.onStart()
        // Check and request permissions
        if (hasNoPermissions()) {
            requestPermission()
        }
        else{
            bindCamera()
        }


    }



}
