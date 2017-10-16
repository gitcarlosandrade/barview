package ca.com.br.bar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
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
    private var estimatedBarLayer: LayerDrawable
    private var actualBarLayer: LayerDrawable
    private var overBarLayer: LayerDrawable
    private var estimatedBar: GradientDrawable
    private var actualBarStroke: GradientDrawable
    private var actualBarSolid: GradientDrawable
    private var overBarStroke: GradientDrawable
    private var overBarSolid: GradientDrawable

    init {
        View.inflate(context, R.layout.month_item_bar, this)
        estimatedBarLayer = getEstimatedBarDrawable()
        actualBarLayer = getActualBarDrawable()
        overBarLayer = getOverBarDrawable()

        estimatedBar = estimatedBarLayer.findDrawableByLayerId(R.id.stroke) as GradientDrawable
        actualBarStroke = actualBarLayer.findDrawableByLayerId(R.id.rect_stroke) as GradientDrawable
        actualBarSolid = actualBarLayer.findDrawableByLayerId(R.id.rect_solid) as GradientDrawable
        overBarStroke = overBarLayer.findDrawableByLayerId(R.id.rect_stroke) as GradientDrawable
        overBarSolid = overBarLayer.findDrawableByLayerId(R.id.rect_solid) as GradientDrawable
    }

    private fun readAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MonthBarView)
        barColor = typedArray.getColor(
                R.styleable.MonthBarView_bar_color,
                ContextCompat.getColor(context, R.color.black_divider))

        overBarColor = typedArray.getColor(
                R.styleable.MonthBarView_over_bar_color,
                ContextCompat.getColor(context, R.color.black_divider))

        valueTxt.setTextColor(barColor)
        typedArray.recycle()
    }

    fun bind(value: Double,
             estimatedValue: Double = 0.0,
             over: Double = 0.0,
             maxValue: Double,
             parentWidth: Int,
             showEstimated: Boolean) {

        val currentBarValue = if (showEstimated) {
            Math.max(Math.abs(estimatedValue), Math.abs(value) + Math.abs(over))
        } else {
            Math.abs(value) + Math.abs(over)
        }

        val barSize = getBarSize(maxValue, currentBarValue, parentWidth, valueTxt)
        valueTxt.text = currentBarValue.toString()

        //configura contorno (valor estimado)
        if (showEstimated) {
            estimatedBar.setStroke(1, barColor)
            //define o espaço  para colocar as barras
            //esse espaço é usado para mostrar o estimado também
            estimated_bar.layoutParams.width = barSize
            //define o tamanho da barra de gasto atual
//            actual_bar.layoutParams.actualWidth = barSize / 2
        }
        else {
            estimatedBar.setStroke(0, barColor)
            estimated_bar.layoutParams.width = barSize
        }

        val actualWidth = getWidthForValue(
                value = value,
                maxValue = maxValue,
                width = barSize
        )
        actual_bar.layoutParams.width = actualWidth

        //define as cores das barras
        actualBarSolid.setColor(barColor)
        overBarSolid.setColor(overBarColor)



        if (over > 0.0) {
            //define o tamanho do excedido
            over_bar.layoutParams.width = getWidthForValue(
                    value = over,
                    maxValue = maxValue,
                    width = barSize
            )

            setOverBar(barSize, showEstimated)

        } else {

            //some com a barra de excedido
            over_bar.layoutParams.width = 0

            //define todos os cantos arredondados
            actualBarSolid.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
            actualBarStroke.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)

            //define o contorno
            actualBarStroke.setStroke(1, barColor)
        }

        setBackgrounds()

        invalidate()

        requestLayout()
    }

    private fun setOverBar(barSize: Int, showEstimated: Boolean) {

        //define os cantos (top right e bottom right) sem arredondamento
        actualBarSolid.cornerRadii = floatArrayOf(8f, 8f, 0f, 0f, 0f, 0f, 8f, 8f)
        actualBarStroke.cornerRadii = floatArrayOf(8f, 8f, 0f, 0f, 0f, 0f, 8f, 8f)

        //tira a linha
        actualBarStroke.setStroke(0, barColor)

        //muda a cor do contorno da barra de estimado
        if (showEstimated) estimatedBar.setStroke(1, overBarColor)
    }

    private fun setBackgrounds() {
        estimatedBarLayer.setDrawableByLayerId(R.id.stroke, estimatedBar)
        actualBarLayer.setDrawableByLayerId(R.id.stroke, actualBarStroke)
        actualBarLayer.setDrawableByLayerId(R.id.rect_solid, actualBarSolid)
        overBarLayer.setDrawableByLayerId(R.id.stroke, overBarStroke)
        overBarLayer.setDrawableByLayerId(R.id.rect_solid, overBarSolid)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            estimated_bar.background = estimatedBarLayer
            actual_bar.background = actualBarLayer
            over_bar.background = overBarLayer
        } else {
            estimated_bar.setBackgroundDrawable(estimatedBarLayer)
            actual_bar.setBackgroundDrawable(actualBarLayer)
            over_bar.setBackgroundDrawable(overBarLayer)
        }
    }

    private fun getActualBarDrawable() =
            ContextCompat.getDrawable(context, R.drawable.rounded_bar_fill_drawable) as LayerDrawable

    private fun getEstimatedBarDrawable() =
            ContextCompat.getDrawable(context, R.drawable.rounded_bar_stroke_drawable) as LayerDrawable

    private fun getOverBarDrawable() =
            ContextCompat.getDrawable(context, R.drawable.over_rounded_bar_fill_drawable) as LayerDrawable

    private fun getBarSize(maxValue: Double, value: Double, parentWidth: Int, relatedView: View) : Int{
        var max = maxValue
        var currentValue = value
        val relatedViewWidth = if (relatedView.width == 0) {
            relatedView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            relatedView.measuredWidth
        } else {
            relatedView.width
        }

        relatedView.minimumWidth = relatedViewWidth
        var width: Int = (parentWidth - relatedViewWidth)

        if (max < 0) max *= -1
        if (currentValue < 0) currentValue *= -1

        if (max > 0.0 && currentValue > 0.0) width = (currentValue / max * width).toInt()

        return width
    }

    fun getWidthForValue(value: Double, maxValue: Double, width: Int): Int {
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