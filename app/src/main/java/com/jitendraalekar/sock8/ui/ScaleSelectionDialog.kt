package com.jitendraalekar.sock8.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jitendraalekar.sock8.R

class ScaleSelectionDialog(var isRecent : Boolean) : DialogFragment() {

    internal lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogPositiveClick(isRecent: Boolean)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Set the dialog title
            builder.setTitle(R.string.sscale_dialog_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(R.array.scales, if(isRecent) 0 else 1) { _, which ->
                    isRecent = which == 0
                }
                // Set the action buttons
                .setPositiveButton(R.string.ok) { _, id ->
                    listener.onDialogPositiveClick(isRecent)
                }
                .setNegativeButton(
                    R.string.cancel) { _, id ->
                    dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}