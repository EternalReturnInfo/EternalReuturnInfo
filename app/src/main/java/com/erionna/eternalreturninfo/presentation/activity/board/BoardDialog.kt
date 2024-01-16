package com.erionna.eternalreturninfo.presentation.activity.board

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.erionna.eternalreturninfo.databinding.BoardDialogBinding


interface DialogListener {
    fun onOKButtonClicked()
}

class BoardDialog(context: Context, val userName: String, private val dialogListener: DialogListener) : Dialog(context) {

    private lateinit var binding: BoardDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = BoardDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.boardDialogMessage.text = "${userName}님과 1:1 채팅을 하시겠습니까?"

        binding.boardDialogBtnYes.setOnClickListener {
            dialogListener.onOKButtonClicked()
            dismiss()
        }

        binding.boardDialogBtnNo.setOnClickListener {
            dismiss()
        }
    }
}