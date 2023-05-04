package com.dore.hairsegmentdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.haircolor_change_view.*
import com.dore.hairsegment.HairSegmentManager
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.util.*


class HairColorChangeView : AppCompatActivity() , TextureView.SurfaceTextureListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val REQUEST_CAMERA_PERMISSION = 0
    private var lensFacing = CameraX.LensFacing.FRONT
    private var imageCapture: ImageCapture? = null
    private val bEngine: HairSegmentManager = HairSegmentManager()
    private var cur_color: Int = Color.BLUE

    private val minThreshold = 0.4f  //change min Threshold From 0.1f to 0.6f for M1 model,  change min Threshold From 0.9 to 2.0f for M2 model
    private val maxThreshold = 1.01f //Only for M2 model - change max Threshold From 1.0f to 2.5f for M2 model
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        val lickeycode = getString(R.string.lic_key)

        bEngine.init_data(this,lickeycode, minThreshold, "M1") //model_code : M1|M2



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
        var textureView: TextureView = findViewById(R.id.bg_tex_view)
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
        val dimage =  DoreImage.fromBitmap(bg_tex_view.bitmap)

        var result = bEngine.run(dimage)
        val mask_0 = result!!.getMask(0.4f, cur_color)  //For M1 model
        //val mask_0 = result!!.getHairSegmentMask(0.4f, cur_color, maxThreshold)  //For M2 model
        val result_out = bg_tex_view.bitmap?.let { bEngine.clor_blend(it,mask_0,PorterDuff.Mode.ADD) }
        runOnUiThread {
            bg_outImg.setImageBitmap(result_out)
        }
    }

    public fun hairsegment_clor_blend(orgImg: Bitmap, maskImg : Bitmap?, imgBlendMode: PorterDuff.Mode) : Bitmap?{

        val mask = Bitmap.createScaledBitmap(maskImg as Bitmap, orgImg.width, orgImg.height, true)
        val result_out = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result_out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(imgBlendMode)
        mCanvas.drawBitmap(orgImg, 0f, 0f, null)
        mCanvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        return result_out
    }


}