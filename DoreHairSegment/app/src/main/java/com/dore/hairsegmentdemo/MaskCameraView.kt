package com.dore.hairsegmentdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.mask_camera_view.*
import android.view.Menu
import android.view.MenuItem
import com.dore.hairsegment.HairSegmentManager


val permissions = arrayOf(android.Manifest.permission.CAMERA)


class MaskCameraView : AppCompatActivity() , TextureView.SurfaceTextureListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val REQUEST_CAMERA_PERMISSION = 0
    private var lensFacing = CameraX.LensFacing.FRONT
    private var imageCapture: ImageCapture? = null
    private val bEngine: HairSegmentManager = HairSegmentManager()

    private val minThreshold = 0.4f  //change min Threshold From 0.1f to 0.6f for M1 model,  change min Threshold From 0.9 to 2.0f for M2 model
    private val maxThreshold = 1.01f //Only for M2 model - change max Threshold From 1.0f to 2.5f for M2 model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        val lickeycode = getString(R.string.lic_key)
        bEngine.init_data(this,lickeycode, minThreshold, "M1") //model_code : M1|M2

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

        CameraX.unbindAll()

        // Preview config for the camera
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(lensFacing)
            .build()

        val preview = Preview(previewConfig)

         //Image capture config which controls the Flash and Lens
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .setTargetRotation(windowManager.defaultDisplay.rotation)
            .setLensFacing(lensFacing)
            .setFlashMode(FlashMode.ON)
            .build()

        imageCapture = ImageCapture(imageCaptureConfig)

        // The view that displays the preview
        val textureView: TextureView = findViewById(R.id.tex_view)
        textureView.surfaceTextureListener = this

        // Handles the output data of the camera
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            // Displays the camera image in our preview view
            textureView.setSurfaceTexture(previewOutput.surfaceTexture)
        }

        // Bind the camera to the lifecycle
        CameraX.bindToLifecycle(this as LifecycleOwner, imageCapture, preview)
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

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        // Perform action when surfaceTexture is available. For example, start camera etc.

    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        // Ignored, Camera does all the work for us
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        // Perform action when surfaceTexture is destroyed. For example, stop camera, release resources etc.
        return true
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
          val dimage =  DoreImage.fromBitmap(tex_view.bitmap)
          var result = bEngine.run(dimage)

          runOnUiThread {
              outImg.setImageBitmap(result?.getMask(1f))  //For m1 model
            //outImg.setImageBitmap(result?.getHairSegmentMask(1f,maxThreshold))  //For m2 model
          }
    }




}