package com.dore.hairsegmentlitedemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.mask_camera_view.*
import android.view.Menu
import android.view.MenuItem
import androidx.camera.lifecycle.ProcessCameraProvider
import com.dore.hairsegmentlite.HairSegmentLiteListener
import com.dore.hairsegmentlite.HairSegmentLiteManager
import kotlinx.android.synthetic.main.haircolor_change_view.*
import java.util.concurrent.Executors


val permissions = arrayOf(android.Manifest.permission.CAMERA)

class MaskCameraView : AppCompatActivity() ,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val REQUEST_CAMERA_PERMISSION = 0
    private var imageCapture: ImageCapture? = null
    private val bEngine: HairSegmentLiteManager = HairSegmentLiteManager()

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
        setContentView(R.layout.mask_camera_view)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder2.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer (Executors.newSingleThreadExecutor(), ImageAnalysis.Analyzer { image ->
                        runOnUiThread {
                            val bitmap: Bitmap = viewFinder2.bitmap!!
                            if (isLibLoaded) {
                                val dimage = DoreImage.fromBitmap(bitmap)
                                var result = bEngine.run(dimage)
                                outImg.setImageBitmap(result?.getMask(1f))  //alpha value 0.1 to 1
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