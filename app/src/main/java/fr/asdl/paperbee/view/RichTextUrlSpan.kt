package fr.asdl.paperbee.view

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.TextView

interface UrlSpanLongClickListener {
    fun onUrlLongClick(span: RichTextUrlSpan)
}

class RichTextUrlSpan(url: String) : URLSpan(url) {
    fun onLongClick(textView: TextView) =
        (textView as? UrlSpanLongClickListener)?.onUrlLongClick(this)
}

class LongClickLinkMovementMethod : LinkMovementMethod() {
    private var isLongPressed = false
    override fun onTouchEvent(
        widget: TextView, buffer: Spannable,
        event: MotionEvent
    ): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_CANCEL)
            widget.handler.removeCallbacksAndMessages(null)
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(off, off, RichTextUrlSpan::class.java)
            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    widget.handler.removeCallbacksAndMessages(null)
                    if (!isLongPressed)
                        link[0].onClick(widget)
                    isLongPressed = false
                } else {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                    widget.handler.postDelayed({
                        link[0].onLongClick(widget)
                        isLongPressed = true
                    }, ViewConfiguration.getLongPressTimeout().toLong())
                }
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    companion object {
        private var mInstance: LongClickLinkMovementMethod? = null
        val instance: LongClickLinkMovementMethod get() {
            if (mInstance == null) mInstance = LongClickLinkMovementMethod()
            return mInstance!!
        }
    }

}