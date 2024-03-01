package com.irionna.eternalreturninfo.presentation.activity.board

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.irionna.eternalreturninfo.databinding.BoardDialogBinding
import com.irionna.eternalreturninfo.databinding.ReportDialogBinding

class ReportDialog(context: Context, private val dialogListener: DialogListener) : Dialog(context) {

    private lateinit var binding: ReportDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ReportDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.reportDialogBtnYes.setOnClickListener {
            dialogListener.onOKButtonClicked()
            dismiss()
        }

        binding.reportDialogBtnNo.setOnClickListener {
            dismiss()
        }
    }
}