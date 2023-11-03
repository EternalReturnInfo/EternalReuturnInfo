package com.erionna.eternalreturninfo.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.erionna.eternalreturninfo.databinding.ChatListFragmentBinding
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.model.Message
import com.erionna.eternalreturninfo.ui.activity.ChatActivity
import com.erionna.eternalreturninfo.ui.adapter.ChatListAdapter
import com.erionna.eternalreturninfo.ui.viewmodel.ChatListViewModel
import com.erionna.eternalreturninfo.ui.viewmodel.ChatListViewModelFactory
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_MODEL
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_POSITION
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_MESSAGE
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_TIME
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatListFragment : Fragment() {

    companion object {
        fun newInstance() = ChatListFragment()
    }

    private var _binding: ChatListFragmentBinding? = null
    private val binding get() = _binding!!

    private var auth = Firebase.auth
    private lateinit var database: DatabaseReference

    private val chatListAdapter by lazy {
        ChatListAdapter(
            onClickItem = { position, item ->
                Log.d("choco5733 list", "$item")
                chatLauncher.launch(
                    ChatActivity.newIntentForModify(
                        requireContext(),
                        position,
                        item
                    )
                )
            }
        )
    }

//    private val viewModel: ChatListViewModel by viewModels {
//        ChatListViewModelFactory()
//    }

    private val viewModel: ChatListViewModel by lazy {
        ViewModelProvider(this, ChatListViewModelFactory())[ChatListViewModel::class.java]
    }

    private val chatLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val position = result.data?.getIntExtra(EXTRA_ER_POSITION, -1)
                val message = result.data?.getStringExtra(EXTRA_MESSAGE)
                val time = result.data?.getStringExtra(EXTRA_TIME)
                val eRModel = result.data?.getParcelableExtra<ERModel>(EXTRA_ER_MODEL)

                Log.d("choco5733 : 돌아왔을때", "$message $time $position")
                viewModel.modifyItem(position!!, message!!, time!!)
            }
        }

    override fun onResume() {
        chatListAdapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChatListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initModel()
//        setDataFromChatting()
//        setDataFromDatabase()
    }

    private fun setDataFromChatting() = with(binding) {
        database = Firebase.database.reference
    }


    private fun initView() = with(binding) {
        chatListRecyclerview.adapter = chatListAdapter
        chatListRecyclerview.layoutManager = LinearLayoutManager(context)
        chatListRecyclerview.itemAnimator = null
//        viewModel.addUser(ERModel(id = 1, name = "장재용", msg = "뭐요", time = "아앙기모디"))
//        viewModel.addUser(ERModel(id = 1, name = "장재용", msg = "뭐요", time = "아앙기모디"))
//        viewModel.addUser(ERModel(id = 1, name = "장재용2", msg = "뭐요", time = "아앙기모디"))

        Log.d("choco5744", "${viewModel.currentList()}")
        whatTheFuck()

    }



    private fun whatTheFuck() {
        database = Firebase.database.reference
    database.child("user").get().addOnSuccessListener {
            // 리스트 초기화
//                viewModel.clearList()
            var senderRoom = ""
            var receiverRoom = ""

            for (child in it.children) {
                val currentUser = child.getValue(ERModel::class.java)
                senderRoom = currentUser?.uid + auth.currentUser?.uid


                database.child("chats").child(senderRoom).child("messages")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("choco5744", "sender : ${senderRoom}")
                            var message = Message()
                            for (child in snapshot.children) {
                                message = child.getValue(Message::class.java)!!
                            }
                            if (message.whereRU == true) {
                                viewModel.addUser(
                                    currentUser?.copy(
                                        msg = message.message,
                                        time = message.time,
                                        readOrNot = message.readOrNot
                                    )
                                )

                                Log.d("choco5744", "currentUser : ${currentUser}")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
            }

        }
    }

    private fun initModel() = with(viewModel) {
        list.observe(viewLifecycleOwner) {
            chatListAdapter.submitList(it)
        }
    }

    private fun setDataFromDatabase() = with(binding) {

        database = Firebase.database.reference
        // 회원 정보 가져오기
        database.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 리스트 초기화
                viewModel.clearList()
                var senderRoom: String
                var receiverRoom: String

                for (child in snapshot.children) {
                    val currentUser = child.getValue(ERModel::class.java)
                    senderRoom = auth.currentUser?.uid + currentUser?.uid
                    receiverRoom = currentUser?.uid + auth.currentUser?.uid
                    Log.d("choco5733" , "senderRoom : ${senderRoom}")
                    Log.d("choco5733" , "receiverRoom : ${receiverRoom}")

                    var message = Message()
                    var convertTime = ""
                    var sb = StringBuilder()

                    database.child("chats").child(receiverRoom).child("messages")
                        .get().addOnSuccessListener {
                            for (child in it.children) {
                                message = child.getValue(Message::class.java)!!
                            }
                            Log.d("choco5733 in msg", "$message")

                            if (auth.currentUser?.uid != currentUser?.uid) {
                                if (message.time != "") {
                                    sb.append(message.time)
                                    convertTime = sb.substring(0, 13)
                                } else {
                                    convertTime = message.time!!
                                }

                                viewModel.addUser(
                                    currentUser?.copy(
                                        msg = "${message.message}",
                                        time = convertTime,
                                        readOrNot = message.readOrNot
                                    )
                                )
                            } else {
                                // 현재 접속자 상단에 표시
                                binding.chatListTitle.text = " ${currentUser?.name} 님 반갑습니다!"
                                Log.d("choco5733 currentuser", "${currentUser?.name}")
                            }
                        }


                        database.child("chats").child(receiverRoom).child("messages")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    for (child in snapshot.children) {
                                        message = child.getValue(Message::class.java)!!
                                    }
                                    Log.d("choco5733 in msg", "$message")

                                    if (auth.currentUser?.uid != currentUser?.uid) {
                                        if (message.time != "") {
                                            sb.append(message.time)
                                            convertTime = sb.substring(0, 13)
                                        } else {
                                            convertTime = message.time!!
                                        }

                                        viewModel.modifyItem2(
                                            currentUser?.copy(
                                                msg = "${message.message}",
                                                time = convertTime,
                                                readOrNot = message.readOrNot
                                            )
                                        )
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 가져오기 실패 시
                Toast.makeText(requireContext(), "가져오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
