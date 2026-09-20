package com.example.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueAccent
import com.example.util.LinkHelper

@Composable
fun LinkifiedText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = Color(0xFF1E293B),
    linkColor: Color = BlueAccent,
    lineHeight: TextUnit = 21.sp,
    maxLines: Int = Int.MAX_VALUE,
    onNonLinkClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val links = remember(text) { LinkHelper.extractLinks(text) }

    if (links.isEmpty()) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            lineHeight = lineHeight,
            maxLines = maxLines,
            overflow = if (maxLines != Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip,
            modifier = modifier
        )
    } else {
        val annotatedString = remember(text, color, linkColor) {
            LinkHelper.buildLinkifiedAnnotatedString(text, color, linkColor)
        }

        ClickableText(
            text = annotatedString,
            modifier = modifier,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color,
                lineHeight = lineHeight
            ),
            maxLines = maxLines,
            overflow = if (maxLines != Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip,
            onClick = { offset ->
                val annotation = annotatedString
                    .getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()

                if (annotation != null) {
                    LinkHelper.openUrl(context, annotation.item)
                } else {
                    onNonLinkClick?.invoke()
                }
            }
        )
    }
}
