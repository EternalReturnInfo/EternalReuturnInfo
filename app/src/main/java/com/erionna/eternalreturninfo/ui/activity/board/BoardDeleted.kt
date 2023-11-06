package com.erionna.eternalreturninfo.ui.activity.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.api.load
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.BoardDeletedActivityBinding

class BoardDeleted : AppCompatActivity() {

    private lateinit var binding: BoardDeletedActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BoardDeletedActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.boardDeletedIv.load(R.drawable.ic_board_alert)

        binding.boardDeletedBtnOk.setOnClickListener {
            finish()
        }

        binding.boardDeletedBtnBack.setOnClickListener {
            finish()
        }

    }
}