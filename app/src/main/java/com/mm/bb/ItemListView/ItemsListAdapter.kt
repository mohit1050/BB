package com.mm.bb.ItemListView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mm.bb.R


class ItemsListAdapter(
    private val context: Context,
    private val ItemDataList: ArrayList<HashMap<String, String>>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return ItemDataList.size
    }

    override fun getItem(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var dataitem = ItemDataList[position]

        val rowView = inflater.inflate(R.layout.listview_items_list, parent, false)
        rowView.findViewById<TextView>(R.id.row_name).text = dataitem["name"]
        rowView.findViewById<TextView>(R.id.row_quantity).text = dataitem["quantity"]
        rowView.findViewById<TextView>(R.id.row_gst).text = dataitem["gst"] + "%"
        rowView.findViewById<TextView>(R.id.row_amount).text = dataitem["amount"]



        rowView.tag = position
        return rowView
    }
}