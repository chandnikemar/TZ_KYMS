package com.example.tzpoc.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.text.Html
import androidx.appcompat.app.AlertDialog
import com.example.tzpoc.views.LoginActivity


class SessionManager(context: Context) {
    // Shared Preferences
    var sharedPrefer: SharedPreferences

    // Editor for Shared preferences
    var editor: SharedPreferences.Editor
    // Context
    var context: Context
    // Shared Pref mode
    var PRIVATE_MODE = 0
    // Constructor
    init {
        this.context = context
        sharedPrefer = context.getSharedPreferences(Constants.SHARED_PREF, PRIVATE_MODE)
        editor = sharedPrefer.edit()
    }

    /**
     * Call this method on/after login to store the details in session
     */
    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()

        user["userName"] = sharedPrefer.getString(KEY_USER_NAME, null)
        user["jwtToken"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        user["refreshToken"] = sharedPrefer.getString(KEY_REFRESH_TOKEN, null)
//        user[Constants.LOCATION_ID] = sharedPrefer.getString(Constants.LOCATION_ID, null)
//        user[Constants.KEY_SERVER_IP] = sharedPrefer.getString(Constants.KEY_SERVER_IP, null)
//        user[Constants.KEY_HTTP] = sharedPrefer.getString(Constants.KEY_HTTP, null)
        return user
    }
    fun createLoginSession(
        firstName: String?,
        lastName: String?,
        email: String?,
        mobileNumber: String?,
        isVerified: String?,
        userName: String?,
        jwtToken: String?,
        refreshToken: String?,
        roleName:String?,
        id:Int?,
        coordinates:String?,
        locationId:String

    ) {

        //editor.putString(KEY_USERID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_FIRST_NAME, firstName)
        editor.putString(KEY_USER_LAST_NAME, lastName)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_MOBILE_NUMBER, mobileNumber)
        editor.putString(KEY_USER_IS_VERIFIED, isVerified)
        editor.putString(USER_COORDINATES, coordinates)
        editor.putString(Key_LOCATION_ID, locationId)


        //editor.putString(KEY_RDT_ID, rdtId)
        //editor.putString(KEY_TERMINAL, terminal)
        //editor.putBoolean(KEY_ISLOGGEDIN, true)
        editor.putString(KEY_JWT_TOKEN, jwtToken)
        editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        editor.putString(ROLE_NAME, roleName)
        id?.let { editor.putInt(KEY_USER_ID, it) }

        // commit changes
        editor.commit()
    }
    fun saveSelectedLocation(locationId: String) {
        val editor = sharedPrefer.edit()
        editor.putString("selected_location", locationId)
        editor.apply()
    }

    fun getSelectedLocation(): String? {
        return sharedPrefer.getString("selected_location", null) // Return null if no value exists
    }

    fun clearSelectedLocation() {
        val editor = sharedPrefer.edit()
        editor.remove("selected_location")
        editor.apply()
    }
    fun logoutUser() {
        editor.remove(KEY_JWT_TOKEN)
        editor.remove(KEY_REFRESH_TOKEN)
        editor.remove("selected_location_name")
        editor.remove("selected_location_id")

        editor.putBoolean(Constants.LOGGEDIN, false)
        editor.apply()
        editor.commit()
    }

    /**
     * Call this method anywhere in the project to Get the stored session data
     */
    /*fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user["userId"] = sharedPrefer.getString(KEY_USERID, null)
        user["userName"] = sharedPrefer.getString(KEY_USER_NAME, null)
        user["rdtId"] = sharedPrefer.getString(KEY_RDT_ID, null)
        user["terminal"] = sharedPrefer.getString(KEY_TERMINAL, null)
        user["jwtToken"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        user["refreshToken"] = sharedPrefer.getString(KEY_REFRESH_TOKEN, null)
        return user
    }*/

    /*  fun getHeaderDetails(): HashMap<String, String?> {
          val user_header = HashMap<String, String?>()
          user_header["UserId"] = sharedPrefer.getString(KEY_USERID, null)
          user_header["RDTId"] = sharedPrefer.getString(KEY_RDT_ID, null)
          user_header["TerminalId"] = sharedPrefer.getString(KEY_TERMINAL, null)
          user_header["Token"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
          return user_header
      }
  */
    fun isAlreadyLoggedIn(): HashMap<String, Boolean> {
        val user = HashMap<String, Boolean>()
        user["isLoggedIn"] = sharedPrefer.getBoolean(KEY_ISLOGGEDIN, false)
        return user
    }

    fun getAdminDetails(): HashMap<String, String?> {
        val admin = HashMap<String, String?>()
        admin["serverIp"] = sharedPrefer.getString(KEY_SERVER_IP, null)
        admin["port"] = sharedPrefer.getString(KEY_PORT, null)
        return admin
    }

    fun getToken(): String{
        val token = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        return token?:""
    }
    fun updateJwtToken(jwtToken: String) {
        editor.putString(KEY_JWT_TOKEN, jwtToken)
        editor.commit()
    }

    fun getJwtToken(): String? {
        return sharedPrefer.getString(KEY_JWT_TOKEN, null)
    }
    fun saveJwtToken(token: String) {
        return  sharedPrefer.edit().putString("jwt_token", token).apply()
    }
    fun getRefreshToken(): String? {
        return sharedPrefer.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getRole(): String{
        val role = sharedPrefer.getString(ROLE_NAME, null)
        return role?:""
    }

    fun getUserName(): String{
        val userName = sharedPrefer.getString(KEY_USER_NAME, null)
        return userName?:""
    }

    fun saveAdminDetails(serverIp: String?, http: String?) {
//        editor.putString(Constants.KEY_SERVER_IP, serverIp)
//        editor.putString(Constants.KEY_HTTP, http)
        editor.putBoolean(KEY_ISLOGGEDIN, false)
        editor.commit()
    }


    fun clearSharedPrefs() {
        editor.clear()
        editor.commit()
    }
    fun showCustomDialog(title: String?, message: String?,context: Activity) {
        var alertDialog: AlertDialog? = null
        val builder: AlertDialog.Builder
        if (title.equals(""))
            builder = AlertDialog.Builder(context)
                .setMessage(Html.fromHtml(message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else if (message.equals(""))
            builder = AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else
            builder = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    if (title.equals("Session Expired")) {
                        logout(context)
                    } else {
                        alertDialog?.dismiss()
                    }
                }
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun logout(context: Activity) {
        logoutUser()
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        //context.finish()
        context.finishAfterTransition()
    }

    companion object {
        private const val PREF_NAME = "shared_pref"
        //const val KEY_USERID = Constants.USER_ID
        const val KEY_USER_ID = "id"
        const val KEY_USER_NAME = "userName"
        const val KEY_USER_FIRST_NAME = "firstName"
        const val KEY_USER_LAST_NAME= "lastName"
        const val KEY_USER_EMAIL = "email"
        const val KEY_USER_MOBILE_NUMBER = "mobileNumber"
        const val KEY_USER_IS_VERIFIED = "isVerified"
        const val Key_LOCATION_ID="locationId"
        const val ROLE_NAME = "roleName"
        // const val KEY_RDT_ID = Constants.RDT_ID
        //const val KEY_TERMINAL = Constants.TERMINAL_ID
        const val KEY_ISLOGGEDIN = "isLoggedIn"
        const val KEY_JWT_TOKEN = "jwtToken"
        const val KEY_REFRESH_TOKEN = "refreshToken"
        //Admin Shared Prefs
        const val KEY_SERVER_IP = "serverIp"
        const val USER_COORDINATES = "coordinates"
        const val KEY_PORT = "port"


    }
    fun saveCoordinates(coordinates: String?) {
//        editor.remove(Constants.KEY_COORDINATE)
//        editor.putString(Constants.KEY_COORDINATE, coordinates)
        editor.commit()
    }


    //    fun getCoordinates(): String? {
//        return sharedPrefer.getString(Constants.KEY_COORDINATE, null)
//    }
    fun showToastAndHandleErrors(resultResponse: String,context: Activity) {

        when (resultResponse) {
            "java.net.ConnectException: Failed to connect" -> {
                // Handle connection failure error
                // Show a toast message or display a dialog
            }
            Constants.SESSION_EXPIRE, "Authentication token expired", Constants.CONFIG_ERROR -> {
                showCustomDialog(
                    "Session Expired",
                    "Please re-login to continue",
                    context
                )
            }

        }
    }
    fun showAlertMessage(context: Activity) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setMessage("The location permission is disabled. Do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                context.startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    10
                )
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                context.finish()
            }
        val alert: android.app.AlertDialog = builder.create()
        alert.show()
    }}

//    fun loadLocationPoints(): ArrayList<LatLng>? {
//        val json = sharedPrefer.getString(Constants.KEY_COORDINATE, null)
//
//        if (json != null) {
//            try {
//                val locationList: ArrayList<LatLng> = ArrayList()
//                val jsonArray = JSONArray(json)
//                for (i in 0 until jsonArray.length()) {
//                    val jsonObject = jsonArray.getJSONObject(i)
//                    if (jsonObject.has(Constants.LATITUDE) && jsonObject.has(Constants.LONGITUDE)) {
//                        val latitude = jsonObject.getDouble(Constants.LATITUDE)
//                        val longitude = jsonObject.getDouble(Constants.LONGITUDE)
//                        val latLng = LatLng(latitude, longitude)
//                        locationList.add(latLng)
//                    }
//                }
//                return locationList
//            } catch (e: JSONException) {
//                // Handle JSON parsing error
//                e.printStackTrace()
//            }
//        }
//
//        return null
//    }
//
//}
