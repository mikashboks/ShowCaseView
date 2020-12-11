package ir.smartdevelop.eram.showcaseview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import smartdevelop.ir.eram.showcaseviewlib.TargetAble

class CircleView : View, TargetAble {

    private var _circleColor = DEFAULT_CIRCLE_COLOR
    private var _paint: Paint? = null
    private val _guidePath = Path()
    private val _boundingRect = RectF()

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        _paint = Paint()
        _paint!!.isAntiAlias = true
    }

    var circleColor: Int
        get() = _circleColor
        set(circleColor) {
            _circleColor = circleColor
            invalidate()
        }

    private fun usableWidth(): Int {
        val w = width
        val pl = paddingLeft
        val pr = paddingRight
        return w - (pl + pr)
    }

    private fun usableHeight(): Int {
        val h = height
        val pt = paddingTop
        val pb = paddingBottom
        return h - (pt + pb)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pl = paddingLeft
        val pt = paddingTop
        val usableWidth = usableWidth()
        val usableHeight = usableHeight()
        val halfUsableWidth = usableWidth / 2
        val halfUsableHeight = usableHeight / 2
        val radius = Math.min(usableWidth, usableHeight) / 2
        val cx = pl + halfUsableWidth
        val cy = pt + halfUsableHeight
        _paint!!.color = _circleColor
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), _paint!!)
        val locationTarget = IntArray(2)
        getLocationOnScreen(locationTarget)
        val centerX = pl + halfUsableWidth + locationTarget[0]
        val centerY = pt + halfUsableHeight + locationTarget[1]
        _guidePath.reset()
        _guidePath.addCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            radius.toFloat(),
            Path.Direction.CW
        )
        _boundingRect.left = locationTarget[0].toFloat()
        _boundingRect.top = locationTarget[1].toFloat()
        _boundingRect.right = locationTarget[0] + width.toFloat()
        _boundingRect.bottom = locationTarget[1] + height.toFloat()
    }

    override fun guidePath(): Path? {
        return _guidePath
    }

    override fun boundingRect(): RectF? {
        return _boundingRect
    }

    companion object {
        private const val DEFAULT_CIRCLE_COLOR = Color.RED
    }
}
