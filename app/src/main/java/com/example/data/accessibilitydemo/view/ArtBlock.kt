package com.example.data.accessibilitydemo.view

import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import java.util.concurrent.atomic.AtomicInteger

/**
 * 艺术字方块
 * <br>
 * 交由SecureTestVIew进行绘制
 */
class ArtBlock(val charText:String,val left:Float,val top:Float) {
    val virtualId by lazy { globalVirtualId.getAndIncrement() }
    val right = left+100
    val bottom = top+100
    var charX = left+30
    var charY = top+70

    fun draw(canvas: Canvas,borderPaint: Paint, textPaint: TextPaint){
        canvas.apply {
            drawRect(left, top, right,bottom, borderPaint)
            drawText(charText,charX,charY,textPaint)
        }
    }

    fun isHint(x:Float,y:Float):Boolean{
        return ((x in left..right) && (y in top..bottom))
    }


    companion object{
        val globalVirtualId = AtomicInteger(289387)
    }
}