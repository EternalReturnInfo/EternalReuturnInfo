package com.erionna.eternalreturninfo.ui.adapter.board

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.BoardRvItemBinding
import com.erionna.eternalreturninfo.model.BoardModel
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.retrofit.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class BoardRecyclerViewAdapter() : ListAdapter<BoardModel, BoardRecyclerViewAdapter.ViewHolder>(

    object : DiffUtil.ItemCallback<BoardModel>() {
        override fun areItemsTheSame(
            oldItem: BoardModel,
            newItem: BoardModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: BoardModel,
            newItem: BoardModel
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    interface OnItemClickListener {
        fun onItemClick(boardItem: BoardModel)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BoardRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(
        private val binding: BoardRvItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BoardModel) = with(binding) {

            FBRef.userRef.child(item?.author.toString()).addValueEventListener(object :
                ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists()){
                        val author = snapshot.getValue<ERModel>()

                        boardPostIvProfile.load(author?.profilePicture)
                        boardPostTvUser.text = author?.name

                    }else{

                        boardPostIvProfile.load(R.drawable.ic_baseimage)
                        boardPostTvUser.text = "탈퇴한 회원"

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

            if(item.category == "공지"){
                boardPostCategory.setBackgroundResource(R.drawable.shape_board_category_notice)
                boardPostCategory.text = "공지"
            }else{
                boardPostCategory.setBackgroundResource(R.drawable.shape_board_category)
                boardPostCategory.text = item.category
            }

            boardPostTvTitle.text = item.title
            boardPostTvContent.text = item.content

            if(item.comments.size == 0){
                boardPostBtnComment.visibility = View.INVISIBLE
                boardPostIvComment.visibility = View.INVISIBLE
            }else{
                boardPostIvComment.visibility = View.VISIBLE
                boardPostBtnComment.visibility = View.VISIBLE
                boardPostBtnComment.text = item.comments.size.toString()
            }

            boardPostLayout.setOnClickListener {
                onItemClickListener?.onItemClick(item)
            }

        }
    }

}


