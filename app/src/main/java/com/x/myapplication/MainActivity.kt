package com.hiconnect.app

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val titleText = TextView(this).apply {
            text = "하이커넥트"
            textSize = 32f
        }

        val subtitleText = TextView(this).apply {
            text = "가까운 수소충전소를 추천합니다"
            textSize = 18f
        }

        val button = Button(this).apply {
            text = "충전소 찾기"
        }

        layout.addView(titleText)
        layout.addView(subtitleText)
        layout.addView(button)

        setContentView(layout)
    }
}