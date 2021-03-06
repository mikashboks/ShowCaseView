package smartdevelop.ir.eram.showcaseviewlib

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.text.Spannable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */
class GuideView private constructor(context: Context, view: View?) : FrameLayout(context) {
    private val selfPaint = Paint()
    private val paintLine = Paint()
    private val paintCircle = Paint()
    private val paintCircleInner = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val X_FER_MODE_CLEAR: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val target: View?
    private var targetRect: RectF? = null
    private val selfRect = Rect()
    private val density: Float
    private var stopY = 0f
    private var isTop = false
    var isShowing = false
        private set
    private var yMessageView = 0
    private var startYLineAndCircle = 0f
    private var circleIndicatorSize = 0f
    private var circleIndicatorSizeFinal = 0f
    private var circleInnerIndicatorSize = 0f
    private var lineIndicatorWidthSize = 0f
    private var messageViewPadding = 0
    private var marginGuide = 0f
    private var strokeCircleWidth = 0f
    private var indicatorHeight = 0f
    private var isPerformedAnimationSize = false
    private var mGuideListener: GuideListener? = null
    private var mGravity: Gravity? = null
    private var dismissType: DismissType? = null
    private val mMessageView: GuideMessageView
    private fun startAnimationSize() {
        if (!isPerformedAnimationSize) {
            val circleSizeAnimator = ValueAnimator.ofFloat(0f, circleIndicatorSizeFinal)
            circleSizeAnimator.addUpdateListener {
                circleIndicatorSize = circleSizeAnimator.animatedValue as Float
                circleInnerIndicatorSize = circleSizeAnimator.animatedValue as Float - density
                postInvalidate()
            }
            val linePositionAnimator = ValueAnimator.ofFloat(stopY, startYLineAndCircle)
            linePositionAnimator.addUpdateListener {
                startYLineAndCircle = linePositionAnimator.animatedValue as Float
                postInvalidate()
            }
            linePositionAnimator.duration = SIZE_ANIMATION_DURATION.toLong()
            linePositionAnimator.start()
            linePositionAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    circleSizeAnimator.duration = SIZE_ANIMATION_DURATION.toLong()
                    circleSizeAnimator.start()
                    isPerformedAnimationSize = true
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
        }
    }

    private fun init() {
        lineIndicatorWidthSize = LINE_INDICATOR_WIDTH_SIZE * density
        marginGuide = MARGIN_INDICATOR * density
        indicatorHeight = INDICATOR_HEIGHT * density
        messageViewPadding = (MESSAGE_VIEW_PADDING * density).toInt()
        strokeCircleWidth = STROKE_CIRCLE_INDICATOR_SIZE * density
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density
    }

    private val navigationBarSize: Int
        private get() {
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }

    private val isLandscape: Boolean
        private get() {
            val display_mode = resources.configuration.orientation
            return display_mode != Configuration.ORIENTATION_PORTRAIT
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (target != null) {
            selfPaint.color = BACKGROUND_COLOR
            selfPaint.style = Paint.Style.FILL
            selfPaint.isAntiAlias = true
            canvas.drawRect(selfRect, selfPaint)
            paintLine.style = Paint.Style.FILL
            paintLine.color = LINE_INDICATOR_COLOR
            paintLine.strokeWidth = lineIndicatorWidthSize
            paintLine.isAntiAlias = true
            paintCircle.style = Paint.Style.STROKE
            paintCircle.color = CIRCLE_INDICATOR_COLOR
            paintCircle.strokeCap = Paint.Cap.ROUND
            paintCircle.strokeWidth = strokeCircleWidth
            paintCircle.isAntiAlias = true
            paintCircleInner.style = Paint.Style.FILL
            paintCircleInner.color = CIRCLE_INNER_INDICATOR_COLOR
            paintCircleInner.isAntiAlias = true
            val x = targetRect!!.left / 2 + targetRect!!.right / 2
            canvas.drawLine(x,
                    startYLineAndCircle,
                    x,
                    stopY,
                    paintLine)
            canvas.drawCircle(x, startYLineAndCircle, circleIndicatorSize, paintCircle)
            canvas.drawCircle(x, startYLineAndCircle, circleInnerIndicatorSize, paintCircleInner)
            targetPaint.xfermode = X_FER_MODE_CLEAR
            targetPaint.isAntiAlias = true
            if (target is TargetAble) {
                canvas.drawPath((target as TargetAble).guidePath()!!, targetPaint)
            } else {
                canvas.drawRoundRect(targetRect!!, RADIUS_SIZE_TARGET_RECT.toFloat(), RADIUS_SIZE_TARGET_RECT.toFloat(), targetPaint)
            }
        }
    }

    fun dismiss() {
        ((context as Activity).window.decorView as ViewGroup).removeView(this)
        isShowing = false
        if (mGuideListener != null) {
            mGuideListener!!.onDismiss(target)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (dismissType) {
                DismissType.outside -> if (!isViewContains(mMessageView, x, y)) {
                    dismiss()
                }
                DismissType.anywhere -> dismiss()
                DismissType.targetView -> if (targetRect!!.contains(x, y)) {
                    target!!.performClick()
                    dismiss()
                }
                DismissType.selfView -> if (isViewContains(mMessageView, x, y)) {
                    dismiss()
                }
            }
            return true
        }
        return false
    }

    private fun isViewContains(view: View, rx: Float, ry: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height
        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    private fun setMessageLocation(p: Point) {
        mMessageView.x = p.x.toFloat()
        mMessageView.y = p.y.toFloat()
        postInvalidate()
    }

    fun updateGuideViewLocation() {
        requestLayout()
    }

    private fun resolveMessageViewLocation(): Point {
        var xMessageView = 0
        xMessageView = if (mGravity === Gravity.center) {
            (targetRect!!.left - mMessageView.width / 2 + target!!.width / 2).toInt()
        } else targetRect!!.right.toInt() - mMessageView.width
        if (isLandscape) {
            xMessageView -= navigationBarSize
        }
        if (xMessageView + mMessageView.width > width) xMessageView = width - mMessageView.width
        if (xMessageView < 0) xMessageView = 0


        //set message view bottom
        if (targetRect!!.top + indicatorHeight > height / 2) {
            isTop = false
            yMessageView = (targetRect!!.top - mMessageView.height - indicatorHeight).toInt()
        } else {
            isTop = true
            yMessageView = (targetRect!!.top + target!!.height + indicatorHeight).toInt()
        }
        if (yMessageView < 0) yMessageView = 0
        return Point(xMessageView, yMessageView)
    }

    fun show() {
        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        this.isClickable = false
        ((context as Activity).window.decorView as ViewGroup).addView(this)
        val startAnimation = AlphaAnimation(0.0f, 1.0f)
        startAnimation.duration = APPEARING_ANIMATION_DURATION.toLong()
        startAnimation.fillAfter = true
        startAnimation(startAnimation)
        isShowing = true
    }

    fun setTitle(str: String?) {
        mMessageView.setTitle(str)
    }

    fun setContentText(str: String?) {
        mMessageView.setContentText(str)
    }

    fun setContentSpan(span: Spannable?) {
        mMessageView.setContentSpan(span)
    }

    fun setTitleTypeFace(typeFace: Typeface?) {
        mMessageView.setTitleTypeFace(typeFace)
    }

    fun setContentTypeFace(typeFace: Typeface?) {
        mMessageView.setContentTypeFace(typeFace)
    }

    fun setTitleTextSize(size: Int) {
        mMessageView.setTitleTextSize(size)
    }

    fun setContentTextSize(size: Int) {
        mMessageView.setContentTextSize(size)
    }

    class Builder(private val context: Context) {
        private var targetView: View? = null
        private var title: String? = null
        private var contentText: String? = null
        private var gravity: Gravity? = null
        private var dismissType: DismissType? = null
        private var contentSpan: Spannable? = null
        private var titleTypeFace: Typeface? = null
        private var contentTypeFace: Typeface? = null
        private var guideListener: GuideListener? = null
        private var titleTextSize = 0
        private var contentTextSize = 0
        private var lineIndicatorHeight = 0f
        private var lineIndicatorWidthSize = 0f
        private var circleIndicatorSize = 0f
        private var circleInnerIndicatorSize = 0f
        private var strokeCircleWidth = 0f
        fun setTargetView(view: View?): Builder {
            targetView = view
            return this
        }

        /**
         * gravity GuideView
         *
         * @param gravity it should be one type of Gravity enum.
         */
        fun setGravity(gravity: Gravity?): Builder {
            this.gravity = gravity
            return this
        }

        /**
         * defining a title
         *
         * @param title a title. for example: submit button.
         */
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        /**
         * defining a description for the target view
         *
         * @param contentText a description. for example: this button can for submit your information..
         */
        fun setContentText(contentText: String?): Builder {
            this.contentText = contentText
            return this
        }

        /**
         * setting spannable type
         *
         * @param span a instance of spannable
         */
        fun setContentSpan(span: Spannable?): Builder {
            contentSpan = span
            return this
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         */
        fun setContentTypeFace(typeFace: Typeface?): Builder {
            contentTypeFace = typeFace
            return this
        }

        /**
         * adding a listener on show case view
         *
         * @param guideListener a listener for events
         */
        fun setGuideListener(guideListener: GuideListener?): Builder {
            this.guideListener = guideListener
            return this
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         */
        fun setTitleTypeFace(typeFace: Typeface?): Builder {
            titleTypeFace = typeFace
            return this
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        fun setContentTextSize(size: Int): Builder {
            contentTextSize = size
            return this
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        fun setTitleTextSize(size: Int): Builder {
            titleTextSize = size
            return this
        }

        /**
         * this method defining the type of dismissing function
         *
         * @param dismissType should be one type of DismissType enum. for example: outside -> Dismissing with click on outside of MessageView
         */
        fun setDismissType(dismissType: DismissType?): Builder {
            this.dismissType = dismissType
            return this
        }

        /**
         * changing line height indicator
         *
         * @param height you can change height indicator (Converting to Dp)
         */
        fun setIndicatorHeight(height: Float): Builder {
            lineIndicatorHeight = height
            return this
        }

        /**
         * changing line width indicator
         *
         * @param width you can change width indicator
         */
        fun setIndicatorWidthSize(width: Float): Builder {
            lineIndicatorWidthSize = width
            return this
        }

        /**
         * changing circle size indicator
         *
         * @param size you can change circle size indicator
         */
        fun setCircleIndicatorSize(size: Float): Builder {
            circleIndicatorSize = size
            return this
        }

        /**
         * changing inner circle size indicator
         *
         * @param size you can change inner circle indicator size
         */
        fun setCircleInnerIndicatorSize(size: Float): Builder {
            circleInnerIndicatorSize = size
            return this
        }

        /**
         * changing stroke circle size indicator
         *
         * @param size you can change stroke circle indicator size
         */
        fun setCircleStrokeIndicatorSize(size: Float): Builder {
            strokeCircleWidth = size
            return this
        }

        fun build(): GuideView {
            val guideView = GuideView(context, targetView)
            guideView.mGravity = if (gravity != null) gravity else Gravity.auto
            guideView.dismissType = if (dismissType != null) dismissType else DismissType.targetView
            val density = context.resources.displayMetrics.density
            guideView.setTitle(title)
            if (contentText != null) guideView.setContentText(contentText)
            if (titleTextSize != 0) guideView.setTitleTextSize(titleTextSize)
            if (contentTextSize != 0) guideView.setContentTextSize(contentTextSize)
            if (contentSpan != null) guideView.setContentSpan(contentSpan)
            if (titleTypeFace != null) {
                guideView.setTitleTypeFace(titleTypeFace)
            }
            if (contentTypeFace != null) {
                guideView.setContentTypeFace(contentTypeFace)
            }
            if (guideListener != null) {
                guideView.mGuideListener = guideListener
            }
            if (lineIndicatorHeight != 0f) {
                guideView.indicatorHeight = lineIndicatorHeight * density
            }
            if (lineIndicatorWidthSize != 0f) {
                guideView.lineIndicatorWidthSize = lineIndicatorWidthSize * density
            }
            if (circleIndicatorSize != 0f) {
                guideView.circleIndicatorSize = circleIndicatorSize * density
            }
            if (circleInnerIndicatorSize != 0f) {
                guideView.circleInnerIndicatorSize = circleInnerIndicatorSize * density
            }
            if (strokeCircleWidth != 0f) {
                guideView.strokeCircleWidth = strokeCircleWidth * density
            }
            return guideView
        }

    }

    companion object {
        const val TAG = "GuideView"
        private const val INDICATOR_HEIGHT = 40
        private const val MESSAGE_VIEW_PADDING = 5
        private const val SIZE_ANIMATION_DURATION = 700
        private const val APPEARING_ANIMATION_DURATION = 400
        private const val CIRCLE_INDICATOR_SIZE = 6
        private const val LINE_INDICATOR_WIDTH_SIZE = 3
        private const val STROKE_CIRCLE_INDICATOR_SIZE = 3
        private const val RADIUS_SIZE_TARGET_RECT = 15
        private const val MARGIN_INDICATOR = 15
        private const val BACKGROUND_COLOR = -0x67000000
        private const val CIRCLE_INNER_INDICATOR_COLOR = -0x333334
        private const val CIRCLE_INDICATOR_COLOR = Color.WHITE
        private const val LINE_INDICATOR_COLOR = Color.WHITE
    }

    init {
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        target = view
        density = context.resources.displayMetrics.density
        init()
        targetRect = if (view is TargetAble) {
            (view as TargetAble).boundingRect()
        } else {
            val locationTarget = IntArray(2)
            target!!.getLocationOnScreen(locationTarget)
            RectF(locationTarget[0].toFloat(),
                    locationTarget[1].toFloat(),
                    (locationTarget[0] + target.width).toFloat(),
                    (locationTarget[1] + target.height).toFloat())
        }
        mMessageView = GuideMessageView(getContext())
        mMessageView.setPadding(messageViewPadding, messageViewPadding, messageViewPadding, messageViewPadding)
        mMessageView.setColor(Color.WHITE)
        addView(mMessageView, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setMessageLocation(resolveMessageViewLocation())
        val layoutListener: ViewTreeObserver.OnGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                setMessageLocation(resolveMessageViewLocation())
                targetRect = if (target is TargetAble) {
                    (target as TargetAble).boundingRect()
                } else {
                    val locationTarget = IntArray(2)
                    target!!.getLocationOnScreen(locationTarget)
                    RectF(locationTarget[0].toFloat(),
                            locationTarget[1].toFloat(),
                            (locationTarget[0] + target.width).toFloat(),
                            (locationTarget[1] + target.height).toFloat())
                }
                selfRect[paddingLeft, paddingTop, width - paddingRight] = height - paddingBottom
                marginGuide = (if (isTop) marginGuide else -marginGuide)
                startYLineAndCircle = (if (isTop) targetRect!!.bottom else targetRect!!.top) + marginGuide
                stopY = yMessageView + indicatorHeight
                startAnimationSize()
                viewTreeObserver.addOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }
}
