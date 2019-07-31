package com.junmo.imagevisionapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_analyze_view.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_REQUEST = 1000
    private val GALLERY_PERMISSION_REQUEST = 1001
    private val FILE_NAME = "picture.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupListener()
    }

    private fun setupListener() {
        upload_image.setOnClickListener {
            UploadChooser().apply {
                addNotifier(object : UploadChooser.UploadChooserNotifierInterface {
                    override fun cameraOnClick() {
                        //카메라권한
                        checkCameraPermission()
                    }

                    override fun galleryOnClick() {
                        //저장장치권한
                        checkGalleryPermission()
                    }
                })
            }.show(supportFragmentManager, "")
            //UploadChooser().show(supportFragmentManager,"")
        }
    }

    private fun checkCameraPermission() {
        if (PermissionUtility().requestPermission(
                this, CAMERA_PERMISSION_REQUEST, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            openCamera()
        }
    }

    private fun checkGalleryPermission() {
        PermissionUtility().requestPermission(
            this, GALLERY_PERMISSION_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun openCamera() {
        val photoUri =
            FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", createCameraFile())
        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }
                val photoUri =
                    FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", createCameraFile())
                uploadImage(photoUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        if(bitmap.width>bitmap.height) bitmap = rotateImage(bitmap,-90f)
        uploaded_image.setImageBitmap(bitmap)
    }


    private fun rotateImage(bitmap: Bitmap , degree:Float):Bitmap{
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
    }


    private fun createCameraFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GALLERY_PERMISSION_REQUEST -> {

            }
            CAMERA_PERMISSION_REQUEST -> {
                if (PermissionUtility().permissionGranted(requestCode, CAMERA_PERMISSION_REQUEST, grantResults)) {
                    openCamera()
                }
            }
        }
    }
}
