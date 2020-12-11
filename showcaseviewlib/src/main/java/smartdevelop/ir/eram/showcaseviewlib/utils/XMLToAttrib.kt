package smartdevelop.ir.eram.showcaseviewlib.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Xml
import androidx.annotation.XmlRes
import org.xmlpull.v1.XmlPullParser
import smartdevelop.ir.eram.showcaseviewlib.R


object XMLToAttrib {

    fun get(context: Context, @XmlRes xmlFile: Int): AttributeSet {
        val parser: XmlPullParser = context.resources.getXml(xmlFile)
        try {
            parser.next()
            parser.nextTag()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Xml.asAttributeSet(parser)
    }
}