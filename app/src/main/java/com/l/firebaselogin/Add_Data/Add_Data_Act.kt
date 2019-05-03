package com.l.firebaselogin.Add_Data

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.l.firebaselogin.R
import com.l.firebaselogin.ShowData
import kotlinx.android.synthetic.main.add_data.*

class Add_Data_Act:AppCompatActivity() {
    lateinit var refDb:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_data)

        refDb=FirebaseDatabase.getInstance().getReference("USERS")
        btnSave.setOnClickListener {
            saveData()
            val intent=Intent(this,
                ShowData::class.java)
            startActivity(intent)
        }
    }
    private fun saveData(){
        val nama=inputNama.text.toString()
        val status=inputStatus.text.toString()

        val userId=refDb.push().key.toString()
        val user=Users(userId,nama,status)

        refDb.child(userId).setValue(user)
            .addOnCompleteListener{
                Toast.makeText(this,
                    "Sucess",Toast.LENGTH_SHORT).show()
                inputNama.setText("")
                inputStatus.setText("")
            }
    }
}