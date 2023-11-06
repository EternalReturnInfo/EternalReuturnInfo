package com.erionna.eternalreturninfo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.erionna.eternalreturninfo.databinding.ChatListItemBinding
import com.erionna.eternalreturninfo.model.ERModel

class ChatListAdapter(
    private val onClickItem: (Int, ERModel) -> Unit
) : ListAdapter<ERModel, ChatListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ERModel>() {
        override fun areItemsTheSame(
            oldItem: ERModel,
            newItem: ERModel
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: ERModel,
            newItem: ERModel
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.ViewHolder {
        return ViewHolder(
            ChatListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onClickItem
        )
    }

    override fun onBindViewHolder(holder: ChatListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

   class ViewHolder(
        private val binding: ChatListItemBinding,
        private val onClickItem: (Int, ERModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ERModel) = with(binding) {
            chatListName.text = item.name
            chatListProfilePicture.load(item.profilePicture.toString())
            chatListMsg.text = item.msg
            chatListDate.text = item.time

            chatListNewMsg.isVisible = item.readOrNot == false

            chatListContainer.setOnClickListener {
                onClickItem(
                    adapterPosition,
                    item
                )
            }
        }
    }
}