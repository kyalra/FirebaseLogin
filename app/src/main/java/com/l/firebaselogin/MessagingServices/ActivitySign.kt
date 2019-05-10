package com.l.firebaselogin.MessagingServices

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.l.firebaselogin.R

class ActivitySign : AppCompatActivity(),
    GoogleApiClient.OnConnectionFailedListener,
    View.OnClickListener {

    private val TAG = "ActivitySign"
    private val RC_SIGN_IN = 9001
    private var mSignInButton: SignInButton? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    //firebase instance variable
    private var mfirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin_activity)

        //Assign fields
        mSignInButton = findViewById<View>(R.id.sign_in_button) as SignInButton
        //set Click listener
        mSignInButton!!.setOnClickListener(this)
        //Configure Google Sign In
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /*FragmentActivity*/, this /*OnConnectionFailedListener*/)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

        //Initialize FirebaseAuth
        mfirebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + "$p0")
        Toast.makeText(this, "Google Play" + "Services Error.", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
        }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi
            .getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //result returned from launching the Intent from google
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi
                .getSignInResultFromIntent(data)
            if (result.isSuccess) {
                //google signin was succes
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                //google signin failed
                Log.e(TAG, "Google Sign-in Failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG,"firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mfirebaseAuth!!.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)
            if (!task.isSuccessful) {
                Log.w(TAG, "signInWithCredential", task.exception)
                Toast.makeText(
                    this@ActivitySign,
                    "Autentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startActivity(
                    Intent(this@ActivitySign, MainMessage::class.java)
                )
                finish()
            }
        }
    }
}
