package com.cnt57cl.image_sitting

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View

import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView

import pl.droidsonroids.gif.GifImageView

class Sondialog(context: Context,imgsrc:Int):Dialog(context) {

    var inflater: LayoutInflater?=null

    var  con:Context? = null
    var imgs:Int?=null
    var img:GifImageView?=null
    var messenger: TextView?=null
    init {
        window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
        this.con=context
        this.imgs=imgsrc
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        inflater= LayoutInflater.from(con)

        val view:View= inflater!!.inflate(R.layout.contensondialog,null)
        img=view.findViewById(R.id.img_gif)
        messenger=view.findViewById(R.id.text_view)
        setimage(imgs!!)
        setContentView(view)

        setanimationfortext()



    }


    fun setanimationfortext()
    {

        val anphal:AlphaAnimation = AlphaAnimation(1.0f,0.2f)

        anphal.duration=1200
        anphal.repeatCount=Animation.INFINITE
        anphal.repeatMode= Animation.REVERSE

        this.messenger?.startAnimation(anphal)

    }

    fun setmessenger(messenger:String)
    {
        this.messenger?.text=messenger


    }

    fun setimage(imgsrc:Int )
    {
        img?.setImageResource(imgsrc)
    }
}