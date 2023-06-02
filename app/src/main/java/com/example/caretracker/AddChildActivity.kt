package com.example.caretracker

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONException
import org.json.JSONObject
import java.security.AccessController.getContext

class AddChildActivity : AppCompatActivity(), PaymentResultListener {

//    private var requestQueue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_child)

        var linkCreated = intent.getStringExtra("linkToAdd")

//      Replaced "&" with it's percent-encoded "%26" so it can be added in qr-code otherwise link part after
//      "&" isn't appearing after scanning qr-code.
        if (linkCreated != null) {
            linkCreated = linkCreated.replace("&", "%26")
        }

        val submitButton : Button = findViewById(R.id.submit)
        submitButton.let {
            it.setOnClickListener {
                val childName : EditText = findViewById(R.id.child_name)
                val guardianEmail : EditText = findViewById(R.id.guardians_email)
                val guardianPhone : EditText = findViewById(R.id.guardians_phone)
                val qrImagePlaceHolder : ImageView= findViewById(R.id.qr_container)

                val url = "https://api.qrserver.com/v1/create-qr-code/?data=$linkCreated&amp;size=200x200"
//                val url = "https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=$linkCreated"

                val isDataValid = inputValidator(childName.text.toString(), guardianEmail.text.toString(), guardianPhone.text.toString())
                if(isDataValid){
                    val buyNowBtn : Button = findViewById(R.id.buy_now)
                    buyNowBtn.visibility = android.view.View.VISIBLE
//                    buyNowBtn.visibility = 1
                    Glide.with(this).load(url).into(qrImagePlaceHolder)

                    val map = hashMapOf("name" to childName.text.toString(),
                        "email" to guardianEmail.text.toString(),
                        "phone" to guardianPhone.text.toString())

                    val curId = FirebaseAuth.getInstance().currentUser?.uid
                    val collectionsPath = "users/$curId/child_details"
                    FirebaseFirestore.getInstance().collection(collectionsPath).document("added_child")
                        .set(map)
                        .addOnSuccessListener {
                        }.addOnFailureListener {
                        }

                    buyNowBtn.let {
                        it.setOnClickListener {
                            val amount = 999*100

                            val checkout = Checkout()
                            checkout.setKeyID("rzp_test_N46xVvlxPRenZm")
                            checkout.setImage(R.drawable.logo)

                            val options = JSONObject()

                            try {
                                options.put("name", "Care Tracker")
                                options.put("description", "amount to be pain")
                                options.put("currency", "INR")
                                options.put("amount", amount)
                                options.put("prefill.contact", guardianPhone.text.toString())
                                options.put("prefill.email", guardianEmail.text.toString())

                                checkout.open(this, options)

                            }catch (e : JSONException){
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

    }

    private fun inputValidator(name : String, guardianEmail : String, guardianPhone : String) : Boolean{

//              Checking here if any field is empty or not
        if(name == "" || guardianEmail == "" || guardianPhone == "") {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT)
                .show()
            return false

//              Here we validate the email entered
        }else if(!Patterns.PHONE.matcher(guardianPhone).matches() || guardianPhone.length != 10 || guardianPhone[0] !in arrayOf('9', '8', '7')){
            Toast.makeText(this, "Incorrect phone number", Toast.LENGTH_SHORT).show()
            return false

//              Here we validate the email entered
        }else if(!Patterns.EMAIL_ADDRESS.matcher(guardianEmail).matches()){
            Toast.makeText(this, "Incorrect email address", Toast.LENGTH_SHORT).show()
            return false

        }
        return true
    }

    override fun onPaymentSuccess(s: String?) {
        Toast.makeText(this, "Payment is successful", Toast.LENGTH_SHORT).show();
    }

    override fun onPaymentError(i: Int, s: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
    }
}