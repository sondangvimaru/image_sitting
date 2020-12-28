package com.cnt57cl.image_sitting

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.snackbar.Snackbar
import com.roger.catloadinglibrary.CatLoadingView
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.bytedeco.javacpp.opencv_stitching.Stitcher
import org.opencv.android.BaseLoaderCallback
import org.opencv.core.Mat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var radioGroup: RadioGroup
    private  lateinit var  catload:CatLoadingView
    private  lateinit var sondialog: Sondialog
    private lateinit var imageStitcher: ImageStitcher
    private lateinit var disposable: Disposable

    private val stitcherInputRelay = PublishSubject.create<StitcherInput>()
    private lateinit var mat:Mat
    private lateinit var output:Mat
    private  var listformatimage: ArrayList<String>?=null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    val call:BaseLoaderCallback =object :BaseLoaderCallback(this)
    {

        override fun onManagerConnected(status: Int) {
            super.onManagerConnected(status)


            mat= Mat()
            output=Mat()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpViews()
        setUpStitcher()
    }

    private fun setUpViews() {
        if (shouldAskPermissions()) {
            askPermissions()
        }

        setfomatdefual()
        setSupportActionBar(toolbar)
        imageView = findViewById(R.id.image)
        radioGroup = findViewById(R.id.radio_group)
        findViewById<View>(R.id.button).setOnClickListener { chooseImages() }
    }

    @Suppress("DEPRECATION")
    private fun setUpStitcher() {
        imageStitcher = ImageStitcher(FileUtil(applicationContext))

//        val dialog = ProgressDialog(this).apply {
//            setMessage(getString(R.string.processing_images))
//            setCancelable(false)
//
//        }
        catload= CatLoadingView()
        catload.isCancelable=false
        catload.setText(getString(R.string.processing_images))

        sondialog= Sondialog(this,R.drawable.dr)
        sondialog.setmessenger(getString(R.string.processing_images))
        sondialog.setCancelable(false)

        disposable = stitcherInputRelay.switchMapSingle {
            imageStitcher.stitchImages(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {catload.show(supportFragmentManager,"son") }
                .doOnSuccess { catload.dismiss() }
        }
            .subscribe({ processResult(it) }, { processError(it) })
    }

    private fun chooseImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType(INTENT_IMAGE_TYPE)
            .putExtra(EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, CHOOSE_IMAGES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_IMAGES && resultCode == Activity.RESULT_OK && data != null) {
            val clipData = data.clipData
            val images = if (clipData != null) {
                List(clipData.itemCount) { clipData.getItemAt(it).uri }
            } else {
                listOf(data.data!!)
            }
            processImages(images)
        }
    }

    private fun processImages(uris: List<Uri>) {
        imageView.setImageDrawable(null) // reset preview
        val isScansChecked = radioGroup.checkedRadioButtonId == R.id.radio_scan
        val stitchMode = if (isScansChecked) Stitcher.SCANS else Stitcher.PANORAMA
        stitcherInputRelay.onNext(StitcherInput(uris, stitchMode))
    }

    private fun processError(e: Throwable) {
        Log.e(TAG, "", e)
        Toast.makeText(this, e.message + "", Toast.LENGTH_LONG).show()
    }

    private fun processResult(output: StitcherOutput) {
        when (output) {
            is StitcherOutput.Success -> showImage(output.file)
            is StitcherOutput.Failure -> processError(output.e)
        }
    }

    private fun showImage(file: File) {



        Picasso.with(this).load(file)
           .error(R.drawable.ic_launcher_background)
            .into(imageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.mn_save_image ->
            {
                if(imageView.drawable!=null) save_menu_click()
                else Toast.makeText(applicationContext,"Ảnh không tồn tại",Toast.LENGTH_SHORT).show()



            }
            R.id.mn_share->
            {
                if(imageView.drawable!=null) shareclick()
                else Toast.makeText(applicationContext,"Ảnh không tồn tại",Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    protected fun shouldAskPermissions(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    protected fun askPermissions() {
        val permissions = arrayOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        )
        val requestCode = 200
        requestPermissions(permissions, requestCode)
    }
    fun save_menu_click() {
        val inflater = this@MainActivity.layoutInflater
        val view = inflater.inflate(R.layout.saveimage, null)
        val bd = AlertDialog.Builder(this@MainActivity)

        bd.setTitle("Save Image")
        bd.setView(view)
        val edt_name = view.findViewById(R.id.ed_nameimage) as EditText
        val sp_format = view.findViewById(R.id.sp_format) as Spinner
        val adapter: ArrayAdapter<String> = ArrayAdapter(this@MainActivity,android.R.layout.simple_list_item_1,listformatimage!!)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        sp_format.adapter = adapter
        val img_save = view.findViewById<View>(R.id.img_saveimage) as ImageView
        img_save.setImageResource(R.drawable.saveimage)
        bd.setPositiveButton("Hủy") { dialog, which -> Toast.makeText(this@MainActivity, "Đã hủy Thao tác", Toast.LENGTH_SHORT).show() }.setNegativeButton("Lưu") { dialog, which ->
            try {
                val bitmap = (imageView.getDrawable() as BitmapDrawable).bitmap
                val format: String = getformatimage(sp_format.selectedItem.toString())!!
                saveanh(bitmap, edt_name.text.toString().trim { it <= ' ' }, format)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lỗi khi Lưu ảnh" + e.message, Toast.LENGTH_SHORT).show()
            }
        }.setCancelable(false)
        bd.create().show()
    }
    fun setfomatdefual() {
        listformatimage= ArrayList()
        listformatimage?.add("PNG(*.png)")
        listformatimage?.add("JPEG(jpg;jpeg;jpe;jfif)")
    }
    fun getformatimage(formatype: String?): String? {
        when (formatype) {
            "PNG(*.png)" -> return "png"
            "JPEG(jpg;jpeg;jpe;jfif)" -> return "jpg"

        }
        return "png"
    }
    fun getDisc(): File? {
        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        return File(file, "CNT57CL")
    }
    fun saveanh(bm: Bitmap, name: String, type: String) {
        val new_file: File
        var fileOutputStream: FileOutputStream? = null
        val file: File = getDisc()!!
        if (!file.exists()) {
            file.mkdir()
        }
        try {
            val file_name = file.absolutePath + "/" + name + "." + type
            new_file = File(file_name)
            fileOutputStream = FileOutputStream(new_file)


            if (type.trim { it <= ' ' } == "jpg") {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            } else if (type.trim { it <= ' ' } == "png") {
                bm.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            }

            val snack:Snackbar = Snackbar.make(layout_main,"lưu thành công",Snackbar.LENGTH_SHORT)
            val v: View   = snack.view
         val textView = v.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
          textView.setTextColor(Color.RED)
          textView.isAllCaps=true
            textView.textSize=20f
            v.setBackgroundColor(Color.parseColor("#03A9F4"))
            snack.show()
            fileOutputStream.flush()
          fileOutputStream.close()
            Log.d("loivang","vang")
         Lammoi(new_file)
        } catch (e: FileNotFoundException) {
          Log.d("loi_save",e.message.toString())
        } catch (e: IOException) {
            Log.d("loi_save",e.message.toString())
        }
    }
    private fun Lammoi(file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        sendBroadcast(intent)
    }
    fun shareclick() {
        try {
            val bmshare = (imageView.getDrawable() as BitmapDrawable).bitmap
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bmshare))
            shareIntent.type = "image/*"
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ Ảnh"))
        } catch (e: java.lang.Exception) {
        }
    }
    private fun getLocalBitmapUri(bmp: Bitmap): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image" + System.currentTimeMillis() + ".png")
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 50, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }
    companion object {
        private const val TAG = "TAG"
        private const val EXTRA_ALLOW_MULTIPLE = "android.intent.extra.ALLOW_MULTIPLE"
        private const val INTENT_IMAGE_TYPE = "image/*"
        private const val CHOOSE_IMAGES = 777
    }
}
