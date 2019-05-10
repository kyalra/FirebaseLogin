package com.l.firebaselogin.MessagingServices

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
            messageTextView = itemView.findViewById<View>(
                R.id.messageTextView
            ) as TextView
            messageImageView = itemView.findViewById<View>(
                R.id.messageImageView
            ) as ImageView
            messagerTextView = itemView.findViewById<View>(
                R.id.messagerTextView
            ) as TextView
            messagerImageView = itemView.findViewById<View>(
                R.id.messagerImageView
            ) as CircleImageView
        }

    }

    private val TAG = "MainActivity"
    val MESSAGE_CHILD = "messages"
    private val REQUEST_INVITE = 1
    private val REQUEST_IMAGE = 2
    private val REQUEST_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    val ANONYMOUS = "anonymous"
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null
    private var mSharedPreferences: SharedPreferences? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mSendButton: Button? = null
    private var mMessageRecyclerView: RecyclerView? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mProgressBar: ProgressBar? = null
    private var mMessageEditText: EditText? = null
    private var mAddMessageImageView: ImageView? = null
    //firebase instance variable
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFireBaseUser: FirebaseUser? = null
    private var mFirebaseDatabaseReference: DatabaseReference? = null
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<FriendlyServices, MessageViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_message)
        mSharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(this)
        //set default username
        mUsername = ANONYMOUS

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this/*FragmentActivity*/, this/*OnConnectionFailedListener*/)
            .addApi(Auth.GOOGLE_SIGN_IN_API)
            .build()

        //initialize progres bar and recyclerview
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mMessageRecyclerView = findViewById<View>(R.id.messageRecycleView) as RecyclerView
        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager!!.stackFromEnd = true
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        val parser = SnapshotParser<FriendlyServices> {dataSnapshot->
            val friendlyMessage = dataSnapshot.getValue(FriendlyServices::class.java)
            if (friendlyMessage!=
                    null /*no imagee */){
                friendlyMessage!!.setId(dataSnapshot.key!!)
            }
            friendlyMessage!!
        }
        val messagesRef=mFirebaseDatabaseReference!!
            .child(MESSAGE_CHILD)
        val Options=FirebaseRecyclerOptions
            .Builder<FriendlyServices>()
            .setQuery(messagesRef,parser)
            .build()
            mFirebaseAdapter=object :
        FirebaseRecyclerAdapter<FriendlyServices,MessageViewHolder>(options){
                override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: FriendlyServices) {
                    mProgressBar!!.visibility=ProgressBar.INVISIBLE

                }

                override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MessageViewHolder {
                    val inflater=LayoutInflater.from(viewGroup.context)
                    return MessageViewHolder(inflater.inflate(
                        R.layout.item_message,viewGroup,false
                    ))
                }
            }

    }
}