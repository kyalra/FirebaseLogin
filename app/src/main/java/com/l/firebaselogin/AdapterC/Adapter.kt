package com.l.firebaselogin.AdapterC

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.l.firebaselogin.Add_Data.Users
import com.l.firebaselogin.R
import com.l.firebaselogin.ShowData
import kotlinx.android.synthetic.main.show_insert.view.*

class Adapter(
    val mCtx: Context, val layoutResId: Int,
    val list: List<Users>
) : ArrayAdapter<Users>(mCtx, layoutResId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater =
            LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(layoutResId, null)

        val tvNama = view.findViewById<TextView>(R.id.tvNama)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        val textUpdate = view.findViewById<TextView>(R.id.textUpdate)
        val textDelete = view.findViewById<TextView>(R.id.textDelete)

        val users = list[position]

        tvNama.text = users.nama
        tvStatus.text = users.status

        textUpdate.setOnClickListener {
            showUpdateDialog(users)
        }
        textDelete.setOnClickListener {
            Deleteinfo(users)
        }
        return view
    }

    private fun Deleteinfo(users: Users) {
        val progressDialog = ProgressDialog(context, R.style.AppTheme)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Deleting...")
        progressDialog.show()
        val mydatabase = FirebaseDatabase.getInstance()
            .getReference("USERS")
        mydatabase.child(users.id).removeValue()
        Toast.makeText(
            mCtx, "Deleted!!",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(context, ShowData::class.java)
        context.startActivity(intent)
    }

    private fun showUpdateDialog(users: Users) {
        val builder = AlertDialog.Builder(mCtx)
            builder.setTitle ("Update")
        val inflater = LayoutInflater.from(mCtx)
        val view = inflater.inflate(R.layout.update_adata, null)
        val updNama = view.findViewById<EditText>(R.id.updNama)
        val updStatus = view.findViewById<EditText>(R.id.updStatus)

        updNama.setText(users.nama)
        updStatus.setText(users.status)
        builder.setView(view)
        builder.setPositiveButton("Update") { dialog, which ->

            val dbUser = FirebaseDatabase.getInstance().getReference("USERS")
            val nama = updNama.text.toString().trim()
            val status = updStatus.text.toString().trim()
            if (nama.isEmpty()) {
                updNama.error = "Nama Gaboleh Kosong"
                updNama.requestFocus()
                return@setPositiveButton
            }
            if (status.isEmpty()) {
                updStatus.error = "Status gaboleh Kosong"
                updStatus.requestFocus()
                return@setPositiveButton
            }
            val user = Users(users.id, nama, status)
            dbUser.child(user.id).setValue(user).addOnCompleteListener {
                Toast.makeText(mCtx, "Updated", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("No") { dialog, which -> }

        val alert = builder.create()
        alert.show()
    }
}