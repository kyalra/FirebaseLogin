package com.l.firebaselogin

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.upd_image.*

class UploadAct : AppCompatActivity() {

    val PERMISION_REQUES_CODE = 1001
    val PICK_IMAGE_REQUEST = 900;
    lateinit var filePath: Uri

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.upd_image)
        image.setOnClickListener {
            when {
                (Build.VERSION.SDK_INT >= Build
                    .VERSION_CODES.M) -> {
                    if (ContextCompat.checkSelfPermission(
                            this@UploadAct, Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ), PERMISION_REQUES_CODE
                        )
                    } else {
                        chooseFile()
                    }
                }
                else -> chooseFile()
            }
        }
    }
    private fun chooseFile(){
        val intent=Intent().apply {
            type="image/*"
            action=Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISION_REQUES_CODE->{
                if(grantResults.isEmpty()||grantResults[0]
                ==PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this@UploadAct,"Oops!Permission Denied!!",Toast.LENGTH_SHORT).show()
                else
                    chooseFile()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode!=Activity.RESULT_OK){
            return;
        }
        when(requestCode){
            PICK_IMAGE_REQUEST->{
                filePath=data!!.getData()
                uploadFile()
            }
        }
    }
    private fun uploadFile(){
        val progres=ProgressDialog(this).apply {
            setTitle("Uploading Picture...")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
        val data=FirebaseStorage.getInstance()
        var value=0.0
        var storage=data.getReference()
            .child("mypic.jpg")
            .putFile(filePath)
            .addOnProgressListener {
                taskSnapshot ->
                value=(100.0*taskSnapshot.bytesTransferred)/taskSnapshot
                    .totalByteCount
                Log.v("value","value=="+value)
                progres.setMessage("Uploaded..."+value.toInt()+"%")
            }.addOnSuccessListener {
                taskSnapShot->
                progres.dismiss()

                val uri=taskSnapShot.storage
                    .downloadUrl
                Log.v("Download File","File..."
                        +uri)
                Glide.with(this@UploadAct).load(uri).into(image)
            }.addOnFailureListener{
                exception ->exception
                .printStackTrace()
            }
    }
}