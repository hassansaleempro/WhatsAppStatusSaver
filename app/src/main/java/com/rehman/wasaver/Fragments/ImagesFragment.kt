package com.rehman.wasaver.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.rehman.wasaver.HelperClasses.WAStatusAdapter
import com.rehman.wasaver.HelperClasses.WAStatusModels
import com.rehman.wasaver.R
import com.rehman.wasaver.databinding.FragmentImagesBinding
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class ImagesFragment : Fragment() {

    private lateinit var binding: FragmentImagesBinding
    private lateinit var list: ArrayList<WAStatusModels>
    private lateinit var waStatusAdapter: WAStatusAdapter
    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_images, container, false)

        val textView = view.findViewById<TextView>(R.id.title_img)


        val textPaint = textView.paint
        val width = textPaint.measureText("    Images")

        val textShader = LinearGradient(
            0f, 0f, width, textView.textSize, intArrayOf(
                Color.parseColor("#3bc1e6"), Color.parseColor("#ce38ce")
            ), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader




        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentImagesBinding.bind(view)

        list = ArrayList()
        val result = readDataFromPrefs()

        if (result) {
            val sh = requireContext().applicationContext.getSharedPreferences(
                "DATA_PATH", AppCompatActivity.MODE_PRIVATE
            )
            val uriPath = sh.getString("PATH", "")

            requireContext().applicationContext.contentResolver.takePersistableUriPermission(
                Uri.parse(uriPath), Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            if (uriPath != null) {

                val fileDoc = DocumentFile.fromTreeUri(
                    requireContext().applicationContext, Uri.parse(uriPath)
                )

                list.clear()

                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    if (!file.name!!.endsWith(".nomedia")) {
                        if (file.name!!.endsWith(".jpg")) {
                            val data = WAStatusModels(file.name!!, file.uri.toString())

                            list.add(data)
                        }
                    } else {

                    }
                }

                setUpRecyclerView(list)

            }


        } else {
            getFolderPermission()
        }



        binding.swipeRefresh.setOnRefreshListener {

            checkPermissions(0)

            if (result) {
                val sh = requireContext().applicationContext.getSharedPreferences(
                    "DATA_PATH", AppCompatActivity.MODE_PRIVATE
                )
                val uriPath = sh.getString("PATH", "")

                requireContext().applicationContext.contentResolver.takePersistableUriPermission(
                    Uri.parse(uriPath), Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                if (uriPath != null) {

                    val fileDoc = DocumentFile.fromTreeUri(
                        requireContext().applicationContext, Uri.parse(uriPath)
                    )

                    list.clear()

                    for (file: DocumentFile in fileDoc!!.listFiles()) {
                        if (!file.name!!.endsWith(".nomedia")) {
                            if (file.name!!.endsWith(".jpg")) {
                                val data = WAStatusModels(file.name!!, file.uri.toString())

                                list.add(data)
                            }
                        } else {

                        }
                    }

                    setUpRecyclerView(list)

                }


            }

            if (list.size == 0) {
                binding.imageNoResult.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = false
            } else {
                binding.imageNoResult.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = true
            }

            binding.swipeRefresh.isRefreshing = false

        }



        if (list.size == 0) {
            binding.imageNoResult.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        } else {
            binding.imageNoResult.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = true
        }

        binding.swipeRefresh.isRefreshing = false

    }


    private fun getFolderPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val sm = requireContext().applicationContext.getSystemService(
                AppCompatActivity.STORAGE_SERVICE
            ) as StorageManager

            val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
            var starDir = ""

            if (isInstalled("com.whatsapp")) {

                starDir = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
            } else if (isInstalled("com.whatsapp.w4b")) {
                starDir = "Android%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%2FMedia%2F.Statuses"
            }
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
            var scheme = uri.toString()

            scheme = scheme.replace("/root/", "/Document/")

            scheme += "%3A$starDir"

            uri = Uri.parse(scheme)

            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            intent.putExtra("android.content.extra.SHOW_ADVANCED",true)

            startActivityForResult(intent, 1234)


        }

        checkPermissions(0)
    }

    private fun isInstalled(s: String): Boolean {
        val packageManager = requireContext().applicationContext?.packageManager
        var isInstalled = false

        try {
            packageManager?.getPackageInfo(s, PackageManager.GET_ACTIVITIES)
            isInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return isInstalled
    }


    private fun checkPermissions(type: Int): Boolean {
        var result: Int
        val listPermissionNeeded: MutableList<String> = ArrayList()

        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(requireContext(), p)

            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(p)
            }
        }

        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                (activity as Activity?)!!, listPermissionNeeded.toTypedArray(), type
            )

            return false
        } else {
            getData()
        }
        return true
    }

    private fun getData() {
        var targetPath =
            Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/.Statuses"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            targetPath =
                Environment.getExternalStorageDirectory().absolutePath + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"


        }

        val targetDirectory = File(targetPath)

        val allFiles = targetDirectory.listFiles()

        try {
            list.clear()
            for (file in allFiles) {

                if (!file.name.endsWith(".nomedia")) {
                    if (file.name.endsWith(".jpg")) {
                        list.add(WAStatusModels(file.name, file.path))
                    }
                }
            }

            setUpRecyclerView(list)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun readDataFromPrefs(): Boolean {
        val sh = requireContext().applicationContext.getSharedPreferences(
            "DATA_PATH", AppCompatActivity.MODE_PRIVATE
        )
        val uriPath = sh.getString("PATH", "")

        if (uriPath != null) {
            if (uriPath.isEmpty()) {
                return false
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 1234) {
            val treeUri = data?.data

            val sharedPref = requireContext().applicationContext.getSharedPreferences(
                "DATA_PATH", AppCompatActivity.MODE_PRIVATE
            )

            val myEdit = sharedPref.edit()

            myEdit.putString("PATH", treeUri.toString())

            myEdit.apply()

            if (treeUri != null) {
                requireContext().applicationContext.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val fileDoc = DocumentFile.fromTreeUri(requireContext().applicationContext, treeUri)
                list.clear()

                for (file: DocumentFile in fileDoc!!.listFiles()) {

                    if (!file.name!!.endsWith(".nomedia")) {
                        if (file.name!!.endsWith(".jpg")) {

                            list.add(WAStatusModels(file.name!!, file.uri.toString()))

                        }


                    }

                    setUpRecyclerView(list)
                }
            }
        }
    }

    private fun setUpRecyclerView(list: ArrayList<WAStatusModels>) {

        waStatusAdapter = requireContext().applicationContext?.let {
            WAStatusAdapter(requireContext(), list) { selectedStatusItem: WAStatusModels ->
                listItemClicked(selectedStatusItem)
            }
        }!!

        binding.rvImages.setHasFixedSize(true)
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        binding.rvImages.layoutManager = manager
        binding.rvImages.adapter = waStatusAdapter


    }

    private fun listItemClicked(selectedStatusItem: WAStatusModels) {

        saveFile(selectedStatusItem)

    }

    @SuppressLint("SimpleDateFormat")
    private fun saveFile(selectedStatusItem: WAStatusModels) {

        if (selectedStatusItem.fileUri.endsWith(".mp4")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val inputStream =
                    requireContext().applicationContext.contentResolver.openInputStream(
                        Uri.parse(selectedStatusItem.fileUri)
                    )

                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("d:MMM:yy hh:mm:ss")
                val imgName = simpleDateFormat.format(calendar.time)


                val fileName = "WA_Saver_${imgName}.mp4"

                try {

                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)

                    values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")

                    values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOCUMENTS + "/WA Saver/"
                    )

                    val uri = requireContext().applicationContext.contentResolver.insert(
                        MediaStore.Files.getContentUri("external"), values
                    )

                    val outputStream: OutputStream = uri?.let {
                        requireContext().applicationContext.contentResolver.openOutputStream(it)
                    }!!

                    if (inputStream != null) {
                        outputStream.write(inputStream.readBytes())
                    }

                    outputStream.close()

                    Toast.makeText(requireContext(), "Video Saved", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

            } else {

                try {

                    createFileFolder()

                    val saveFilePath =
                        "${Environment.getExternalStorageDirectory()}/Documents/WA " + "Saver/"

                    val path: String = selectedStatusItem.fileUri

                    val fileName = path.substring(path.lastIndexOf("/") + 1)

                    val file = File(path)

                    val destFile = File(saveFilePath)

                    try {
                        FileUtils.copyFileToDirectory(file, destFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val calendar = Calendar.getInstance()
                    val simpleDateFormat = SimpleDateFormat("d:MMM:yy hh:mm:ss")
                    val imgName = simpleDateFormat.format(calendar.time)


                    val fileNameChange = "WA_Saver_${imgName}.mp4"

                    val newFile = File(saveFilePath + fileNameChange)

                    var contentType = "video/*"

                    MediaScannerConnection.scanFile(context,
                        arrayOf(newFile.absolutePath),
                        arrayOf(contentType),
                        object : MediaScannerConnectionClient {
                            override fun onScanCompleted(p0: String?, p1: Uri?) {

                            }

                            override fun onMediaScannerConnected() {

                            }

                        })

                    val from = File(saveFilePath, fileName)

                    val to = File(saveFilePath, fileNameChange)

                    from.renameTo(to).apply {
                        Toast.makeText(requireContext(), "Video Saved", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {


                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("d:MMM:yy hh:mm:ss")
                val imgName = simpleDateFormat.format(calendar.time)

                var bitmap: Bitmap? = null
                val fileName = "WA_Saver_${imgName}.jpg"
                var fos: OutputStream? = null

                bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().applicationContext.contentResolver,
                    Uri.parse(selectedStatusItem.fileUri)
                )

                requireContext().applicationContext.contentResolver.also { resolver ->

                    val contentValue = ContentValues().apply {

                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)

                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")

                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOCUMENTS + "/WA Saver/"
                        )

                    }

                    val imageUri: Uri? = resolver.insert(
                        MediaStore.Files.getContentUri("external"), contentValue
                    )

                    fos = imageUri?.let { resolver.openOutputStream(it) }


                }

                fos.use {
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_SHORT).show()
                }

            } else {

                try {

                    createFileFolder()

                    val saveFilePath =
                        "${Environment.getExternalStorageDirectory()}/Documents/WA " + "Saver/"

                    val path: String = selectedStatusItem.fileUri

                    val fileName = path.substring(path.lastIndexOf("/") + 1)

                    val file = File(path)

                    val destFile = File(saveFilePath)

                    try {
                        FileUtils.copyFileToDirectory(file, destFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val calendar = Calendar.getInstance()
                    val simpleDateFormat = SimpleDateFormat("d:MMM:yy hh:mm:ss")
                    val imgName = simpleDateFormat.format(calendar.time)


                    val fileNameChange = "WA_Saver_${imgName}.jpg"

                    val newFile = File(saveFilePath + fileNameChange)

                    var contentType = "image/*"

                    MediaScannerConnection.scanFile(context,
                        arrayOf(newFile.absolutePath),
                        arrayOf(contentType),
                        object : MediaScannerConnectionClient {
                            override fun onScanCompleted(p0: String?, p1: Uri?) {

                            }

                            override fun onMediaScannerConnected() {

                            }

                        })

                    val from = File(saveFilePath, fileName)

                    val to = File(saveFilePath, fileNameChange)

                    from.renameTo(to).apply {
                        Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun createFileFolder() {

        if (!File("${Environment.getExternalStorageDirectory()}/Documents/WA Saver/").exists()) {

            File("${Environment.getExternalStorageDirectory()}/Documents/WA Saver/").mkdir().apply {


            }
        }
    }

}

