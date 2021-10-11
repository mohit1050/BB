package com.mm.bb

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.util.*
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.Window

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.dialog_show_qr.*

import android.graphics.drawable.ColorDrawable
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_show_q_r.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.*
import androidx.room.Room
import com.example.bb_items_roomdb.RoomDB.AppDb
import com.mm.bb.ItemListView.ItemsListAdapter
import java.io.File
import android.widget.ArrayAdapter
import com.mm.bb.ItemListView.InvoiceListAdapter
import kotlinx.android.synthetic.main.activity_items_list.*


class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userName: String
    private lateinit var currentUser: FirebaseUser
    private val collectionRef = Firebase.firestore.collection("Users")
    private lateinit var scanneUID: String
    private var dataList = ArrayList<HashMap<String, String>>()
    private var filelist = ArrayList<File>()


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

    override fun onResume() {
        super.onResume()
        loadIntoList()
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
//                    Toast.makeText(this, "Scanned: $scanneUID", Toast.LENGTH_LONG).show()

                    mAuth = FirebaseAuth.getInstance()
                    currentUser = mAuth.currentUser!!

                    try {
                        retrieveData()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    }
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
                    if (document.exists() && document != null) {
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


                        Toast.makeText(
                            this@DashboardActivity,
                            "Data Retrieved",
                            Toast.LENGTH_SHORT
                        )
                            .show()
//                   tvYourName.setText(document.data)
                        val intent =
                            Intent(this@DashboardActivity, ItemsListActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this@DashboardActivity,
                            "User not found",
                            Toast.LENGTH_LONG
                        )
                            .show()
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


    private fun loadIntoList() {
        dataList.clear()
        val invoicePath =
            getExternalFilesDir("")!!.absolutePath + "/" //location where the pdf will store
// get list of pdf from data/files directory

        val dir = File(invoicePath)


        var filelist: Array<File>? = null
        if (filelist != null) {
            filelist.drop(0)
        }

        dir.listFiles().also { filelist = it }
        Arrays.sort(
            filelist,
            Comparator.comparingLong { obj: File -> obj.lastModified() }.reversed()
        )

        val theNamesOfFiles = arrayOfNulls<String>(filelist!!.size)
        for (i in theNamesOfFiles.indices) {
            theNamesOfFiles[i] = filelist!![i].name

            val filename = theNamesOfFiles[i]
            Log.d("909", filename.toString())
//            val filepath = "$invoicePath$filename"

            val listFileName = theNamesOfFiles[i]!!.replace(".pdf", "")

            val map = HashMap<String, String>()
            map["file_name"] = listFileName
//            map["file_name"] = theNamesOfFiles[i].toString()
            dataList.add(map)
        }

        runOnUiThread(kotlinx.coroutines.Runnable {
            findViewById<ListView>(R.id.lvInvoiceList).adapter =
                InvoiceListAdapter(this, dataList)
        })  // used this because it cannot be accessed outside of main thread (to fix activity crash on pressing back and error loading listview

        // to open list item on onClick form listview
        findViewById<ListView>(R.id.lvInvoiceList).setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, ActivityViewInvoices::class.java)

            val clickedFileName = theNamesOfFiles[i]
            val filepath = "$invoicePath$clickedFileName"
            intent.putExtra("invoice_path", filepath)
//            Toast.makeText(this, clickedFileName, Toast.LENGTH_LONG).show()
            startActivity(intent)
        }

        if (filelist!!.size > 4) {
            animation_view_file_list.visibility = View.GONE
        }
        if (filelist!!.size > 1) {
            tv_your_inv_list_here.visibility = View.GONE
            tv_scan_to_get_started.visibility = View.GONE
        }
    }


}