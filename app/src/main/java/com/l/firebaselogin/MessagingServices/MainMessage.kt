package com.l.firebaselogin.MessagingServices

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
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
    private val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
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
        mMessageRecyclerView!!.layoutManager = mLinearLayoutManager
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        val parser = SnapshotParser<FriendlyServices> { dataSnapshot ->
            val friendlyServices = dataSnapshot.getValue(FriendlyServices::class.java)
            if (friendlyServices !=
                null /*no imagee */) {
                friendlyServices!!.setId(dataSnapshot.key!!)
            }
            friendlyServices!!
        }
        val messagesRef = mFirebaseDatabaseReference!!
            .child(MESSAGE_CHILD)
        val Options = FirebaseRecyclerOptions
            .Builder<FriendlyServices>()
            .setQuery(messagesRef, parser)
            .build()
        mFirebaseAdapter = object :
            FirebaseRecyclerAdapter<FriendlyServices, MessageViewHolder>(Options) {
            override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): MessageViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                return MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false))
            }


            override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: FriendlyServices) {
                mProgressBar!!.visibility = ProgressBar.INVISIBLE
                if (model.getText() != null) {
                    holder.messageTextView.text = model.getText()
                    holder.messageTextView.visibility = TextView.VISIBLE
                    holder.messageImageView.visibility = ImageView.GONE
                } else if (model.getImageUrl() != null) {
                    val imageUrl = model.getImageUrl()
                    if (imageUrl!!.startsWith("gs://")) {
                        val storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl)
                        storageReference.downloadUrl
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val downloadUrl = task.result!!.toString()
                                    Glide.with(holder.messageImageView.context)
                                        .load(downloadUrl)
                                        .into(holder.messagerImageView)
                                } else {
                                    Log.w(TAG, "Getting download" + "url was not successful.", task.exception)
                                }
                            }
                    } else {
                        Glide.with(holder.messagerImageView.context)
                            .load(model.getImageUrl()!!)
                            .into(holder.messageImageView)
                    }
                    holder.messageImageView.visibility = ImageView.VISIBLE
                    holder.messageTextView.visibility = TextView.GONE
                }
                holder.messagerTextView.text = model.getName()
                if (model.getphotoUrl() == null) {
                    holder.messagerImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainMessage,
                            android.R.drawable.btn_star_big_off
                        )
                    )
                } else {
                    Glide.with(this@MainMessage).load(model.getphotoUrl()).into(holder.messagerImageView)
                }
            }
        }
        mFirebaseAdapter!!.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    val friendlyMessageCount = mFirebaseAdapter!!.itemCount
                    val lastVisiblePosition = mLinearLayoutManager!!
                        .findLastCompletelyVisibleItemPosition()
                    if ((lastVisiblePosition == -1 || (
                                (positionStart >=
                                        (friendlyMessageCount - 1)
                                        && lastVisiblePosition ==
                                        (positionStart - 1))))
                    ) {
                        mMessageRecyclerView!!.scrollToPosition(positionStart)
                    }
                }
            }
        )
        mMessageRecyclerView!!.adapter = mFirebaseAdapter
        mMessageEditText = findViewById<View>(R.id.messageEditText) as EditText
        mMessageEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim { it <= ' ' }.length > 0) {
                    mSendButton!!.isEnabled = true
                } else {
                    mSendButton!!.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        mSendButton = findViewById<View>(R.id.sendButton) as Button
        mSendButton!!.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val friendlyServices = FriendlyServices(
                        mMessageEditText!!.text.toString(),
                        mUsername!!,
                        mPhotoUrl!!, null
                    )
                    mFirebaseDatabaseReference!!
                        .child(MESSAGE_CHILD).push().setValue(friendlyServices)
                    mMessageEditText!!.setText("")
                }
            })
        mAddMessageImageView = findViewById<View>(R.id.addMessageImageView) as ImageView
        mAddMessageImageView!!.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    //select image for image message on click
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_IMAGE)
                }
            })
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFireBaseUser = mFirebaseAuth!!.currentUser
        if (mFireBaseUser == null) {
            //not sign in,launch the sign in activity
            startActivity(Intent(this, ActivitySign::class.java))
            finish()
            return
        } else {
            mUsername = mFireBaseUser!!.displayName
            if (mFireBaseUser!!.photoUrl != null) {
                mPhotoUrl = mFireBaseUser!!.photoUrl.toString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        mFirebaseAdapter!!.stopListening()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            R.id.signout_menu -> {
                mFirebaseAuth!!.signOut()
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                mUsername = ANONYMOUS
                startActivity(Intent(this, ActivitySign::class.java))
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            TAG, "onActivityReseult:" +
                    "requestCode=$requestCode," +
                    "resultCode=$requestCode"
        )
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    Log.d(TAG, "Uri" + uri!!.toString())

                    val tempMessage = FriendlyServices(
                        null, mUsername!!, mPhotoUrl!!, LOADING_IMAGE_URL
                    )
                    mFirebaseDatabaseReference!!.child(MESSAGE_CHILD).push()
                        .setValue(tempMessage, object : DatabaseReference.CompletionListener {
                            override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
                                if (p0 == null) {
                                    val key = p1.key
                                    val storageReference = FirebaseStorage.getInstance()
                                        .getReference(mFireBaseUser!!.uid)
                                        .child(key!!)
                                        .child(uri.lastPathSegment!!)
                                    putImageInStorage(storageReference, uri, key)

                                } else {
                                    Log.w(TAG, "Unable to write" + "message to database.", p0!!.toException())
                                }
                            }
                        })
                }
            }
        }
    }

    private fun putImageInStorage(
        storageReference:
        StorageReference, uri: Uri?, key: String?
    ) {
        storageReference.putFile(uri!!)
            .addOnCompleteListener(this@MainMessage,
                object : OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(
                        task: Task<UploadTask
                        .TaskSnapshot>
                    ) {
                        if (task.isSuccessful) {
                            task.result!!.metadata!!
                                .reference!!.downloadUrl
                                .addOnCompleteListener(this@MainMessage,
                                    object : OnCompleteListener<Uri> {
                                        override fun onComplete(task: Task<Uri>) {
                                            if (task.isSuccessful) {
                                                val friendlyMessage = FriendlyServices(
                                                    null, mUsername!!, mPhotoUrl!!,
                                                    task.result!!.toString()
                                                )
                                                mFirebaseDatabaseReference!!
                                                    .child(MESSAGE_CHILD).child(key!!)
                                                    .setValue(friendlyMessage)
                                            }
                                        }
                                    })
                        } else {
                            Log.w(
                                TAG, "Image upload" +
                                        " task was not successful.",
                                task.exception
                            )
                        }
                    }
                })
    }
}






