package com.krishhh.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    var customProgressDialog : Dialog? = null
    private var lastSavedFilePath: String? = null
    private var isPermissionForSaving = false

    val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data!=null){
            val imageBackGround:ImageView = findViewById(R.id.iv_background)

            imageBackGround.setImageURI(result.data?.data)
        }
    }

    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val permissionName = entry.key
                val isGranted = entry.value

                if (isGranted) {
                    Toast.makeText(this@MainActivity, "Permission granted", Toast.LENGTH_SHORT).show()

                    if (isPermissionForSaving) {
                        // ✅ User will click Save again
                    } else {
                        // ✅ This is for opening gallery
                        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
                    }

                    // Reset the flag
                    isPermissionForSaving = false
                } else {
                    if (permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        Toast.makeText(this@MainActivity, "Oops! Permission denied", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(10.toFloat())

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)

        mImageButtonCurrentPaint = linearLayoutPaintColors[0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        val ib_brush : ImageButton = findViewById((R.id.ib_brush))
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        val ibGallery : ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener {
            isPermissionForSaving = false  // ✅ Mark it for gallery access
            requestStoragePermission()
        }

        val ib_undo : ImageButton = findViewById((R.id.ib_undo))
        ib_undo.setOnClickListener {
            drawingView?.onClickUndo()
        }

        val ib_redo : ImageButton = findViewById((R.id.ib_redo))
        ib_redo.setOnClickListener {
            drawingView?.onClickRedo()
        }

//        val ibSave : ImageButton = findViewById((R.id.ib_save))
//        ibSave.setOnClickListener {
//            if(isReadStorageAllowed()){
//                showProgressDialog()
//                lifecycleScope.launch {
//                    val flDrawingView: FrameLayout = findViewById((R.id.fl_drawing_view_container))
//                    // val myBitmap: Bitmap = getBitmapFromView(flDrawingView)
//                    saveBitmapFile(getBitmapFromView(flDrawingView))
//                }
//            }
//        }

        val ibSave : ImageButton = findViewById((R.id.ib_save))
        ibSave.setOnClickListener {
            if(isReadStorageAllowed()){
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView: FrameLayout = findViewById((R.id.fl_drawing_view_container))
                    val result = saveBitmapFile(getBitmapFromView(flDrawingView))
                    lastSavedFilePath = result  // ✅ THIS LINE FIXES YOUR ISSUE
                }
            } else {
                isPermissionForSaving = true  // ✅ Track that it's for saving, not gallery
                requestStoragePermission()  // ✅ Request permission if not granted
            }
        }

        val ibShare: ImageButton = findViewById(R.id.ib_share)
        ibShare.setOnClickListener {
            if (lastSavedFilePath != null && lastSavedFilePath!!.isNotEmpty()) {
                shareImage(lastSavedFilePath!!)
            } else {
                Toast.makeText(this, "Please save the image before sharing.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // To select which brush we need to use and dismiss it
    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    // when paint is clicked it will show the different colors and unselect the button when clicked on other color
    fun paintClicked(view: View){
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )

            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            mImageButtonCurrentPaint = view
        }
    }

    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                ))
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
                showRationalDialog("Kids Drawing App", "Kids Drawing App needs to Access Your External Storage")
            } else {
                requestPermission.launch(arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                ))
            }
        }
    }


    private fun showRationalDialog(
        title: String,
        message: String,
    ){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background

        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }

//    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String{
//        var result = ""
//        withContext(Dispatchers.IO){
//            if(mBitmap != null){
//                try {
//                    val bytes = ByteArrayOutputStream()
//                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
//
//                    val f = File(externalCacheDir?.absoluteFile.toString() + File.separator + "KidsDrawingApp_" + System.currentTimeMillis() / 1000 + ".png")
//
//                    val fo = FileOutputStream(f)
//                    fo.write(bytes.toByteArray())
//                    fo.close()
//
//                    result = f.absolutePath
//
//                    runOnUiThread{
//                        cancelProgressDialog()
//                        if(result.isNotEmpty()){
//                            Toast.makeText(this@MainActivity, "File saved successfully: $result", Toast.LENGTH_SHORT).show()
//                        } else {
//                            Toast.makeText(this@MainActivity, "Something went wrong while saving the file.", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//                catch (e: Exception) {
//                    result = ""
//                    e.printStackTrace()
//                }
//            }
//        }
//        lastSavedFilePath = result
//        return result
//    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String {
        var savedImagePath = ""

        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {
                    // Step 1: Set up the metadata (file name, MIME type, path)
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "KidsDrawingApp_${System.currentTimeMillis() / 1000}.png")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KidsDrawingApp") // Saves in Pictures/KidsDrawingApp folder
                    }

                    // Step 2: Get a URI where the image will be saved using MediaStore
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    // Step 3: If URI is valid, get OutputStream and write the bitmap to it
                    if (uri != null) {
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }

                        savedImagePath = uri.toString() // Store the image path as a string

                        // Step 4: Notify the user on the UI thread
                        runOnUiThread {
                            cancelProgressDialog()
                            Toast.makeText(
                                this@MainActivity,
                                "Image saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Handle case where image URI could not be created
                        runOnUiThread {
                            cancelProgressDialog()
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to create image Uri",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    savedImagePath = ""
                    runOnUiThread {
                        cancelProgressDialog()
                        Toast.makeText(
                            this@MainActivity,
                            "Something went wrong while saving the image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        return savedImagePath
    }


    // Method is used to show the custom progress dialog
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)

        // Set the screen content from a layout resource.
        // The resource will be inflated, adding all top_level views to the screen
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        // Start the dialog and display it on screen
        customProgressDialog?.show()
    }

    private fun cancelProgressDialog(){
        if(customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

//    private fun shareImage(result: String){
//        MediaScannerConnection.scanFile(this, arrayOf(result), null){
//            path, uri ->
//            val shareIntent =  Intent()
//            shareIntent.action = Intent.ACTION_SEND
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
//            shareIntent.type = "image/png"
//            startActivity(Intent.createChooser(shareIntent, "Share"))
//        }
//    }

    private fun shareImage(uriString: String) {
        val uri = Uri.parse(uriString)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }


}