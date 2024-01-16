package com.erionna.eternalreturninfo.presentation.fragment.chat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.api.load
import com.erionna.eternalreturninfo.databinding.ChatListFragmentBinding
import com.erionna.eternalreturninfo.data.model.ERModel
import com.erionna.eternalreturninfo.presentation.activity.chat.ChatActivity
import com.erionna.eternalreturninfo.presentation.adapter.chat.ChatListAdapter
import com.erionna.eternalreturninfo.presentation.viewmodel.ChatListViewModel
import com.erionna.eternalreturninfo.presentation.viewmodel.ChatListViewModelFactory
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_MODEL
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_ER_POSITION
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_MESSAGE
import com.erionna.eternalreturninfo.util.Constants.Companion.EXTRA_TIME
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase

class ChatListFragment : Fragment() {

    companion object {
        fun newInstance() = ChatListFragment()
    }

    private var _binding: ChatListFragmentBinding? = null
    private val binding get() = _binding!!

    private val chatListAdapter by lazy {
        ChatListAdapter(
            onClickItem = { position, item ->
                startActivity(
                    ChatActivity.newIntent(
                        requireContext(),
                        item
                    )
                )
            }
        )
    }

    private val viewModel: ChatListViewModel by lazy {
        ViewModelProvider(this, ChatListViewModelFactory())[ChatListViewModel::class.java]
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
        initViewModel()
    }

    private fun initView() = with(binding) {
        chatListRecyclerview.adapter = chatListAdapter
        chatListRecyclerview.layoutManager = LinearLayoutManager(context)
        chatListRecyclerview.itemAnimator = null
    }

    private fun initViewModel() = with(viewModel) {
        loadProfilePicture()

        list.observe(viewLifecycleOwner) {
            chatListAdapter.submitList(it)
        }

        profilePictureUrl.observe(viewLifecycleOwner) {
            binding.chatListMyProfilePicture.load(it)
        }

        profileName.observe(viewLifecycleOwner) {
            binding.chatListTitle.text = it
        }

        addChatList()
    }

    override fun onResume() {
        chatListAdapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnectFirebase()
//        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

//    override fun onStart() {
//        super.onStart()
//        initView()
//        initViewModel()
//    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
