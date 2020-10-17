package com.mm.bb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.civUserProfileImage
import kotlinx.android.synthetic.main.activity_dashboard.tvUserEmail
import kotlinx.android.synthetic.main.activity_dashboard.tvUserName
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    val lvSettingName = arrayOf<String>(
        "Account",
        "How it works",
        "Report an issue",
        "Help"
    )
    val lvSettingDescription = arrayOf<String>(
        "Edit and Update account",
        "Learn about app",
        "Report Bugs",
        "FAQ, Privacy Policy"
    )

    val lvImageIcons = arrayOf<Int>(
        R.drawable.ic_settings,
        R.drawable.ic_settings,
        R.drawable.ic_settings,
        R.drawable.ic_settings,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        tvUserName.text = currentUser?.displayName
        tvUserEmail.text = currentUser?.email

        Glide.with(this).load(currentUser?.photoUrl).into(civUserProfileImage)

        val settingListAdapter =
            SettingListAdapter(this, lvSettingName, lvSettingDescription, lvImageIcons)
        lvSettings.adapter = settingListAdapter

        lvSettings.setOnItemClickListener() { adapterView, view, position, id ->
//            val itemAtPos = adapterView.getItemAtPosition(position)
//            val itemIdAtPos = adapterView.getItemIdAtPosition(position)
//            Toast.makeText(this, "$itemAtPos item id $itemIdAtPos", Toast.LENGTH_LONG).show()

            when (position) {
                0 -> {
                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                }
                1 -> {
//                    val intent = Intent(this, ShowQRActivity::class.java)
//                    startActivity(intent)
                }
                2 -> {
//                    val intent = Intent(this, ShowQRActivity::class.java)
//                    startActivity(intent)
                }
                3 -> {
//                    val intent = Intent(this, ShowQRActivity::class.java)
//                    startActivity(intent)
                }
            }
        }
    }
}