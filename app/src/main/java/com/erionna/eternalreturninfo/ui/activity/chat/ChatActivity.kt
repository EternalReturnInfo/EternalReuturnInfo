package com.erionna.eternalreturninfo.ui.activity.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.erionna.eternalreturninfo.databinding.ChatActivityBinding
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.model.Message
import com.erionna.eternalreturninfo.ui.adapter.chat.ChatAdapter
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_MODEL
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_POSITION
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_MESSAGE
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_TIME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

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
        fun newIntentForModify(
            context: Context,
            position: Int,
            erModel: ERModel
        ) = Intent(context, ChatActivity::class.java).apply {
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

    // position
    private val position by lazy {
        intent.getIntExtra(EXTRA_ER_POSITION, -1)
    }

    // data
    private val data by lazy {
        intent.getParcelableExtra<ERModel>(EXTRA_ER_MODEL)
    }

    // reference
    private lateinit var refDb: DatabaseReference
    private lateinit var refEventListener: ValueEventListener

    // AtomicLong
    private val idGenerate = AtomicLong(1L)

    // chatAdpater
    private val chatAdapter by lazy {
        ChatAdapter(
            messageList,
            onClickItem = { _ ->
                val imm =
                    this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.chatMsgEt.windowToken, 0)
            }
        )
    }

    override fun onBackPressed() {
        refDb.removeEventListener(refEventListener)
        finish()
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        saveChat()
        loadChat()

    }

    private fun initView() = with(binding) {
        // 리사이클러뷰 초기화
        chatRecycler.adapter = chatAdapter
        chatRecycler.layoutManager = LinearLayoutManager(this@ChatActivity)

        // 툴바에 채팅상대 이름 출력하기
        chatToolbarTitle.text = data?.name

        // 뒤로가기 클릭 시 채팅방에서 빠져나옴
        chatBackBtn.setOnClickListener{
            refDb.removeEventListener(refEventListener)
            finish()
        }


    }

    private fun saveChat() {
        receiverName = data?.name.toString()
        receiverUid = data?.uid.toString()

        // 로그인 한 사용자 uid
        val senderUid = auth.currentUser?.uid

        // 보낸이 방
        senderRoom = receiverUid + senderUid
        // 받는이 방
        receiverRoom = senderUid + receiverUid

        // 메시지 저장하기
        binding.chatSendBtn.setOnClickListener {
            val time = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a hh시 mm분"))
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            // et에 입력한 메시지
            val message = binding.chatMsgEt.text.toString()
            val messageObject = Message(id = "${idGenerate.getAndIncrement()}" + time, message = message , sendId = senderUid, time = time, receiverId = receiverUid, readOrNot = false, whereRU = true)

            if (message != "") {
                // 송수신 방 둘 다 저장
                database.child("chats").child(senderRoom).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        database.child("chats").child(receiverRoom).child("messages").push()
                            .setValue(messageObject)
                    }

                // 메시지 전송 후 EditText 공백 처리
                binding.chatMsgEt.setText("")

                // 전송 버튼을 누르면 whereRU를 true로 바꿔줘 채팅리스트에 추가
                val map = HashMap<String, Any>()
                map.put("whereRU", true)

            }
        }
    }

    private fun loadChat() {
        receiverName = data?.name.toString()
        receiverUid = data?.uid.toString()

        // 로그인 한 사용자 uid
        val senderUid = auth.currentUser?.uid

        // 보낸이 방
        senderRoom = receiverUid + senderUid
        // 받는이 방
        receiverRoom = senderUid + receiverUid

        var finalMessage = ""
        var finalTime = ""

        refDb = database.child("chats").child(senderRoom).child("messages")
        refEventListener = object :ValueEventListener {
            override fun onDataChange(snapShot: DataSnapshot) {
                // 새로운 메시지 송, 수신시 최하단 화면으로 이동
                binding.chatRecycler.scrollToPosition(messageList.size)

                messageList.clear()

                for (child in snapShot.children) {
                    val message = child.getValue(Message::class.java)
                    var readOrNot: Boolean = false

                    messageList.add(message!!)

                    // 채팅방 들어왔을시 가장 밑으로 이동
                    binding.chatRecycler.scrollToPosition(messageList.size - 1)

                    // 마지막으로 읽은 메시지와 시간을 채팅목록 화면에 주기위한 코드
                    finalMessage = messageList.last().message.toString()
                    finalTime = messageList.last().time.toString()

                    // 가져왔을 시 readOrNot을 true로 변경
                    val map = HashMap<String, Any>()
                    map.put("readOrNot", true)

                    val key = child.key
                    database.child("chats").child(senderRoom)
                        .child("messages").child("$key").updateChildren(map)
                }

                val intent = Intent().apply {
                    putExtra(
                        EXTRA_MESSAGE,
                        finalMessage
                    )
                    putExtra(
                        EXTRA_TIME,
                        finalTime
                    )
                    Log.d("choco5744", "finaltime : $finalTime")
                    putExtra(
                        EXTRA_ER_POSITION,
                        position
                    )
                }
                setResult(Activity.RESULT_OK, intent)
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("choco5733", error.message)
            }
        }

        // 메시지 가져오기
        refDb.addValueEventListener(refEventListener)
    }
}