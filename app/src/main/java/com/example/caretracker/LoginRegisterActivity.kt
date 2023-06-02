package com.example.caretracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class LoginRegisterActivity : AppCompatActivity() {

    private val TAG = "LoginRegisterActivity"
    val AUTHUI_REQUEST_CODE = 10001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        if (FirebaseAuth.getInstance().currentUser == null) {

            val provider: List<AuthUI.IdpConfig> = listOf(AuthUI.IdpConfig.EmailBuilder().build(),
                                                          AuthUI.IdpConfig.GoogleBuilder().build(),
                                                          AuthUI.IdpConfig.PhoneBuilder().build())

            val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(provider)
                .setTheme(R.style.Theme_CareTracker)
                .setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
                .setAlwaysShowSignInMethodScreen(true)
                .setLogo(R.drawable.logo)
                .build()

            startActivityForResult(intent, AUTHUI_REQUEST_CODE)

        }else{
            goToMainActivity()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == AUTHUI_REQUEST_CODE){
            if(resultCode == RESULT_OK){

                val user = FirebaseAuth.getInstance().currentUser
                Log.d(TAG, "onActivityResult: " + user?.email)
                goToMainActivity()

            }else{
                val response = IdpResponse.fromResultIntent(data)
                if(response == null){
                    Log.d(TAG, "onActivityResult: the user cancelled the sign in request" )
                }else{
                    Log.d(TAG, "onActivityResult: ", response.error )
                }
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}