package com.dore.doredeepstyledemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dore.coreai.vision.DoreImage
import com.dore.deepstyle.DeepStyleListener
import com.dore.deepstyle.DeepStyleManager
import kotlinx.android.synthetic.main.reamtime_deepstyle_view.*
import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView



val permissions = arrayOf(android.Manifest.permission.CAMERA)

fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(getAdapterPosition(), getItemViewType())
    }
    return this
}

private class adapter(internal var context: Context, internal var mData: List<String>) :
    RecyclerView.Adapter<adapter.myViewHolder>() {

    private var rView : RealTimeDeepStyleView = RealTimeDeepStyleView()

    public fun setRview(sView : RealTimeDeepStyleView){
        this.rView = sView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): adapter.myViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.style_listitem, parent, false)
        //return myViewHolder(view)
        return myViewHolder(view).listen { pos, type ->
            val cur_txt =  mData[pos]


            this.rView.load_style(cur_txt)
           // bEngine.load_style(view.lbstyleItem.text.toString())
        }
    }

    override fun onBindViewHolder(holder: adapter.myViewHolder, position: Int) {
        holder.lbTxt.text = mData[position]
    }

    override fun getItemCount(): Int {
        return mData.size
    }



    inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var lbTxt: TextView

        init {
            lbTxt = itemView.findViewById(R.id.lbstyleItem)
        }
    }
}

class RealTimeDeepStyleView : AppCompatActivity() , TextureView.SurfaceTextureListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var list_col : ArrayList<String> = ArrayList()

    private val REQUEST_CAMERA_PERMISSION = 0
    private var lensFacing = CameraX.LensFacing.FRONT
    private var imageCapture: ImageCapture? = null
    private val bEngine: DeepStyleManager = DeepStyleManager()
    private var cur_color: Int = Color.GREEN
    private  var isStyleLoaded : Boolean = false
    private var recyclerAdapter: adapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)



        val lickeycode = getString(R.string.lic_key)
        bEngine.init_data(this,lickeycode)

        bEngine.setDeepStyleListener( object: DeepStyleListener {
            override fun onSuccess(info: String) {
                r_progress_bar.visibility = View.INVISIBLE
                isStyleLoaded = true
            }

            override fun onFailure(error: String) {
                r_progress_bar.visibility = View.INVISIBLE

            }

            override fun onProgressUpdate(progress: String) {
                r_progress_bar.visibility = View.VISIBLE
                r_progress_bar.setProgress( Integer.parseInt(progress))

            }

        })


    }

    public fun load_style(styleID : String){

        bEngine.load_style(styleID)
    }

    fun init_view(){
        for (x in 0..188) {
            list_col.add("s" + (x+1).toString())
        }
        var strArray  = Array<String>(list_col.size,  { "$it" })
        list_col.toArray(strArray)

        val listView: RecyclerView = findViewById(R.id.lstStyleview)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        listView.layoutManager = layoutManager

        recyclerAdapter = adapter(this, list_col)
        recyclerAdapter?.setRview(this)

       // val myListAdapter = StyleListAdapter(this, strArray )
        listView.adapter = recyclerAdapter


//        listView.setOnItemClickListener(){adapterView, view, position, id ->
//            val itemAtPos = adapterView.getItemAtPosition(position)
//            val itemIdAtPos = adapterView.getItemIdAtPosition(position)
//
//            bEngine.load_style( "s" + (itemIdAtPos + 1).toString())
//            //url path from your server url
//            //bEngine.load_style_byURL("https://xxx/deepstyle/android/low/", "s" + (itemIdAtPos + 1).toString())
//
//        }
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
        setContentView(R.layout.reamtime_deepstyle_view)
        r_progress_bar.visibility = View.INVISIBLE
        init_view()

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
        val textureView: TextureView = findViewById(R.id.bg_tex_view)
        textureView.surfaceTextureListener = this

        // Handles the output data of the camera
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            // Displays the camera image in our preview view
            textureView.surfaceTexture = previewOutput.surfaceTexture
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

        if(isStyleLoaded) {
            val dimage = DoreImage.fromBitmap(bg_tex_view.bitmap)
            var result = bEngine.run(dimage)

            runOnUiThread {
                val outimg = result?.toBitmap(Size(bg_tex_view.bitmap.width, bg_tex_view.bitmap.height))
                bg_outImg.setImageBitmap(outimg)
            }

        }
    }

}