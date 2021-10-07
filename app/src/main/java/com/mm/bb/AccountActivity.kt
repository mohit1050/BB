package com.mm.bb

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.transition.TransitionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AccountActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val collectionRef = Firebase.firestore.collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        retrieveData()
    }


    private fun retrieveData() {
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = Firebase.firestore.collection("Users").document(currentUser.uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
//                    Log.d("101", "DocumentSnapshot data: ${document.data}")

                        val uName = (document.getString("yourName")).toString()
                        val uBusinessName = (document.getString("businessName")).toString()
                        val uContact = (document.getString("contact")).toString()
                        val uBuildingStreet = (document.getString("buildingStreet")).toString()
                        val uLandmark = (document.getString("landmark")).toString()
                        val uCityPin = (document.getString("cityPin")).toString()
                        val uWebsite = (document.getString("website")).toString()

                        etYourName.setText(uName)
                        etBusinessName.setText(uBusinessName)
                        etContactNo.setText(uContact)
                        etBuildingStreet.setText(uBuildingStreet)
                        etLandmark.setText(uLandmark)
                        etCityPin.setText(uCityPin)
                        etWebsite.setText(uWebsite)

                        val sharedPref = getSharedPreferences("userData", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.apply {
                            //save values to local storage also
                            putString("uName", uName)
                            putString("uBusinessName", uBusinessName)
                            putString("uContact", uContact)
                            putString("uBuildingStreet", uBuildingStreet)
                            putString("uLandmark", uLandmark)
                            putString("uCityPin", uCityPin)
                            putString("uWebsite", uWebsite)
                            apply()
                        }

                        Toast.makeText(this@AccountActivity, "Data Retrieved", Toast.LENGTH_SHORT)
                            .show()
//                   tvYourName.setText(document.data)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@AccountActivity,
                        "Failed to retrieve data try again later",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
        }
    }


    // save data on fireStore
    fun btnUpload(view: View) {
        if (isValidInput()) {

            // data is sent to AccountFSDataClass
            val dcYourName = etYourName.text.toString().trim()
            val dcBusinessName = etBusinessName.text.toString().trim()
            val dcContact = etContactNo.text.toString().trim()
            val dcBuildingStreet = etBuildingStreet.text.toString().trim()
            val dcLandmark = etLandmark.text.toString().trim()
            val dcCityPin = etCityPin.text.toString().trim()
            val dcWebsite = etWebsite.text.toString().trim()

            val accountFSDataCalss = AccountFSDataClass(
                dcYourName,
                dcBusinessName,
                dcContact,
                dcBuildingStreet,
                dcLandmark,
                dcCityPin,
                dcWebsite
            )
            saveData(accountFSDataCalss)
        }

    }

    private fun saveData(accountFSDataClass: AccountFSDataClass) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                collectionRef.document(currentUser.uid).set(accountFSDataClass).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AccountActivity,
                        "Successfully Saved Data",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                    val intent = Intent(this@AccountActivity, DashboardActivity::class.java)
                    startActivity(intent)

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AccountActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

    // Custom method to validate form inputted data
    private fun isValidInput(): Boolean {
        val yourName1 = etYourName.text.toString().trim()
        val businessName1 = etBusinessName.text.toString().trim()
        val contact1 = etContactNo.text.toString().trim()
        val buildingStreet1 = etBuildingStreet.text.toString().trim()
        val landmark1 = etLandmark.text.toString().trim()
        val cityPin1 = etCityPin.text.toString().trim()
        val website1 = etWebsite.text.toString().trim()
        var isValid = true

        TransitionManager.beginDelayedTransition(rootLayout)
        if (yourName1.isEmpty()) {
            tilYourName.isErrorEnabled = true
            tilYourName.error = "Required"
            isValid = false
        } else tilYourName.isErrorEnabled = false

        if (businessName1.isEmpty()) {
            tilBusinessName.isErrorEnabled = true
            tilBusinessName.error = "Required"
            isValid = false
        } else tilBusinessName.isErrorEnabled = false

        when {
            contact1.isEmpty() -> {
                tilContactNo.isErrorEnabled = true
                tilContactNo.error = "Required"
                isValid = false
            }
            contact1.length < 10 -> {
                tilContactNo.isErrorEnabled = true
                tilContactNo.error = "10 digit mobile no. required"
                isValid = false
            }
            else -> tilContactNo.isErrorEnabled = false
        }

        if (buildingStreet1.isEmpty()) {
            tilBuildingStreet.isErrorEnabled = true
            tilBuildingStreet.error = "Required"
            isValid = false
        } else tilBuildingStreet.isErrorEnabled = false

        if (landmark1.isEmpty()) {
            tilLandmark.isErrorEnabled = true
            tilLandmark.error = "Required"
            isValid = false
        } else tilLandmark.isErrorEnabled = false

        if (cityPin1.isEmpty()) {
            tilCityPin.isErrorEnabled = true
            tilCityPin.error = "Required"
            isValid = false
        } else tilCityPin.isErrorEnabled = false

        if (website1.isEmpty()) {
            tilWebsite.isErrorEnabled = true
            tilWebsite.error = "Required"
            isValid = false
        } else tilWebsite.isErrorEnabled = false

        return isValid
    }

    fun signout(view: android.view.View) {
        mAuth.signOut()
        finish()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

}