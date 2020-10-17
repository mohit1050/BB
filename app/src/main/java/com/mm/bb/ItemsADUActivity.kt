package com.mm.bb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.room.Room
import androidx.transition.TransitionManager
import com.example.bb_items_roomdb.RoomDB.AppDb
import com.example.bb_items_roomdb.RoomDB.ItemEntity
import kotlinx.android.synthetic.main.activity_items_a_d_u.*

class ItemsADUActivity : AppCompatActivity() {

    lateinit var modifyId: String
    private var boolProcessWithGST: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_a_d_u)

        /* Check  if activity opened from List Item Click */
        if (intent.hasExtra("id")) {
            modifyId = intent.getStringExtra("id").toString()
            Toast.makeText(this, "Item Id: $modifyId", Toast.LENGTH_LONG).show()
            etItemActivitystatus.text = "Edit Item"

            etName.setText(intent.getStringExtra("name"))
            etQuantity.setText(intent.getStringExtra("quantity"))
            etGst.setText(intent.getStringExtra("gst"))
            etAmount.setText(intent.getStringExtra("amount"))
            btnAdd.visibility = View.GONE
        } else {
            etItemActivitystatus.text = "Add new item"
            btnUpdate.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }
        smGSTtoggle.setOnCheckedChangeListener { _, isChecked ->
            etGst.isEnabled = isChecked
            etGst.text?.clear()
            tilGST.error = null
            boolProcessWithGST = false // set false if toggle switch is off
        }
    }

    fun addItems(view: View) {
        val name1 = etName.text.toString().trim()
        val quantity1 = etQuantity.text.toString().trim()
        val gst1 = etGst.text.toString().trim()
        val amount1 = etAmount.text.toString().trim()
        fun showToast() {
            runOnUiThread(kotlinx.coroutines.Runnable {
                Toast.makeText(this, "$name1 added", Toast.LENGTH_LONG).show()
            }) // runnable is used because activity is immediately killed and toast cannot be sowed hence it is to be done on ui tread
        }
        if (isValidInput()) {
            //Insert Case
            val thread = Thread {
                val db = Room.databaseBuilder(this, AppDb::class.java, "BookDB")
                    .build()
                val item = ItemEntity()
                item.itemName = name1
                item.quantity = quantity1
                item.amount = amount1
                item.subTotal = (amount1.toInt() * quantity1.toInt()).toString()

                if (boolProcessWithGST) {
                    item.gst = gst1
                    item.taxTotal = (quantity1.toInt() * gst1.toInt()).toString()
                    db.abstractItemDao().saveItems(item)
                    showToast()
                } else {
                    db.abstractItemDao().saveItems(item)
                    showToast()
                }
            }
            thread.start()
            finish()
        }
    }

    fun update(view: View) {
        val name1 = etName.text.toString()
        val quantity1 = etQuantity.text.toString()
        val gst1 = etGst.text.toString()
        val amount1 = etAmount.text.toString()
        fun showToast() {
            runOnUiThread(kotlinx.coroutines.Runnable {
                Toast.makeText(this, "$name1 updated", Toast.LENGTH_LONG).show()
            }) // runnable is used because activity is immediately killed and toast cannot be sowed hence it is to be done on ui tread
        }
        if (isValidInput()) {
            val thread = Thread {
                val db = Room.databaseBuilder(this, AppDb::class.java, "BookDB").build()
                val item = ItemEntity()
                item.itemId = modifyId.toInt()  // to edit item according to primary key

                item.itemName = name1
                item.quantity = quantity1
                item.amount = amount1
                item.subTotal = (amount1.toInt() * quantity1.toInt()).toString()

                if (boolProcessWithGST) {
                    item.gst = gst1
                    item.taxTotal = (quantity1.toInt() * gst1.toInt()).toString()
                    db.abstractItemDao().updateItem(item)
                    showToast()
                } else {
                    db.abstractItemDao().updateItem(item)
                    showToast()
                }
            }
            thread.start()
            finish()
        }
    }

    fun delete(view: View) {
        val name1 = etName.text.toString()
        val thread = Thread {
            val db = Room.databaseBuilder(this, AppDb::class.java, "BookDB").build()

            val id = ItemEntity()
            id.itemId = modifyId.toInt()
            db.abstractItemDao().deleteItem(id)
            runOnUiThread(kotlinx.coroutines.Runnable {
                Toast.makeText(this, "$name1 deleted", Toast.LENGTH_LONG).show()
            })
        }
        thread.start()
        finish()
    }

    // Custom method to validate form inputted data
    private fun isValidInput(): Boolean {
        val name1 = etName.text.toString().trim()
        val quantity1 = etQuantity.text.toString().trim()
        val gst1 = etGst.text.toString().trim()
        val amount1 = etAmount.text.toString().trim()
        var isValid = true

        TransitionManager.beginDelayedTransition(root_layout)
        if (name1.isEmpty()) {
            tilName.isErrorEnabled = true
            tilName.error = "Required"
            isValid = false
        } else tilName.isErrorEnabled = false
        when {
            quantity1.isEmpty() -> {
                tilQuantity.isErrorEnabled = true
                tilQuantity.error = "Required"
                isValid = false
            }
            quantity1.toInt() == 0 -> {
                tilQuantity.isErrorEnabled = true
                tilQuantity.error = "can't be 0"
                isValid = false
            }
            else -> tilQuantity.isErrorEnabled = false
        }
        when {
            amount1.isEmpty() -> {
                tilAmount.isErrorEnabled = true
                tilAmount.error = "Required"
                isValid = false
            }
            amount1.toInt() == 0 -> {
                tilAmount.isErrorEnabled = true
                tilAmount.error = "can't be 0"
                isValid = false
            }
            else -> tilAmount.isErrorEnabled = false
        }
        if (boolProcessWithGST) {
            when {
                gst1.isEmpty() -> {
                    tilGST.isErrorEnabled = true
                    tilGST.error = "Required"
                    isValid = false
                }
                gst1.toInt() == 0 -> {
                    tilGST.isErrorEnabled = true
                    tilGST.error = "can't be 0"
                    isValid = false
                }
                else -> tilGST.isErrorEnabled = false
            }
        }
        return isValid
    }
}