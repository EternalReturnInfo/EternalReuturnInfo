package com.erionna.eternalreturninfo.ui.adapter.board

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.BoardPostRvCommentItemBinding
import com.erionna.eternalreturninfo.model.CommentModel
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.retrofit.FBRef
import com.erionna.eternalreturninfo.ui.activity.board.BoardDialog
import com.erionna.eternalreturninfo.ui.activity.chat.ChatActivity
import com.erionna.eternalreturninfo.ui.activity.board.DialogListener
import com.erionna.eternalreturninfo.ui.activity.login.LoginPage
import com.erionna.eternalreturninfo.ui.activity.main.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.skydoves.powermenu.CircularEffect
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BoardCommentRecyclerViewAdpater(
    context: Context
) : ListAdapter<CommentModel, BoardCommentRecyclerViewAdpater.ViewHolder>(

    object : DiffUtil.ItemCallback<CommentModel>() {
        override fun areItemsTheSame(
            oldItem: CommentModel,
            newItem: CommentModel
        ): Boolean {
            return oldItem.author == newItem.author
        }

        override fun areContentsTheSame(
            oldItem: CommentModel,
            newItem: CommentModel
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    interface OnItemClickListener {
        fun onDeleteItemClick(commentItem: CommentModel, position:Int)
        fun onUpdateItemClick(commentItem: CommentModel, position:Int)
    }

    private var onDeleteItemClickListener: OnItemClickListener? = null
    private var onUpdateItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onDeleteItemClickListener = listener
        this.onUpdateItemClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BoardPostRvCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(
        private val binding: BoardPostRvCommentItemBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CommentModel) = with(binding) {

            FBRef.userRef.child(item?.author.toString()).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists()){
                        val author = snapshot.getValue<ERModel>()

                        boardCommentTvUser.text = author?.name

                        if(author?.profilePicture?.isEmpty() == true){
                            boardCommentIbProfile.setImageResource(R.drawable.ic_xiuk)
                        }else{
                            boardCommentIbProfile.load(author?.profilePicture)
                        }

                        // 로그인한 사용자면 ibMenu 보여주기
                        if (author?.uid == BoardSingletone.LoginUser().uid) {
                            boardCommentIbMenu.visibility = View.VISIBLE
                            boardCommentIbMenu.setOnClickListener {


                                // 팝업메뉴 onClick 리스너
                                val onMenuItemClickListener = object : OnMenuItemClickListener<PowerMenuItem> {
                                    override fun onItemClick(position: Int, item2: PowerMenuItem) {
                                        when (position) {
                                            // 0 : 수정,   1 : 삭제
                                            0 -> {
                                                onUpdateItemClickListener?.onUpdateItemClick(item, adapterPosition)
                                            }
                                            else -> {
                                                onDeleteItemClickListener?.onDeleteItemClick(item, adapterPosition)
                                            }
                                        }
                                    }
                                }

                                val powerMenu = PowerMenu.Builder(context)
                                    .addItem(PowerMenuItem("수정"))
                                    .addItem(PowerMenuItem("삭제"))
                                    .setMenuRadius(20f) // sets the corner radius.
                                    .setTextSize(18)
                                    .setWidth(400)
//                                    .setTextGravity(Gravity.CENTER)
                                    .setTextColor(ContextCompat.getColor(context, R.color.white))
                                    .setMenuColor(ContextCompat.getColor(context, R.color.darkgray))
                                    .setSelectedMenuColor(ContextCompat.getColor(context, R.color.black))
                                    .setOnMenuItemClickListener(onMenuItemClickListener)
//                                    .setLifecycleOwner(viewLifecycleOwner)
                                    .setCircularEffect(CircularEffect.BODY)
                                    .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
                                    .build()
                                    .showAsDropDown(it)
//                                    .dissmiss()

                            }
                        } else {
                            boardCommentIbMenu.visibility = View.INVISIBLE

                            boardCommentIbProfile.setOnClickListener {
                                val customDialog = BoardDialog(binding.root.context, author?.name.toString(), object : DialogListener {
                                    override fun onOKButtonClicked() {
                                        val intent = ChatActivity.newIntent(binding.root.context,
                                            ERModel(
                                                uid = author?.uid,
                                                profilePicture = author?.profilePicture,
                                                name = author?.name
                                            )
                                        )
                                        binding.root.context.startActivity(intent)
                                    }
                                })

                                customDialog.show()
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


            boardCommentTvContent.text = item.content
            boardCommentTvDate.text = formatTimeOrDate(item.date)

        }

        fun formatTimeOrDate(postTime: Long): String {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            val date1 = calendar.time

            val simpleDateFormat: SimpleDateFormat
            if (Date(postTime) > date1) {
                simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }
            return simpleDateFormat.format(Date(postTime))
        }

    }
}