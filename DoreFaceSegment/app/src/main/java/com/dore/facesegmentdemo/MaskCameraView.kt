package com.dore.facesegmentdemo

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
import com.dore.coreai.utils.FaceSegmentType
import com.dore.facesegment.FaceSegmentManager


val permissions = arrayOf(android.Manifest.permission.CAMERA)


class MaskCameraView : AppCompatActivity() , TextureView.SurfaceTextureListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val REQUEST_CAMERA_PERMISSION = 0
    private var lensFacing = CameraX.LensFacing.FRONT
    private var imageCapture: ImageCapture? = null
    private val bEngine: FaceSegmentManager = FaceSegmentManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

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
              val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_FACE, 0.4f, Color.BLUE)  //For Face Segment
              //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_HAIR, 0.4f, Color.BLUE)  //For HAIR Segment
              //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_BACKGROUND, 0.4f, Color.BLUE)  //For background Segment
              //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_BODY, 0.4f, Color.BLUE)  //For Body Segment
              //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_SKIN, 0.4f, Color.BLUE)  //For Skin Segment
              outImg.setImageBitmap(mask_0)

          }
    }




}