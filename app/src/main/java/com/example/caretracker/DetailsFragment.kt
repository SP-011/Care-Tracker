package com.example.caretracker

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsFragment() : Fragment() , LocationListener {
    private var uid : String? = null
    constructor(uid : String?):this(){
        this.uid = uid
    }

    private lateinit var locationManager: LocationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val client = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),200
//                requestcode
            )
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        client.lastLocation.addOnSuccessListener {loc->
            if(loc != null){
                val location = "https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
                Log.d("Detail", "Location : $location")
//                Toast.makeText(requireContext(), "${loc.latitude}  ${loc.longitude}", Toast.LENGTH_SHORT).show()
            }
        }

        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val client = LocationServices.getFusedLocationProviderClient(requireActivity())


        val curId = FirebaseAuth.getInstance().currentUser?.uid
        if(curId != null){
            val kl = FirebaseFirestore.getInstance().collection("/users/$uid/child_details").document("added_child")

            kl.get().addOnSuccessListener {
                val nameTxt : TextView = view.findViewById(R.id.name_details)
                nameTxt.text = it.data?.get("name") as CharSequence?

                val numberTxt : TextView = view.findViewById(R.id.number_details)
                numberTxt.text = it.data?.get("phone") as CharSequence?

                val emailTxt : TextView = view.findViewById(R.id.email_details)
                emailTxt.text = it.data?.get("email") as CharSequence?

            }.addOnFailureListener {
            }
        }

        val reportBtn : Button = view.findViewById(R.id.report)
        reportBtn.let {
            it.setOnClickListener {


//                val isPermGranted = isLocationPermissionGranted()

                val phone : TextView = view.findViewById(R.id.number_details)
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ phone.text.toString()))
                startActivity(intent)

            }
        }

    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),200
//                request code
            )
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            false
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            true
        }

    }

    override fun onLocationChanged(loc: Location) {
        val location = "https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
        Log.d("Detail", "Location : $location")
        Toast.makeText(requireContext(), "Location sent", Toast.LENGTH_SHORT).show()
    }

}