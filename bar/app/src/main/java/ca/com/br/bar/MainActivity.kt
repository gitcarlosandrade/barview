package ca.com.br.bar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            calc()
        }
    }

    fun calc() {
        val v = value.text.toString().toDouble()
        val m = max_value.text.toString().toDouble()
        val e = estimated.text.toString().toDouble()
        val o = over.text.toString().toDouble()
        val s = show_estimated.isChecked
        val c = this.window.decorView.findViewById<View>(android.R.id.content).rootView
        bar.bind(value = v, estimatedValue = e, over = o, maxValue = m, parentWidth = c.width, showEstimated = s)
    }
}
