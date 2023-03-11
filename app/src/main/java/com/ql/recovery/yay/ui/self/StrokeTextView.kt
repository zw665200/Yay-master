package com.ql.recovery.yay.ui.self

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.RED
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/3/10 18:49
 */
class StrokeTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    private var strokeColor: Int = RED
    private var strokeWidth: Float = 5f
    private var backGroundText: TextView? = null

    init {
//        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
//        strokeColor = typedArray.getColor(R.styleable.StrokeTextView_strokeColor, RED)
//        strokeWidth = typedArray.getFloat(R.styleable.StrokeTextView_strokeWidth, 10f)
//        typedArray.recycle()

        backGroundText = TextView(context, attrs)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        backGroundText?.layoutParams = params
        super.setLayoutParams(params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //两个TextView上的文字必须一致
        val tt = backGroundText?.text
        if (tt == null || tt != text) {
            backGroundText?.text = text
            this.postInvalidate()
        }

        backGroundText?.measure(widthMeasureSpec, heightMeasureSpec)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        backGroundText?.layout(left, top, left, bottom)
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        // 保存原始文本画笔属性
        val originalPaint = backGroundText!!.paint

        // 设置描边画笔属性
        originalPaint.style = Paint.Style.FILL_AND_STROKE
        originalPaint.strokeWidth = strokeWidth
        originalPaint.color = strokeColor

        backGroundText?.gravity = Gravity.CENTER
        backGroundText?.draw(canvas)

        // 在原始文本位置绘制文本
        super.onDraw(canvas)
    }
}
