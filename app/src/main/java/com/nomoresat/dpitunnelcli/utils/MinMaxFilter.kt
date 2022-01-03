package com.nomoresat.dpitunnelcli.utils

import android.text.InputFilter
import android.text.Spanned


class MinMaxFilter(private val range: IntRange): InputFilter{
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val inputStr = dest.toString() + source.toString()
            val inputInt = inputStr.toInt()
            if (inputInt in range)
                if (inputStr.first() == '0') { // Check for leading zeros
                    if (inputStr.length == 1) return null
                } else
                    return null
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }
}