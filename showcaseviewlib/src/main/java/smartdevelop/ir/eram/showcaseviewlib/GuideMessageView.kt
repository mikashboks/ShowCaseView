package smartdevelop.ir.eram.showcaseviewlib

import android.content.Context
import android.graphics.*
import android.text.Spannable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by Mohammad Reza Eram  on 20/01/2018.
 */
internal class GuideMessageView(context: Context) : LinearLayout(context) {
    private val mPaint: Paint
    private val mRect: RectF
    private val mTitleTextView: TextView
    private val mContentTextView: TextView
    private val padding: Int
    private val paddingBetween: Int
    fun setTitle(title: String?) {
        if (title == null) {
            removeView(mTitleTextView)
            return
        }
        mTitleTextView.text = title
    }

    private fun setContentVisible(value: Boolean) {
        if (value) {
            mContentTextView.visibility = View.VISIBLE
            mTitleTextView.setPadding(padding, padding, padding, paddingBetween)
        } else {
            mContentTextView.visibility = View.GONE
            mTitleTextView.setPadding(padding, padding, padding, padding)
        }
    }

    fun setContentText(content: String?) {
        mContentTextView.text = content
        setContentVisible(!TextUtils.isEmpty(content))
    }

    fun setContentSpan(content: Spannable?) {
        mContentTextView.text = content
        mContentTextView.visibility = View.VISIBLE
    }

    fun setContentTypeFace(typeFace: Typeface?) {
        mContentTextView.typeface = typeFace
    }

    fun setTitleTypeFace(typeFace: Typeface?) {
        mTitleTextView.typeface = typeFace
    }

    fun setTitleTextSize(size: Int) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setContentTextSize(size: Int) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setColor(color: Int) {
        mPaint.alpha = 255
        mPaint.color = color
        invalidate()
    }

    var location = IntArray(2)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        getLocationOnScreen(location)
        mRect[paddingLeft.toFloat(), paddingTop.toFloat(), width - paddingRight.toFloat()] =
            height - paddingBottom.toFloat()
        canvas.drawRoundRect(mRect, 15f, 15f, mPaint)
    }

    init {
        val density = context.resources.displayMetrics.density
        padding = (10 * density).toInt()
        paddingBetween = (3 * density).toInt()
        setWillNotDraw(false)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        mRect = RectF()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.strokeCap = Paint.Cap.ROUND
        mTitleTextView = TextView(context)
        mTitleTextView.setPadding(padding, padding, padding, padding)
        mTitleTextView.gravity = Gravity.CENTER
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        mTitleTextView.setTextColor(Color.BLACK)
        addView(
            mTitleTextView,
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        mContentTextView = TextView(context)
        mContentTextView.visibility = View.GONE
        mContentTextView.setTextColor(Color.BLACK)
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        mContentTextView.setPadding(padding, paddingBetween, padding, padding)
        mContentTextView.gravity = Gravity.CENTER
        addView(
            mContentTextView,
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }
}
