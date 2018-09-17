package com.allow.food4needy.common

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import com.allow.food4needy.R
import timber.log.Timber

@Suppress("DEPRECATION")
fun plainTextToHtml(source: String): Spanned {
    val str: Spanned
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        str = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    } else {
        str = Html.fromHtml(source)
    }
    return str
}

fun htmlToPlainText(source: String): String {
    val chars = CharArray(plainTextToHtml(source).length)
    TextUtils.getChars(source, 0, source.length, chars, 0)
    return String(chars)
}

fun createLinkedText(context: Context, text: String, startIndex: Int, lastIndex: Int, linkUrl: String): SpannableString {
    val forgotPassSpanStr = SpannableString(text)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val pm = context.packageManager
            val startIntent = Intent(ACTION_VIEW, Uri.parse(linkUrl))
            if (startIntent.resolveActivity(pm) != null) {
                context.startActivity(startIntent)
            } else {
                Timber.d("> No Activity available to open: $linkUrl")
            }
        }
    }
    forgotPassSpanStr.setSpan(clickableSpan, startIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    forgotPassSpanStr.setSpan(UnderlineSpan(), startIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    forgotPassSpanStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimaryDark)), startIndex, lastIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    return forgotPassSpanStr
}