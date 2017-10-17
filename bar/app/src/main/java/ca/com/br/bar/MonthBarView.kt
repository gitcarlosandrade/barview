package ca.com.br.bar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.month_item_bar.view.*

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
        View.inflate(context, R.layout.month_item_bar, this)
    }

    private fun initDrawables() {
        estimatedBarLayer = getEstimatedBarDrawable()
        actualBarLayer = getActualBarDrawable()
        overBarLayer = getOverBarDrawable()

        estimatedBar = estimatedBarLayer?.findDrawableByLayerId(R.id.stroke) as GradientDrawable
        actualBarStroke = actualBarLayer?.findDrawableByLayerId(R.id.rect_stroke) as GradientDrawable
        actualBarSolid = actualBarLayer?.findDrawableByLayerId(R.id.rect_solid) as GradientDrawable
        overBarStroke = overBarLayer?.findDrawableByLayerId(R.id.rect_stroke) as GradientDrawable
        overBarSolid = overBarLayer?.findDrawableByLayerId(R.id.rect_solid) as GradientDrawable
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
             over: Double = 0.0,
             maxValue: Double,
             parentWidth: Int,
             showEstimated: Boolean) {

        initDrawables()

        val currentBarValue = if (showEstimated) {
            Math.max(Math.abs(estimatedValue), Math.abs(value) + Math.abs(over))
        } else {
            Math.abs(value) + Math.abs(over)
        }

        monthItemBarLabel.text = currentBarValue.toString()

        val barSize = getBarSize(maxValue, currentBarValue, parentWidth, monthItemBarLabel)

        if (showEstimated) {
            estimatedBar?.setStroke(1.dpToPx(context), barColor)
            monthItemBarEstimatedBar.layoutParams.width = barSize
        }
        else {
            estimatedBar?.setStroke(0, barColor)
            monthItemBarEstimatedBar.layoutParams.width = barSize
        }

        val actualWidth = getWidthForValue(
                value = value,
                maxValue = currentBarValue,
                width = barSize
        )
        monthItemBarActualBar.layoutParams.width = actualWidth

        //define as cores das barras
        actualBarSolid?.setColor(barColor)
        overBarSolid?.setColor(overBarColor)



        if (over > 0.0) {
            //define o tamanho do excedido
            monthItemBarOverBar.layoutParams.width = getWidthForValue(
                    value = over,
                    maxValue = currentBarValue,
                    width = barSize
            )

            setOverBar(barSize, showEstimated)

        } else {

            //some com a barra de excedido
            monthItemBarOverBar.layoutParams.width = 0

            //define todos os cantos arredondados
            actualBarSolid?.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
            actualBarStroke?.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)

            //define o contorno
            actualBarStroke?.setStroke(1.dpToPx(context), barColor)
        }

        setBackgrounds()

        redraw()
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

    private fun setOverBar(barSize: Int, showEstimated: Boolean) {

        //define os cantos (top right e bottom right) sem arredondamento
        actualBarSolid?.cornerRadii = floatArrayOf(8f, 8f, 0f, 0f, 0f, 0f, 8f, 8f)
        actualBarStroke?.cornerRadii = floatArrayOf(8f, 8f, 0f, 0f, 0f, 0f, 8f, 8f)

        //tira a linha
        actualBarStroke?.setStroke(0, barColor)

        //muda a cor do contorno da barra de estimado
        if (showEstimated) estimatedBar?.setStroke(1.dpToPx(context), overBarColor)
    }

    private fun setBackgrounds() {
        estimatedBarLayer?.setDrawableByLayerId(R.id.stroke, estimatedBar)
        actualBarLayer?.setDrawableByLayerId(R.id.stroke, actualBarStroke)
        actualBarLayer?.setDrawableByLayerId(R.id.rect_solid, actualBarSolid)
        overBarLayer?.setDrawableByLayerId(R.id.stroke, overBarStroke)
        overBarLayer?.setDrawableByLayerId(R.id.rect_solid, overBarSolid)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            monthItemBarEstimatedBar.background = estimatedBarLayer
            monthItemBarActualBar.background = actualBarLayer
            monthItemBarOverBar.background = overBarLayer
        } else {
            monthItemBarEstimatedBar.setBackgroundDrawable(estimatedBarLayer)
            monthItemBarActualBar.setBackgroundDrawable(actualBarLayer)
            monthItemBarOverBar.setBackgroundDrawable(overBarLayer)
        }
    }

    private fun getActualBarDrawable() =
            ContextCompat.getDrawable(context, R.drawable.rounded_bar_fill_drawable).mutate() as LayerDrawable

    private fun getEstimatedBarDrawable() =
            ContextCompat.getDrawable(context, R.drawable.rounded_bar_stroke_drawable).mutate() as LayerDrawable

    private fun getOverBarDrawable() =
            ContextCompat.getDrawable(context, R.drawable.over_rounded_bar_fill_drawable).mutate() as LayerDrawable

    private fun getBarSize(maxValue: Double, value: Double, parentWidth: Int, relatedView: View) : Int{
        val max = Math.abs(maxValue)
        val currentValue = Math.abs(value)

        val relatedViewWidth = if (relatedView.width == 0) {
            relatedView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            relatedView.measuredWidth
        } else {
            relatedView.width
        }

        var width: Int = (parentWidth - relatedViewWidth - Math.ceil(6f.dpToPx(context).toDouble()).toInt())

        if (max > 0.0 && currentValue > 0.0) width = ((currentValue * width) / max).toInt()

        return width
    }

    private fun Float.dpToPx(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)
    }

    private fun Int.dpToPx(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), displayMetrics).toInt()
    }

    private fun getWidthForValue(value: Double, maxValue: Double, width: Int): Int {
        return ((value * width) / maxValue).toInt()
    }

    private fun setBackgroundOnBar(fill: Boolean) {

//        layerDrawable.setLayerInset(0, 1.dpToPx(context), 1.dpToPx(context), 1.dpToPx(context), 1.dpToPx(context))
//
//         estimatedValueShape.paint.apply {
//            color = barColor
//            style = if (fill) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
//            strokeWidth = 1f.dpToPx(context)
//        }
//
//        bar.apply{
//            if (SDK_INT >= JELLY_BEAN) {
//                background = layerDrawable
//            } else {
//                setBackgroundDrawable(layerDrawable)
//            }
//        }
    }
}