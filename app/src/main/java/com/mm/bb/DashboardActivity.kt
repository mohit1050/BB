package com.mm.bb

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button

import android.widget.EditText
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.dialog_show_qr.*

import android.widget.TextView

import android.graphics.drawable.ColorDrawable
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_show_q_r.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.SharedPreferences


class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userName: String
    private lateinit var currentUser: FirebaseUser
    private val collectionRef = Firebase.firestore.collection("Users")
    private lateinit var scanneUID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userName = currentUser?.displayName.toString()
        ("Welcome $userName").also { tvUserName.text = it }

//        tvUserEmail.text = currentUser?.email
        val userEmail = currentUser?.email
        Glide.with(this).load(currentUser?.photoUrl).into(civUserProfileImage)


        val time: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val date: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
//        tvTime.text = time
//        tvDate.text = date

        val sharedPref = getSharedPreferences("userData", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("isFirstRun", true)) {
            val editor = sharedPref.edit()
            editor.apply {
                //values retrieved from firebase(customer)
                putString("uEmail", userEmail)
                putBoolean("isFirstRun", false)
                apply()
            }

            finish()
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }


    fun userAccount(view: View) {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }

    fun showQRCode(view: View) {
//        // later implement show qr in a dialog
//        val intent = Intent(this, ShowQRActivity::class.java)
//        startActivity(intent)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_show_qr)

        val showQRiv = dialog.findViewById<View>(R.id.ivShowQR) as ImageView
        val showUserName = dialog.findViewById<View>(R.id.tvDialogUsername) as TextView

        // get uid from firebase
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        val uid = currentUser?.uid
        // generate and set image to image view
        val bitmap = uid?.let { generateQRCode(it) }
        showQRiv.setImageBitmap(bitmap)
//        tvQRtext.text = uid
        showUserName.text = userName
        dialog.show()

//        btnDismissCustomDialog.setOnClickListener {
//            dialog.dismiss()
//        }

    }


    private fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
            Log.d("1002", "generateQRCode: ${e.message}")
        }
        return bitmap
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
                    scanneUID = result.contents
                    Toast.makeText(this, "Scanned: $scanneUID", Toast.LENGTH_LONG).show()

                    mAuth = FirebaseAuth.getInstance()
                    currentUser = mAuth.currentUser!!
                    retrieveData()
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    fun retrieveData() {
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = Firebase.firestore.collection("Users").document(scanneUID)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
//                    Log.d("101", "DocumentSnapshot data: ${document.data}")

                        val yourName = document.getString("yourName")
                        val businessName = document.getString("businessName")
                        val contact = document.getString("contact")
                        val buildingStreet = document.getString("buildingStreet")
                        val landmark = document.getString("landmark")
                        val cityPin = document.getString("cityPin")
                        val website = document.getString("website")

                        val sharedPref = getSharedPreferences("userData", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.apply {
                            //values retrieved from firebase(customer)
                            putString("sName", yourName)
                            putString("sBusinessName", businessName)
                            putString("sContact", contact)
                            putString("sBuildingStreet", buildingStreet)
                            putString("sLandmark", landmark)
                            putString("sCityPin", cityPin)
                            putString("sWebsite", website)
                            apply()
                        }


                        Toast.makeText(this@DashboardActivity, "Data Retrieved", Toast.LENGTH_SHORT)
                            .show()
//                   tvYourName.setText(document.data)

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@DashboardActivity,
                        "Failed to get data please try again",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
        }
    }

    fun invoiceProcess(view: View) {
        val intent = Intent(this, ItemsListActivity::class.java)
        startActivity(intent)
    }

    fun accountSettings(view: View) {}


}