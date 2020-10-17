package com.mm.bb

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        tvUserName.text = currentUser?.displayName
        tvUserEmail.text = currentUser?.email
        Glide.with(this).load(currentUser?.photoUrl).into(civUserProfileImage)


        val time: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val date: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvTime.text = time
        tvDate.text = date

    }


    fun appSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun showQRCode(view: View) {
        // later implement show qr in a dialog
        val intent = Intent(this, ShowQRActivity::class.java)
        startActivity(intent)
    }

    fun scanQRCode(view: View) {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        scanner.setBeepEnabled(false)
        scanner.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    fun invoiceProcess(view: View) {
        val intent = Intent(this, ItemsListActivity::class.java)
        startActivity(intent)
    }

    fun accountSettings(view: View) {}

}