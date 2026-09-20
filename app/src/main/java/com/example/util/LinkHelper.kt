package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import java.util.regex.Pattern

data class LinkMatch(
    val startIndex: Int,
    val endIndex: Int,
    val displayUrl: String,
    val fullUrl: String
)

object LinkHelper {
    // Regex matching standard URLs, www.*, and common domain extensions (e.g. .com, .id, .org, .net, etc.)
    private val URL_PATTERN = Pattern.compile(
        """(?i)\b(?:https?://|www\.)[a-zA-Z0-9+&@#/%?=~_|!:,.;]*[a-zA-Z0-9+&@#/%=~_|]|\b[a-zA-Z0-9.-]+\.(?:com|org|net|id|io|co|me|app|dev|xyz|ai|link|edu|gov|ly)(?:/[a-zA-Z0-9+&@#/%?=~_|!:,.;]*)?"""
    )

    private val TRAILING_PUNCTUATION = charArrayOf('.', ',', '!', '?', ':', ';', ')', ']', '}')

    /**
     * Extracts all valid link matches with their start and end character ranges.
     */
    fun extractLinks(text: String): List<LinkMatch> {
        if (text.isBlank()) return emptyList()

        val results = mutableListOf<LinkMatch>()
        val matcher = URL_PATTERN.matcher(text)

        while (matcher.find()) {
            var rawMatch = matcher.group()
            val start = matcher.start()
            var end = matcher.end()

            // Strip trailing punctuation so "kunjungi https://google.com!" doesn't include "!"
            while (rawMatch.isNotEmpty() && TRAILING_PUNCTUATION.contains(rawMatch.last())) {
                rawMatch = rawMatch.dropLast(1)
                end--
            }

            if (rawMatch.isNotBlank()) {
                val fullUrl = if (!rawMatch.startsWith("http://", ignoreCase = true) &&
                    !rawMatch.startsWith("https://", ignoreCase = true)
                ) {
                    "https://$rawMatch"
                } else {
                    rawMatch
                }

                results.add(LinkMatch(start, end, rawMatch, fullUrl))
            }
        }
        return results
    }

    /**
     * Builds an AnnotatedString with blue underline styling for any matched links.
     */
    fun buildLinkifiedAnnotatedString(
        text: String,
        normalColor: Color,
        linkColor: Color
    ): AnnotatedString {
        val links = extractLinks(text)
        if (links.isEmpty()) {
            return AnnotatedString(text)
        }

        return buildAnnotatedString {
            var lastIndex = 0
            for (link in links) {
                if (link.startIndex > lastIndex && link.startIndex <= text.length) {
                    append(text.substring(lastIndex, link.startIndex))
                }
                if (link.startIndex < text.length) {
                    val actualEnd = minOf(link.endIndex, text.length)
                    pushStringAnnotation(tag = "URL", annotation = link.fullUrl)
                    withStyle(
                        style = SpanStyle(
                            color = linkColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(text.substring(link.startIndex, actualEnd))
                    }
                    pop()
                    lastIndex = actualEnd
                }
            }
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    /**
     * Safely opens a URL in an external browser.
     */
    fun openUrl(context: Context, url: String) {
        try {
            val safeUrl = if (!url.startsWith("http://", ignoreCase = true) &&
                !url.startsWith("https://", ignoreCase = true)
            ) {
                "https://$url"
            } else {
                url
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka link: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
