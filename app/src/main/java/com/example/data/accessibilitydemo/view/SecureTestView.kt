package com.example.data.accessibilitydemo.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 * 一个使用另一个线程绘制内容的View
 * <br>
 * 可以通过setSecure切换是否可以在非安全屏幕显示以及控制屏幕捕获
 */
class SecureTestView : SurfaceView, SurfaceHolder.Callback, Runnable {
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mCanvas: Canvas
    private lateinit var paint: Paint
    private lateinit var textPaint: TextPaint
    private val provider = MyAccessibilityNodeInfoProvider()

    var onArtBlockClickListener:OnArtBlockClickListener? = null


    private val drawContents = arrayListOf<ArtBlock>()

    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        textPaint = TextPaint().apply {
            color = Color.RED
            strokeWidth = 2f
            style = Paint.Style.STROKE
            textSize = 64f
        }

        var left = 20f

        //要绘制的内容
        arrayOf("A", "B", "C", "D", "E", "F").forEach { text ->
            val block = ArtBlock(text, left, 20f)
            drawContents.add(block)
            left = block.right + 20
        }

        // 返回SurfaceHolder，提供对该SurfaceView的基础表面的访问和控制
        mSurfaceHolder = holder
        //注册回调方法
        mSurfaceHolder.addCallback(this)
        //画布透明处理
        setZOrderOnTop(true)
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT)

        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP){
                onArtBlockClickListener?.let { listener->
                    for (artBlock in drawContents){
                        if (artBlock.isHint(event.x,event.y)){
                            listener.onArtBlockClick(artBlock)
                            return@setOnTouchListener true
                        }
                    }
                }
            }

            return@setOnTouchListener true
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Surface创建时触发
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface改变时触发
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface销毁时触发
    }

    override fun run() {
        drawing()
    }

    // 绘制内容
    private fun drawing() {
        mCanvas = mSurfaceHolder.lockCanvas()
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        drawContents.forEach { artBlock ->
            artBlock.draw(mCanvas, borderPaint = paint, textPaint)
        }

        mSurfaceHolder.unlockCanvasAndPost(mCanvas)
    }

    /**
     * 初始化SecureTestView自身无障碍节点信息
     */
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        //此处可以修改SecureTestView的无障碍节点信息，这里修改了类名称作为例子
        info.className = "SecureTestView"
        val infoCompat = AccessibilityNodeInfoCompat.wrap(info)
        drawContents.forEach { artBlock ->
            //为SecureTextView节点添加绘制内容的虚拟无障碍信息节点
            infoCompat.addChild(this, artBlock.virtualId)
        }
    }

    override fun getAccessibilityNodeProvider(): AccessibilityNodeProvider {
        return provider
    }

    /**
     * 实现自身的无障碍信息节点提供者
     */
    inner class MyAccessibilityNodeInfoProvider:AccessibilityNodeProvider(){

        override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
            //HOST_VIEW_ID则为SecureTestView自身，我们需要为其创建AccessibilityNodeInfo
            if (virtualViewId == HOST_VIEW_ID) {
                val nodeInfo = AccessibilityNodeInfo.obtain(this@SecureTestView)
                //需要手动调用原onInitializeAccessibilityNodeInfo方法，否则数据不会得到填充
                onInitializeAccessibilityNodeInfo(nodeInfo)
                return nodeInfo
            } else {
                //以下提供SecureTestView绘制的内容的虚拟AccessibilityNodeInfo
                drawContents.firstOrNull { it.virtualId == virtualViewId }?.let { artBlock ->
                    val nodeInfo = AccessibilityNodeInfoCompat.wrap(AccessibilityNodeInfo.obtain())
                    //需要设置来源，否则只会有第一项出现在树中
                    nodeInfo.setSource(this@SecureTestView,virtualViewId)
                    nodeInfo.className = "ArtBlock"
                    nodeInfo.uniqueId = "artblock_${artBlock.virtualId}"
                    nodeInfo.viewIdResourceName = "artblock_${artBlock.virtualId}"
                    //注意，根据绘制情况设置是否对用户可见，将影响自动化分析界面时的过滤
                    nodeInfo.isVisibleToUser = true
                    //设置其在屏幕中的位置
                    nodeInfo.setBoundsInScreen(
                        Rect(
                            (left + artBlock.left).toInt(),
                            (top + artBlock.top).toInt(),
                            (left + artBlock.right).toInt(),
                            (top + artBlock.bottom).toInt()
                        )
                    )
                    nodeInfo.text = artBlock.charText
                    //SecureTestView中做了绘制内容的点击事件响应，这里将clickable设置为true
                    nodeInfo.isClickable = true
                    return nodeInfo.unwrap()
                }

            }

            return super.createAccessibilityNodeInfo(virtualViewId)
        }
    }

    companion object {
        const val TAG = "SecureTestView"
    }

}
