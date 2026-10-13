package android.util

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer

/**
 * Desktop implementation of android.util.Xml backed by xmlpull + kxml2.
 */
object Xml {
    enum class Encoding { UTF_8, UTF_16, ISO_8859_1, US_ASCII }

    fun newPullParser(): XmlPullParser =
        XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = true
        }.newPullParser()

    fun newSerializer(): XmlSerializer =
        XmlPullParserFactory.newInstance().newSerializer()

    fun findEncodingByName(encodingName: String): Encoding =
        when (encodingName.uppercase()) {
            "UTF-8" -> Encoding.UTF_8
            "UTF-16" -> Encoding.UTF_16
            "ISO-8859-1" -> Encoding.ISO_8859_1
            "US-ASCII" -> Encoding.US_ASCII
            else -> throw java.io.UnsupportedEncodingException(encodingName)
        }

    fun parse(
        xml: java.io.InputStream,
        encoding: Encoding,
        parser: XmlPullParser
    ): XmlPullParser {
        parser.setInput(xml, encoding.name)
        return parser
    }
}
