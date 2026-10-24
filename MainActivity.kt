package com.mchat.app

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val bg = Color.rgb(7, 27, 38)
    private val panel = Color.rgb(14, 43, 57)
    private val teal = Color.rgb(100, 204, 197)
    private val white = Color.rgb(242, 248, 250)
    private val muted = Color.rgb(155, 181, 191)
    private val messages = mutableMapOf<String, MutableList<Pair<Boolean, String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(bg)
        setPadding(dp(20), dp(18), dp(20), dp(12))
        layoutParams = ViewGroup.LayoutParams(-1, -1)
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun rounded(color: Int, radius: Float = 18f): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()

    private fun showHome() {
        val root = baseLayout()
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("MCHAT", 28f, white, true))
        brand.addView(text("Stay close, wherever you are", 13f, muted))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val avatar = TextView(this).apply {
            text = "M"
            textSize = 18f
            setTextColor(bg)
            gravity = Gravity.CENTER
            background = rounded(teal, 24f)
        }
        header.addView(avatar, LinearLayout.LayoutParams(dp(46), dp(46)))
        root.addView(header)
        root.addView(space(24))

        val search = EditText(this).apply {
            hint = "Search conversations"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 14f
            singleLine = true
            background = rounded(panel, 16f)
            setPadding(dp(16), 0, dp(16), 0)
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)))
        root.addView(space(22))
        root.addView(text("MESSAGES", 12f, teal, true))
        root.addView(space(10))

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val chats = listOf(
            Triple("Ahlam", "Salam! kif dayr?", "A"),
            Triple("Family Group", "Photo received", "F"),
            Triple("Youssef", "Nchoufek mn b3d", "Y")
        )
        chats.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(13), dp(12), dp(13))
                background = rounded(panel, 18f)
                isClickable = true
                isFocusable = true
                setOnClickListener { showChat(item.first) }
            }
            val bubble = TextView(this).apply {
                text = item.third
                textSize = 18f
                setTextColor(bg)
                gravity = Gravity.CENTER
                background = rounded(teal, 30f)
            }
            row.addView(bubble, LinearLayout.LayoutParams(dp(48), dp(48)))
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            info.addView(text(item.first, 16f, white, true))
            info.addView(space(4))
            info.addView(text(item.second, 13f, muted))
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(text(if (index == 0) "12:42" else "Yesterday", 11f, muted))
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(10)
            list.addView(row, params)
        }
        root.addView(list)
        val spacer = View(this)
        root.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))
        val note = text("Prototype • messages are saved only on this phone for now", 11f, muted)
        note.gravity = Gravity.CENTER
        root.addView(note)
        setContentView(root)

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                for (i in 0 until list.childCount) {
                    val child = list.getChildAt(i)
                    child.visibility = if (chats[i].first.contains(s.toString(), true)) View.VISIBLE else View.GONE
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun showChat(name: String) {
        val root = baseLayout()
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val back = Button(this).apply {
            text = "‹"
            textSize = 26f
            setTextColor(teal)
            background = rounded(panel, 14f)
            setOnClickListener { showHome() }
        }
        header.addView(back, LinearLayout.LayoutParams(dp(48), dp(46)))
        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        titleBox.addView(text(name, 19f, white, true))
        titleBox.addView(text("MCHAT conversation", 12f, muted))
        header.addView(titleBox)
        root.addView(header)
        root.addView(space(18))

        val transcript = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        scroll.addView(transcript)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        fun renderMessages() {
            transcript.removeAllViews()
            val current = messages.getOrPut(name) {
                mutableListOf(false to "Salam! مرحبا بك فـ MCHAT 👋")
            }
            current.forEach { (mine, body) ->
                val line = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = if (mine) Gravity.END else Gravity.START
                    setPadding(0, dp(4), 0, dp(4))
                }
                val msg = TextView(this).apply {
                    text = body
                    textSize = 15f
                    setTextColor(if (mine) bg else white)
                    setPadding(dp(14), dp(10), dp(14), dp(10))
                    background = rounded(if (mine) teal else panel, 16f)
                    maxWidth = dp(270)
                }
                line.addView(msg)
                transcript.addView(line)
            }
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
        renderMessages()

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val input = EditText(this).apply {
            hint = "Write a message..."
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 14f
            background = rounded(panel, 18f)
            setPadding(dp(14), 0, dp(14), 0)
            singleLine = true
        }
        composer.addView(input, LinearLayout.LayoutParams(0, dp(52), 1f))
        val send = Button(this).apply {
            text = "➤"
            setTextColor(bg)
            background = rounded(teal, 18f)
        }
        val sendParams = LinearLayout.LayoutParams(dp(54), dp(52))
        sendParams.leftMargin = dp(8)
        composer.addView(send, sendParams)
        send.setOnClickListener {
            val body = input.text.toString().trim()
            if (body.isNotEmpty()) {
                messages.getOrPut(name) { mutableListOf() }.add(true to body)
                input.setText("")
                renderMessages()
            }
        }
        root.addView(space(10))
        root.addView(composer)
        setContentView(root)
    }

    private fun space(height: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }
}
