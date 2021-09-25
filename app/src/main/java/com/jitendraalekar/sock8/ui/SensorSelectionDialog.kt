package com.jitendraalekar.sock8.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jitendraalekar.sock8.R

class SensorSelectionDialog(val listOfSensors : Map<String, Boolean>) : DialogFragment() {

    internal lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogPositiveClick(listOfSensors : Map<String, Boolean>)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = listOfSensors.toList().toMutableList()
            val builder = AlertDialog.Builder(it)
            // Set the dialog title
            builder.setTitle(R.string.dialog_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(listOfSensors.keys.toTypedArray(), listOfSensors.values.toBooleanArray(),
                    DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
                        selectedItems[which] = selectedItems[which].copy(second = isChecked)
                    })
                // Set the action buttons
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogPositiveClick(selectedItems.toMap())
                    })
                .setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                      dismiss()
                    })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}