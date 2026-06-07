package com.example.personalbet.ui.help

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.personalbet.R

object TutorialDialog {

    fun show(context: Context) {
        val view = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_tutorial, null)
        view.findViewById<android.widget.TextView>(R.id.text_tutorial).text =
            context.getString(R.string.tutorial_body)

        AlertDialog.Builder(context)
            .setTitle(R.string.tutorial_title)
            .setView(view)
            .setPositiveButton(R.string.tutorial_close, null)
            .show()
    }
}
