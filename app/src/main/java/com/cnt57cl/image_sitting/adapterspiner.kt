package com.cnt57cl.image_sitting

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SpinnerAdapter
import android.widget.TextView

class adapterspiner(context: Context,arr:ArrayList<String>) : SpinnerAdapter {

    var layoutInflater:LayoutInflater?=null
    var context:Context?=null
    var arr:ArrayList<String>?=null

    init
    {
        this.context=context
        this.arr=arr

    }


    override fun isEmpty(): Boolean {
        return  false
    }
    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view= LayoutInflater.from(context).inflate(R.layout.spinneritem,null,false)

        val img= view.findViewById<ImageView>(R.id.icon_type)
        val tv_title= view.findViewById<TextView>(R.id.tv_title)
        img.setImageResource(R.drawable.save_icon)
        tv_title.text= arr!!.get(position)

        return view
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {

    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItem(position: Int): Any {
       return arr!!.get(position)
    }

    override fun getViewTypeCount(): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
       return  position.toLong()
    }

    override fun hasStableIds(): Boolean {
    return false
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCount(): Int {
      return  arr!!.size
    }
}