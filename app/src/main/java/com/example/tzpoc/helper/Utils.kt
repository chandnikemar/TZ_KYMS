package com.example.tzpoc.api

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.tzpoc.helper.Constants


object Utils {
    fun toast(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun setSharedPrefs(context: Context, key: String?, value: String?) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getSharedPrefs(context: Context, key: String?): String? {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getString(key, "")
    }

    fun getSharedPrefs(context: Context, key: String?, defValue: String?): String? {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getString(key, defValue)
    }

    fun setSharedPrefsInteger(context: Context, key: String?, value: Int) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getSharedPrefsInteger(context: Context, key: String?): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, 0)
    }

    fun getSharedPrefsDefaultIndex(context: Context, key: String?): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, -1)
    }

    fun getSharedPrefsInteger(context: Context, key: String?, defValue: Int): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, defValue)
    }

    fun setSharedPrefsLong(context: Context, key: String?, value: Long) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getSharedPrefsLong(context: Context, key: String?): Long {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getLong(key, 0)
    }

    fun setSharedPrefsBoolean(context: Context, key: String?, value: Boolean) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getSharedPrefsBoolean(context: Context, key: String?): Boolean {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(key, true)
    }

    fun getSharedPrefsBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(key, defValue)
    }

    fun removeSharedPrefs(context: Context, key: String?) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(key)
        editor.apply()
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

   /* fun handleGeneralResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { generalResponse ->
                return Resource.Success(generalResponse)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString("errorMessage")
            }
        }
        return Resource.Error(errorMessage)
    }*/

    fun hasInternetConnection(application: Application): Boolean {
        val connectivityManager = application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}