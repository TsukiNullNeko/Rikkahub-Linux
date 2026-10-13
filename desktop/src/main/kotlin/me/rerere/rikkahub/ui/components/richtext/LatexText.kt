package me.rerere.rikkahub.ui.components.richtext

import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.image.BufferedImage
import javax.swing.JLabel

/**
 * LaTeX rendering for desktop, backed by the JVM jlatexmath library.
 * Formulas are rasterized once and cached as Compose images.
 */
class DesktopLatexDrawable(val image: ImageBitmap, val bounds: Rect) {
    val widthPx: Int get() = bounds.width()
    val heightPx: Int get() = bounds.height()
}

fun assumeLatexSize(latex: String, fontSize: Float): Rect =
    renderLatex(latex, fontSize, 0xFF000000.toInt(), 0)?.bounds ?: Rect(0, 0, 0, 0)

@Composable
fun LatexText(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    val mergedStyle = style.merge(fontSize = fontSize, color = color)
    val density = LocalDensity.current

    val drawable = remember(latex, fontSize, mergedStyle) {
        runCatching {
            with(density) {
                getLatexDrawable(
                    latex = processLatex(latex),
                    fontSize = fontSize.toPx(),
                    color = mergedStyle.color.toArgb(),
                    background = mergedStyle.background.toArgb()
                )
            }
        }.getOrNull()
    }

    if (drawable != null) {
        with(density) {
            Image(
                bitmap = drawable.image,
                contentDescription = null,
                modifier = modifier.size(
                    width = drawable.widthPx.toDp(),
                    height = drawable.heightPx.toDp()
                )
            )
        }
    } else {
        Text(text = latex, style = mergedStyle, modifier = modifier)
    }
}

fun getLatexDrawable(
    latex: String,
    fontSize: Float,
    color: Int,
    background: Int
): DesktopLatexDrawable? = renderLatex(processLatex(latex), fontSize, color, background)

/**
 * Splits a long inline formula so it can wrap in text flow. The desktop renderer keeps
 * formulas whole; the caller falls back to the single-formula layout.
 */
fun splitLatex(
    latex: String,
    maxWidthPx: Float,
    fontSize: Float,
    color: Int
): List<DesktopLatexDrawable> {
    val drawable = renderLatex(processLatex(latex), fontSize, color, 0) ?: return emptyList()
    return if (drawable.widthPx <= maxWidthPx * 4) listOf(drawable) else listOf(drawable)
}

@Composable
fun LatexDrawable(drawable: DesktopLatexDrawable, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    with(density) {
        Image(
            bitmap = drawable.image,
            contentDescription = null,
            modifier = modifier.size(
                width = drawable.widthPx.toDp(),
                height = drawable.heightPx.toDp()
            )
        )
    }
}

private fun renderLatex(latex: String, fontSize: Float, color: Int, background: Int): DesktopLatexDrawable? {
    return runCatching {
        val formula = TeXFormula(latex)
        val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize)
        icon.setForeground(java.awt.Color(color, true))
        val width = icon.iconWidth.coerceAtLeast(1)
        val height = icon.iconHeight.coerceAtLeast(1)
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        graphics.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
        )
        if (android.graphics.Color.alpha(background) != 0) {
            graphics.color = java.awt.Color(background, true)
            graphics.fillRect(0, 0, width, height)
        }
        icon.paintIcon(JLabel(), graphics, 0, 0)
        graphics.dispose()
        DesktopLatexDrawable(
            image = image.toComposeImageBitmap(),
            bounds = Rect(0, 0, width, height)
        )
    }.getOrElse {
        null
    }
}

private val inlineDollarRegex = Regex("""^\$(.*?)\$""", RegexOption.DOT_MATCHES_ALL)
private val displayDollarRegex = Regex("""^\$\$(.*?)\$\$""", RegexOption.DOT_MATCHES_ALL)
private val inlineParenRegex = Regex("""^\\\((.*?)\\\)""", RegexOption.DOT_MATCHES_ALL)
private val displayBracketRegex = Regex("""^\\\[(.*?)\\\]""", RegexOption.DOT_MATCHES_ALL)

private fun processLatex(latex: String): String {
    val trimmed = latex.trim()
    return when {
        displayDollarRegex.matches(trimmed) ->
            displayDollarRegex.find(trimmed)?.groupValues?.get(1)?.trim() ?: trimmed

        inlineDollarRegex.matches(trimmed) ->
            inlineDollarRegex.find(trimmed)?.groupValues?.get(1)?.trim() ?: trimmed

        displayBracketRegex.matches(trimmed) ->
            displayBracketRegex.find(trimmed)?.groupValues?.get(1)?.trim() ?: trimmed

        inlineParenRegex.matches(trimmed) ->
            inlineParenRegex.find(trimmed)?.groupValues?.get(1)?.trim() ?: trimmed

        else -> trimmed
    }
}
