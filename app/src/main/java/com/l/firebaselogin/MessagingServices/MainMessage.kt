package com.l.firebaselogin.MessagingServices

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.l.firebaselogin.R
import de.hdodenhof.circleimageview.CircleImageView

class MainMessage : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class MessageViewHolder(v: View) :
        RecyclerView.ViewHolder(v) {
        internal var messageTextView: TextView
        internal var messageImageView: ImageView
        internal var messagerTextView: TextView
        internal var messagerImageView: CircleImageView

        init {
            messageTextView=itemView.findViewById<View>(
                R.id.messageTextView)as TextView
            messageImageView=itemView.findViewById<View>(
                R.id.messageImageView)as ImageView
            messagerTextView=itemView.findViewById<View>(
                R.id.messagerTextView)as TextView
            messagerImageView=itemView.findViewById<View>(
                R.id.messagerImageView)as CircleImageView
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_message)

    }
}