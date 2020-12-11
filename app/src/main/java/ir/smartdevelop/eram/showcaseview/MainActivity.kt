package ir.smartdevelop.eram.showcaseview

import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.appcompat.app.AppCompatActivity
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener

class MainActivity : AppCompatActivity() {

    var view1: View? = null
    var view2: View? = null
    var view3: View? = null
    var view4: View? = null
    var view5: View? = null
    var view6: View? = null
    private var mGuideView: GuideView? = null
    private var builder: GuideView.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        view1 = findViewById(R.id.view1)
        view2 = findViewById(R.id.view2)
        view3 = findViewById(R.id.view3)
        view4 = findViewById(R.id.view4)
        view5 = findViewById(R.id.view5)
        view6 = findViewById(R.id.view6)
        builder = GuideView.Builder(this)
            .setTitle("Guide Title Text")
            .setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....")
            .setGravity(Gravity.center)
            .setDismissType(DismissType.selfView)
            .setTargetView(view1)
            .setGuideListener(object : GuideListener {
                override fun onDismiss(view: View?) {
                    when (view!!.id) {
                        R.id.view1 -> builder!!.setTargetView(view2).build()
                        R.id.view2 -> builder!!.setTargetView(view3).build()
                        R.id.view3 -> builder!!.setTargetView(view4).build()
                        R.id.view4 -> builder!!.setTargetView(view5).build()
                        R.id.view5 -> builder!!.setTargetView(view6).build()
                        R.id.view6 -> return
                    }
                    mGuideView = builder!!.build()
                    mGuideView!!.show()
                }
            })
        mGuideView = builder!!.build()
        mGuideView!!.show()
        updatingForDynamicLocationViews()
    }

    private fun updatingForDynamicLocationViews() {
        view4!!.onFocusChangeListener =
            OnFocusChangeListener { view, b -> mGuideView!!.updateGuideViewLocation() }
    }
}
