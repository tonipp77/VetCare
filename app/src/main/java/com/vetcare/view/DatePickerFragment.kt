package com.vetcare.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(val listener: (day: Int, month: Int, year: Int) -> Unit) :
    DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener(dayOfMonth, month, year)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // sacamos los valores del día de hoy actual
        // pero iniciamos y bloqueamos la cita como mínimo
        // al siguiente día
        val today = Calendar.getInstance()
        val day = today.get(Calendar.DAY_OF_MONTH)
        val month = today.get(Calendar.MONTH)
        val year = today.get(Calendar.YEAR)

        today.set(year, month, day+1)

        val picker = DatePickerDialog(activity as Context, this, year, month, day)
        picker.datePicker.minDate = today.timeInMillis
        return picker
    }

}