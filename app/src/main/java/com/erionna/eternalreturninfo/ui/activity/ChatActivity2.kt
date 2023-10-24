package com.erionna.eternalreturninfo.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.erionna.eternalreturninfo.databinding.ChatActivityBinding
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.model.Message
import com.erionna.eternalreturninfo.ui.adapter.ChatAdapter
import com.erionna.eternalreturninfo.ui.adapter.ChatAdapter2
import com.erionna.eternalreturninfo.ui.viewmodel.ChatListViewModelFactory
import com.erionna.eternalreturninfo.ui.viewmodel.ChatViewModel
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_MODEL
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_POSITION
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_MESSAGE
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_TIME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

class ChatActivity2 : AppCompatActivity() {

    companion object {
        fun newIntent(
            context: Context,
            erModel: ERModel
        ): Intent {
            val intent = Intent(context, ChatActivity2::class.java).apply {
                putExtra(EXTRA_ER_MODEL, erModel)
            }
            return intent
        }
        fun newIntentForModify(
            context: Context,
            position: Int,
            erModel: ERModel
        ) = Intent(context, ChatActivity2::class.java).apply {
            putExtra(EXTRA_ER_MODEL, erModel)
            putExtra(EXTRA_ER_POSITION, position)
        }

    }

    // 뷰바인딩
    private lateinit var binding: ChatActivityBinding

    // 파이어베이스 auth 초기화
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    // 파이어베이스 db 초기화
    private val database by lazy {
        FirebaseDatabase.getInstance().reference
    }

    // 대화상대 정보
    private lateinit var receiverName: String
    private lateinit var receiverUid: String

    // 송수신 대화방
    private lateinit var receiverRoom: String
    private lateinit var senderRoom: String

    // messageList
    private val messageList = ArrayList<Message>()
    private val idGenerate = AtomicLong(1L)

    // position
    private val position by lazy {
        intent.getIntExtra(EXTRA_ER_POSITION, -1)
    }

    // viewModel
    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }

    private val chatAdapter2 by lazy {
        ChatAdapter2(
            onClickItem = { position ->
                val imm =
                    this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.chatMsgEt.windowToken, 0)
            }
        )
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initModel()

        // 채팅목록에서 전달받은 상대방 데이터 저장
        val data = intent.getParcelableExtra<ERModel>(EXTRA_ER_MODEL)
        Log.d("#choco5732", "$data")

        // 툴바에 채팅상대 이름 출력하기
        binding.chatToolbarTitle.text = data?.name

        receiverName = data?.name.toString()
        receiverUid = data?.uid.toString()
        Log.d("#choco5732", "receiverName : $receiverName  , receverUid = $receiverUid")

        // 리사이클러뷰 초기화
        binding.chatRecycler.adapter = chatAdapter2
        binding.chatRecycler.layoutManager = LinearLayoutManager(this)
        binding.chatRecycler.itemAnimator = null

        // 로그인 한 사용자 uid
        val senderUid = auth.currentUser?.uid

        // 보낸이 방
        senderRoom = receiverUid + senderUid
        // 받는이 방
        receiverRoom = senderUid + receiverUid
        Log.d("#choco5732", "senderRoom : $senderRoom")

        // 메시지 저장하기
        binding.chatSendBtn.setOnClickListener {
            val time = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a hh시 mm분"))
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            // et에 입력한 메시지
            val message = binding.chatMsgEt.text.toString()
            val messageObject = Message(id = idGenerate.getAndIncrement(), message = message , sendId = senderUid, time = time, receiverId = receiverUid, readOrNot = false)

            if (message != "") {
                // 송수신 방 둘 다 저장
                database.child("chats").child(senderRoom).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        database.child("chats").child(receiverRoom).child("messages").push()
                            .setValue(messageObject)
                    }

                // 메시지 전송 후 EditText 공백 처리
                binding.chatMsgEt.setText("")

            }
        }
        var finalMessage = ""
        var finalTime = ""

        // 메시지 가져오기
        database.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapShot: DataSnapshot) {
                    // 새로운 메시지 송, 수신시 최하단 화면으로 이동
//                    binding.chatRecycler.scrollToPosition(messageList.size)
                    binding.chatRecycler.scrollToPosition(viewModel.getListSize())

                    viewModel.clearList()
                    messageList.clear()


                    for (postSnapshot in snapShot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                        viewModel.addItem(message)
                        // 새로운 메시지 송, 수신시 최하단 화면으로 이동
                        binding.chatRecycler.scrollToPosition(viewModel.getListSize() - 1)

                        finalMessage = messageList.last().message.toString()
                        finalTime = messageList.last().time.toString()

                        // 가져왔을 시 readOrNot을 true로 변경
                        val map = HashMap<String, Any>()
                        map.put("readOrNot", true)

                        val key = postSnapshot.key
                        database.child("chats").child(senderRoom)
                            .child("messages").child("$key").updateChildren(map)
                    }
                    Log.d("choco5744","message : ${finalMessage}, time : ${finalTime}")

                    val intent = Intent().apply {
                        putExtra(
                            EXTRA_MESSAGE,
                            finalMessage
                        )
                        putExtra(
                            EXTRA_TIME,
                            finalTime
                        )
                        putExtra(
                            EXTRA_ER_POSITION,
                            position
                        )
                    }
                    setResult(Activity.RESULT_OK, intent)
//                    chatAdapter2.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    // observe 대상
    // fragment : viewLifeCycleOwner
    // activity : this
    private fun initModel() = with(viewModel) {
        list.observe(this@ChatActivity2){
            chatAdapter2.submitList(it)
        }
    }
}