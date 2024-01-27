package com.erionna.eternalreturninfo.presentation.adapter.chat


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.erionna.eternalreturninfo.databinding.ChatItemReceiverBinding
import com.erionna.eternalreturninfo.databinding.ChatItemSenderBinding
import com.erionna.eternalreturninfo.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatAdapter(
    private val onClickItem: (Int) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(
            oldItem: Message,
            newItem: Message
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Message,
            newItem: Message
        ): Boolean {
            return oldItem == newItem
        }
    }
) {
    enum class ItemViewType {
        SENDER, RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (
            viewType == ItemViewType.SENDER.ordinal
        ) {
            SenderViewHolder(
                ChatItemSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                onClickItem
            )
        } else {
            ReceiverViewHolder(
                ChatItemReceiverBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                onClickItem
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int
    ) {
        val item = getItem(position)

        if (holder.javaClass == SenderViewHolder::class.java) {
            holder as SenderViewHolder
            holder.bind(item)
        } else {
            holder as ReceiverViewHolder
            holder.bind(item)
        }
    }

    override fun getItemViewType(
        position: Int
    ): Int {
        val currentMessage = getItem(position)
        // 현재 메시지의 senduid와 접속자의 uid가 일치하면 전송모드, 불일치하면 수신모드
        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sendId)) {
            ItemViewType.SENDER.ordinal
        } else {
            ItemViewType.RECEIVER.ordinal
        }
    }


    class SenderViewHolder(
        val binding: ChatItemSenderBinding,
        private val onClickItem: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) = with(binding) {
            val sb = StringBuilder()
            sb.append(item.time)
            val time = sb.substring(14, 24)
            chatItemSenderDate.text = time
            chatItemSenderText.text = item.message

            val database = FirebaseDatabase.getInstance().reference
            var recevierRoom: String = item.sendId + item.receiverId

            database.child("chats").child(recevierRoom).child("messages")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapShot: DataSnapshot) {
                        var receiverReadOrNot: Boolean? = null
                        for (child in snapShot.children) {
                            val message = child.getValue(Message::class.java)
                            if (message?.id == item.id) {
                                receiverReadOrNot = message?.readOrNot
                            }
                        }
                        if (receiverReadOrNot == false) {
                            chatItemSenderReadCount.visibility = View.VISIBLE
                        } else {
                            chatItemSenderReadCount.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("choco5732 chatAdapter", error.message)
                    }
                })

            chatItemSenderContainer.setOnClickListener {
                onClickItem(
                    position
                )
            }
        }
    }

    class ReceiverViewHolder(
        val binding: ChatItemReceiverBinding,
        private val onClickItem: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) = with(binding) {
            val sb = StringBuilder()
            sb.append(item.time)
            val time = sb.substring(14, 24)

            chatItemRecevierDate.text = time
            chatItemReceiverText.text = item.message

            chatItemReceiverContainer.setOnClickListener {
                onClickItem(
                    position
                )
            }
        }
    }
}