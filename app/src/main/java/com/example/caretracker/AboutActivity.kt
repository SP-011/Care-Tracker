package com.example.caretracker

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class AboutActivity : AppCompatActivity() {

//    private val args : AboutActivityArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

    checkForDynamicLinks()
    }

    override fun onStart() {
        super.onStart()

        checkForDynamicLinks()
    }

    private fun checkForDynamicLinks() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
            Log.i("About", "We have dynamic link!")
            val deepLink: Uri? = it?.link

            if (deepLink != null) {
                Log.i("About", "Here's the deep link URL : \n$deepLink")

                val currentPage = deepLink.getQueryParameter("curPage")
    //            val curPage = Integer.parseInt(currentPage)
//                viewPager.setCurrentItem(curPage)
            }
        }.addOnFailureListener {
            Log.i("About", "Failure $it")
        }
    }
}