package ca.com.br.bar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.month_bar_view.view.*

class MonthBarView : RelativeLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        readAttributes(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        readAttributes(attrs)
    }

    private var barColor: Int = 0
    private var overBarColor: Int = 0
    private var estimatedBarLayer: LayerDrawable? = null
    private var actualBarLayer: LayerDrawable? = null
    private var overBarLayer: LayerDrawable? = null
    private var estimatedBar: GradientDrawable? = null
    private var actualBarStroke: GradientDrawable? = null
    private var actualBarSolid: GradientDrawable? = null
    private var overBarStroke: GradientDrawable? = null
    private var overBarSolid: GradientDrawable? = null

    init {
        View.inflate(context, R.layout.month_bar_view, this)
        initDrawables()
    }

    private fun initDrawables() {
        estimatedBarLayer = getEstimatedBarDrawable()
        actualBarLayer = getActualBarDrawable()
        overBarLayer = getOverBarDrawable()

        estimatedBar = estimatedBarLayer?.findDrawableByLayerId(R.id.stroke) as GradientDrawable
        actualBarStroke = actualBarLayer?.findDrawableByLayerId(R.id.strokeRect) as GradientDrawable
        actualBarSolid = actualBarLayer?.findDrawableByLayerId(R.id.solidRect) as GradientDrawable
        overBarStroke = overBarLayer?.findDrawableByLayerId(R.id.strokeRect) as GradientDrawable
        overBarSolid = overBarLayer?.findDrawableByLayerId(R.id.solidRect) as GradientDrawable
    }

    private fun readAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MonthBarView)
        barColor = typedArray.getColor(
                R.styleable.MonthBarView_bar_color,
                ContextCompat.getColor(context, R.color.black_divider))

        overBarColor = typedArray.getColor(
                R.styleable.MonthBarView_over_bar_color,
                ContextCompat.getColor(context, R.color.black_divider))

        monthItemBarLabel.setTextColor(barColor)
        typedArray.recycle()
    }

    fun bind(value: Double,
             estimatedValue: Double = 0.0,
             overValue: Double = 0.0,
             maxValue: Double,
             parentWidth: Int,
             label: String,
             showEstimated: Boolean = false) {

        monthItemBarLabel.text = label

        val currentBarValue = getCurrentBarValue(showEstimated, estimatedValue, value,
                overValue)

        val barSize = getBarSize(maxValue, currentBarValue, parentWidth, monthItemBarLabel)

        setupEstimatedBar(showEstimated, barSize)
        setupActualBar(value, currentBarValue, barSize)
        setupOverBar(overValue, currentBarValue, barSize, showEstimated)
        setupBackgrounds()

        redraw()
    }

    private fun setupOverBar(overValue: Double, currentBarValue: Double, barSize: Int,
                             showEstimated: Boolean) {

        val width: Int
        val actualBarRadii: FloatArray
        val strokeWidth: Int
        when {
            overValue > 0 -> {
                width = getWidthForValue(
                        value = overValue,
                        maxValue = currentBarValue,
                        width = barSize
                )
                actualBarRadii = floatArrayOf(8f, 8f, 0f, 0f, 0f, 0f, 8f, 8f)
                strokeWidth = 0
                if (showEstimated) estimatedBar?.setStroke(1.dpToPx(), overBarColor)
            }
            else -> {
                width = 0
                actualBarRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
                strokeWidth = 1.dpToPx()
            }
        }

        monthItemBarOverBar.layoutParams.width = width
        actualBarSolid?.cornerRadii = actualBarRadii
        actualBarStroke?.cornerRadii = actualBarRadii
        actualBarStroke?.setStroke(strokeWidth, barColor)
    }

    private fun setupEstimatedBar(showEstimated: Boolean, barSize: Int) {
        val estimatedBarStrokeWidth = if (showEstimated) 1.dpToPx() else 0
        estimatedBar?.setStroke(estimatedBarStrokeWidth, barColor)
        monthItemBarEstimatedBar.layoutParams.width = barSize
    }

    private fun setupActualBar(value: Double, currentBarValue: Double, barSize: Int) {
        val actualWidth = getWidthForValue(
                value = value,
                maxValue = currentBarValue,
                width = barSize
        )
        monthItemBarActualBar.layoutParams.width = actualWidth
        actualBarSolid?.setColor(barColor)
        overBarSolid?.setColor(overBarColor)
    }

    private fun getCurrentBarValue(showEstimated: Boolean, estimatedValue: Double, value: Double,
                                   overValue: Double): Double {
        return if (showEstimated) {
            Math.max(estimatedValue, value + overValue)
        } else {
            Math.abs(value) + Math.abs(overValue)
        }
    }

    private fun redraw() {
        monthItemBarEstimatedBar.invalidate()
        monthItemBarEstimatedBar.requestLayout()
        monthItemBarActualBar.invalidate()
        monthItemBarActualBar.requestLayout()
        monthItemBarOverBar.invalidate()
        monthItemBarOverBar.requestLayout()
        invalidate()
        requestLayout()
    }

    private fun setupBackgrounds() {
        estimatedBarLayer?.setDrawableByLayerId(R.id.stroke, estimatedBar)
        actualBarLayer?.setDrawableByLayerId(R.id.stroke, actualBarStroke)
        actualBarLayer?.setDrawableByLayerId(R.id.solidRect, actualBarSolid)
        overBarLayer?.setDrawableByLayerId(R.id.stroke, overBarStroke)
        overBarLayer?.setDrawableByLayerId(R.id.solidRect, overBarSolid)

        ViewCompat.setBackground(monthItemBarEstimatedBar, estimatedBarLayer)
        ViewCompat.setBackground(monthItemBarActualBar, actualBarLayer)
        ViewCompat.setBackground(monthItemBarOverBar, overBarLayer)
    }

    private fun getActualBarDrawable() = ContextCompat.getDrawable(context,
            R.drawable.rounded_bar_fill_drawable).mutate() as LayerDrawable

    private fun getEstimatedBarDrawable() = ContextCompat.getDrawable(context,
            R.drawable.rounded_bar_stroke_drawable).mutate() as LayerDrawable

    private fun getOverBarDrawable() = ContextCompat.getDrawable(context,
            R.drawable.over_rounded_bar_fill_drawable).mutate() as LayerDrawable

    private fun getBarSize(maxValue: Double, value: Double, parentWidth: Int, relatedView: View)
            : Int {
        val max = Math.abs(maxValue)
        val currentValue = Math.abs(value)

        val relatedViewWidth = if (relatedView.width == 0) {
            relatedView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            relatedView.measuredWidth
        } else {
            relatedView.width
        }

        val relatedViewLayoutParams = relatedView.layoutParams
                as RelativeLayout.LayoutParams

        val availableWidth: Int = (parentWidth - relatedViewWidth - relatedViewLayoutParams.leftMargin)

        return getWidthForValue(value = currentValue, maxValue = max, width = availableWidth)
    }

    private fun Int.dpToPx(): Int {
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
                displayMetrics).toInt()
    }

    private fun getWidthForValue(value: Double, maxValue: Double, width: Int): Int {
        return ((value * width) / maxValue).toInt()
    }

}