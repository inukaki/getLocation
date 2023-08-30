package com.websarva.wings.android.implicitintentsample

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog.Locations
import android.provider.SyncStateContract
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private var _latitude = 0.0
    private var _longitude = 0.0

    private lateinit var _fusedLocationClient: FusedLocationProviderClient
    private lateinit var _locationRequest: LocationRequest
    private lateinit var _onUpdateLocation: OnUpdateLocation

    // 追加行
    lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        // 追加行
//        geofencingClient = LocationServices.getGeofencingClient(this)
//
//        var geofenceList: MutableList<Int> = mutableListOf()
//        geofenceList.add(
//            Geofence.Builder()
//            // Set the request ID of the geofence. This is a string to identify this
//            // geofence.
//            .setRequestId(1)
//
//            // Set the circular region of this geofence.
//            .setCircularRegion(
//                _latitude,
//                _longitude,
//                100
//            )
//
//            // Set the expiration duration of the geofence. This geofence gets automatically
//            // removed after this period of time.
//            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//
//            // Set the transition types of interest. Alerts are only generated for these
//            // transition. We track entry and exit transitions in this sample.
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
//
//            // Create the geofence.
//            .build())



        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        val builder = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        _locationRequest = builder.build()
        _onUpdateLocation = OnUpdateLocation()
    }
    override fun onResume(){
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
//        _fusedLocationClient.removeLocationUpdates(_onUpdateLocation)
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 許可をACCESS_FINE_LOCATIONとACCESS_COARSE_LOCATIONに設定
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            // 許可を求めるダイアログを表示。その際、リクエストコードを1000に設定、
            ActivityCompat.requestPermissions(this@MainActivity,permissions,1000)

            return
        }
        _fusedLocationClient.requestLocationUpdates(_locationRequest,_onUpdateLocation,mainLooper)
    }

    fun onMapSearchButtonClick(view: View){
        val etSearchWord =findViewById<EditText>(R.id.etSearchWord)
        var searchWord = etSearchWord.text.toString()

        searchWord = URLEncoder.encode(searchWord,"UTF-8")
        val uriStr = "geo:0,0?q=${searchWord}"

        val uri = Uri.parse(uriStr)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        startActivity(intent)
    }
    fun onMapShowCurrentButtonClick(view: View){
        val uriStr = "geo:${_latitude},${_longitude}"
        val uri = Uri.parse(uriStr)
        val intent = Intent(Intent.ACTION_VIEW,uri)
        startActivity(intent)
    }

    private inner class OnUpdateLocation : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult){
            val location = locationResult.lastLocation
            val TAG ="onLocationResult"
            location?.let {
                _latitude = it.latitude
                _longitude = it.longitude

                val tvLatitude = findViewById<TextView>(R.id.tvLatitude)
                tvLatitude.text = _latitude.toString()

                val tvLongitude = findViewById<TextView>(R.id.tvLongitude)
                tvLongitude.text = _longitude.toString()

                Log.i(TAG, "latitude:$_latitude, longitude:$_longitude")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 位置情報のパーミッションダイアログでかつ許可を選択したならば
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED)
            // 再度許可がおりていないかどうかのチェックをし、おりていないなら処理を中止。
            if(ActivityCompat.checkSelfPermission(this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){
                return
            }
        // 位置情報の追跡を開始
        _fusedLocationClient.requestLocationUpdates(_locationRequest, _onUpdateLocation,mainLooper)
    }
//    fun onStartLocationTrackingButtonClick(view: View) {
//        val serviceIntent = Intent(this, LocationTrackingService::class.java)
//        startService(serviceIntent)
//    }
}


//class LocationTrackingService : Service() {
//
//    private lateinit var _fusedLocationClient: FusedLocationProviderClient
//    private lateinit var _locationRequest: LocationRequest
//    private lateinit var _locationCallback: LocationCallback
//
//    override fun onCreate() {
//        super.onCreate()
//        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        _locationRequest = LocationRequest.create().apply {
//            interval = 10000 // 位置情報の取得間隔（ミリ秒）
//            fastestInterval = 5000 // 最速の位置情報の取得間隔（ミリ秒）
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//        _locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult?) {
//                locationResult?.lastLocation?.let {
//                    // ここで取得した位置情報を処理する
//                }
//            }
//
//            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
//                // 位置情報の利用可能性が変化した場合の処理
//            }
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        _fusedLocationClient.requestLocationUpdates(_locationRequest, _locationCallback, null)
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _fusedLocationClient.removeLocationUpdates(_locationCallback)
//    }
//}