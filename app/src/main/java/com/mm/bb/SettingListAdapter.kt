package com.mm.bb

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

// settings listView adapter class
class SettingListAdapter(
    private val context: Activity,
    private val lvSettingName: Array<String>,
    private val lvSettingDescription: Array<String>,
    private val lvImageIcons: Array<Int>
) : ArrayAdapter<String>(context, R.layout.listview_settings, lvSettingName) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_settings, null, true)

        val settingName1 = rowView.findViewById(R.id.tvLvName) as TextView
        val settingDescription1 = rowView.findViewById(R.id.tvLvDescription) as TextView
        val settingIcon1 = rowView.findViewById(R.id.ivSettingsListView) as ImageView

        settingName1.text = lvSettingName[position]
        settingDescription1.text = lvSettingDescription[position]
        settingIcon1.setImageResource(lvImageIcons[position])

        return rowView
    }
}