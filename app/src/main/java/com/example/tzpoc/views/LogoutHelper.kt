import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.views.LoginActivity


object  LogoutHelper {

    fun showLogoutDialog(context: Context) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, which ->
                logout(context)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }

    private fun logout(context: Context) {
        val session = SessionManager(context)
        session.logoutUser()
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
}
