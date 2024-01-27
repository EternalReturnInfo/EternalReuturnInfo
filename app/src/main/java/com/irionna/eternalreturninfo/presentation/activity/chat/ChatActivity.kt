package com.irionna.eternalreturninfo.presentation.activity.chat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.irionna.eternalreturninfo.databinding.ChatActivityBinding
import com.irionna.eternalreturninfo.data.model.ERModel
import com.irionna.eternalreturninfo.presentation.adapter.chat.ChatAdapter
import com.irionna.eternalreturninfo.presentation.viewmodel.ChatViewModel
import com.irionna.eternalreturninfo.presentation.viewmodel.ChatViewModelFactory
import com.irionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_MODEL
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    companion object {
        fun newIntent(
            context: Context,
            erModel: ERModel
        ): Intent {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_ER_MODEL, erModel)
            }
            return intent
        }
    }

    // 뷰바인딩
    private lateinit var binding: ChatActivityBinding

    // 파이어베이스 auth 초기화
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    // data
    private val data by lazy {
        intent.getParcelableExtra<ERModel>(EXTRA_ER_MODEL)
    }

    // viewModel
    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(this, ChatViewModelFactory())[ChatViewModel::class.java]
    }


    private val chatAdapter by lazy {
        ChatAdapter(
            onClickItem = { position ->
                val imm =
                    this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.chatMsgEt.windowToken, 0)
            }
        )
    }

    override fun onBackPressed() {
        viewModel.disconnectFirebase()
        finish()
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initModel()
    }

    private fun initView() = with(binding) {
        // 리사이클러뷰 초기화
        chatRecycler.adapter = chatAdapter
        chatRecycler.layoutManager = LinearLayoutManager(this@ChatActivity)

        // 툴바에 채팅상대 이름 출력하기
        chatToolbarTitle.text = data?.name

        // 뒤로가기 클릭 시 채팅방에서 빠져나옴
        chatBackBtn.setOnClickListener{
            viewModel.disconnectFirebase()
            finish()
        }
    }

    private fun initModel() = with(viewModel) {
        list.observe(this@ChatActivity){
            chatAdapter.submitList(it)
        }
        message.observe(this@ChatActivity){
            binding.chatMsgEt.setText(it)
        }
        notify.observe(this@ChatActivity){
            chatAdapter.notifyDataSetChanged()
        }
        scroll.observe(this@ChatActivity){
            binding.chatRecycler.scrollToPosition(getListSize() - 1)
        }

        binding.chatSendBtn.setOnClickListener {
            saveChat(data?.uid.toString(), auth.uid.toString(), binding.chatMsgEt.text.toString())
        }

        loadChat(data?.name.toString(), data?.uid.toString(), auth.uid.toString())
    }
}