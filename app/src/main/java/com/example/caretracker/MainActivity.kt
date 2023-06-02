package com.example.caretracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() , FirebaseAuth.AuthStateListener{

    private val TAG = "MainActivity"
    var isOpenedFromLink = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val map = hashMapOf("name" to FirebaseAuth.getInstance().currentUser?.displayName,
            "email" to (FirebaseAuth.getInstance().currentUser?.email),
            "phone" to FirebaseAuth.getInstance().currentUser?.phoneNumber
        )

        val curId = FirebaseAuth.getInstance().currentUser?.uid
        if (curId != null) {
            FirebaseFirestore.getInstance().collection("users").document(curId)
                .set(map)
                .addOnSuccessListener {

                }.addOnFailureListener {  }
        }

        checkForDynamicLinks()

        val link : String = createDynamicLink(curId)
        Log.d("Add", "Generated link from main : $link")

        val registerButton : Button = findViewById(R.id.register)
        registerButton.let {
            it.setOnClickListener {
                val intent = Intent(this, AddChildActivity::class.java)
                intent.putExtra("linkToAdd", link)
                startActivity(intent)
            }
        }

    }

    private fun createDynamicLink(uid : String?): String {
        val dynamicLink =
            Firebase.dynamicLinks.dynamicLink { // or Firebase.dynamicLinks.shortLinkAsync
                link = Uri.parse("https://vikasipar.github.io/ct-userprofile?uid=$uid")
                domainUriPrefix = "https://caretracker.page.link/"
                androidParameters("com.example.caretracker") {
                    minimumVersion = 125
                    fallbackUrl = Uri.parse("https://vikasipar.github.io/ct-userprofile")
                }
            }
        val lnk = dynamicLink.uri.toString()
        Log.i("Main", "Here's the Dyna 2 Uri : ${dynamicLink.uri}")
        return lnk
    }

    private fun checkForDynamicLinks() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
            Log.i("Main", "We have dynamic link!")
            val deepLink: Uri? = it?.link

            if (deepLink != null) {
                isOpenedFromLink = true
                Log.i("Main", "Here's the deep link URL : \n$deepLink")

                val receivedUID = deepLink.getQueryParameter("uid")
                Log.i("Main", "Parameter is uid : $receivedUID")

                val detailsFragment = DetailsFragment(receivedUID)
                val fragTxn = supportFragmentManager.beginTransaction()
                fragTxn.replace(R.id.fragment_container, detailsFragment, "contact-screen").commit()

//                val curPage = Integer.parseInt(currentPage)
//                viewPager.setCurrentItem(curPage)

            }
        }.addOnFailureListener {
            Log.i("Main", "Failure $it")
        }
    }

    override fun onStart() {
        super.onStart()
        checkForDynamicLinks()
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        if(firebaseAuth.currentUser == null && !isOpenedFromLink){
            startLoginActivity()
            return

        }
        firebaseAuth.currentUser!!.getIdToken(true)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: " + it.token)
            }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.my_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.logout -> AuthUI.getInstance().signOut(this)
            R.id.about -> gotoAboutActivity()
            R.id.statistics -> gotoStatisticsActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoStatisticsActivity() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun gotoAboutActivity() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

}