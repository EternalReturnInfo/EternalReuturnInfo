package com.irionna.eternalreturninfo.presentation.activity.board

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.irionna.eternalreturninfo.databinding.BoardDialogBinding
import com.irionna.eternalreturninfo.databinding.ReportDialog2Binding
import com.irionna.eternalreturninfo.databinding.ReportDialogBinding

class ReportDialog2(context: Context, private val dialogListener: DialogListener) : Dialog(context) {

    private lateinit var binding: ReportDialog2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ReportDialog2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.reportDialog2BtnYes.setOnClickListener {
            dialogListener.onOKButtonClicked()
            dismiss()
        }

    }
}