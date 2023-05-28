package com.dore.facesegmentdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dore.coreai.utils.FaceSegmentType
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.haircolor_change_view.*
import com.dore.facesegment.FaceSegmentManager
import java.util.*


class HairColorChangeView : AppCompatActivity() , TextureView.SurfaceTextureListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val REQUEST_CAMERA_PERMISSION = 0
    private var lensFacing = CameraX.LensFacing.FRONT
    private var imageCapture: ImageCapture? = null
    private val bEngine: FaceSegmentManager = FaceSegmentManager()
    private var cur_color: Int = Color.BLUE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        val lickeycode = getString(R.string.lic_key)


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
        //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_FACE, 0.4f, cur_color)  //For Face Segment
        val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_HAIR, 0.4f, cur_color)  //For HAIR Segment
        //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_BACKGROUND, 0.4f, cur_color)  //For Background Segment
        //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_BODY, 0.4f, cur_color)  //For Body Segment
        //val mask_0 = result!!.getFaceSegmentMask(FaceSegmentType.SEG_SKIN, 0.4f, cur_color)  //For Skin Segment
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