package com.mm.bb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.room.Room
import com.example.bb_items_roomdb.RoomDB.AppDb
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mm.bb.ItemListView.ItemsListAdapter

class ItemsListActivity : AppCompatActivity() {

    var dataList = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_list)
    }

    private fun loadIntoList() {
        dataList.clear()
        val db = Room.databaseBuilder(applicationContext, AppDb::class.java, "BookDB").build()

        val thread = Thread {
            db.abstractItemDao().getAllItems().forEach()
            {

                val map = HashMap<String, String>()
                map["id"] = it.itemId.toString()
                map["name"] = it.itemName
                map["quantity"] = it.quantity
                map["gst"] = it.gst
                map["amount"] = it.amount
                dataList.add(map)
            }

            runOnUiThread(kotlinx.coroutines.Runnable {
                findViewById<ListView>(R.id.listView).adapter =
                    ItemsListAdapter(this@ItemsListActivity, dataList)
            })  // used this because it cannot be accessed outside of main thread (to fix activity crash on pressing back and error loading listview

            // to open list item on onClick form listview
            findViewById<ListView>(R.id.listView).setOnItemClickListener { _, _, i, _ ->
                val intent = Intent(this, ItemsADUActivity::class.java)
                intent.putExtra("id", dataList[i]["id"])
                intent.putExtra("name", dataList[i]["name"])
                intent.putExtra("quantity", dataList[i]["quantity"])
                intent.putExtra("gst", dataList[i]["gst"])
                intent.putExtra("amount", dataList[i]["amount"])
                startActivity(intent)
            }
        }
        thread.start()
    }

    fun addItem(v: View) {
        val intent = Intent(this, ItemsADUActivity::class.java)
        startActivity(intent)
    }


    public override fun onResume() {
        super.onResume()
        loadIntoList()
    }

    override fun onBackPressed() {
        finish()
    }

    fun deleteAll(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Items")
            .setMessage("Are you sure, you want to delete all the Items?")
            .setCancelable(false)
            .setNeutralButton("Cancel") { _, _ ->
                // Respond to neutral button press
            }
            .setPositiveButton("Yes") { _, _ ->
                Thread {
                    val db = Room.databaseBuilder(this, AppDb::class.java, "BookDB").build()
                    db.abstractItemDao().deleteAllItems()
                }.start()
                Toast.makeText(this, "deleted", Toast.LENGTH_LONG).show()
                onResume()
            }
            .show()
    }

    fun generatePDF(view: View) {
        val intent = Intent(this, CreateViewPDFActivity::class.java)
        startActivity(intent)
    }

}