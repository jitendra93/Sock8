package com.jitendraalekar.sock8.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jitendraalekar.sock8.R

class SensorSelectionDialog(private val listOfSensors : List<String>) : DialogFragment() {

    internal lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogPositiveClick(sensor : String)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            var selectedItem : String? = null
            val builder = AlertDialog.Builder(it)
            // Set the dialog title
            builder.setTitle(R.string.sensor_dialog_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(listOfSensors.toTypedArray(), -1
                ) { _, which ->
                    selectedItem = listOfSensors[which]
                }
                // Set the action buttons
                .setPositiveButton(R.string.ok
                ) { _, id ->
                    selectedItem?.let {
                        listener.onDialogPositiveClick(it)
                    }

                }
                .setNegativeButton(
                    R.string.cancel
                ) { _, id ->
                    dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}