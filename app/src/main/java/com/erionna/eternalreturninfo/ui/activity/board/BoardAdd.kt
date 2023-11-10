package com.erionna.eternalreturninfo.ui.activity.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.BoardAddActivity2Binding
import com.erionna.eternalreturninfo.model.BoardModel
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.retrofit.FBRef
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.Calendar

class BoardAdd : AppCompatActivity() {

    private lateinit var binding: BoardAddActivity2Binding
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BoardAddActivity2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() = with(binding) {

        boardAddIbBack.setOnClickListener {
            finish()
        }

        if(BoardSingletone.LoginUser().uid != BoardSingletone.manager().uid){
            val spinnerItems = resources.getStringArray(R.array.board_user_option)
            boardAddSpinner.setItems(spinnerItems.toList())
        }else{
            val spinnerItems = resources.getStringArray(R.array.board_option)
            boardAddSpinner.setItems(spinnerItems.toList())
        }

        boardAddSpinner.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newText ->

            when(newText){
                "공지" -> category = "공지"
                "일상" -> category = "일상"
                "질문" -> category = "질문"
                "공략" -> category = "공략"
            }
        }

        boardAddBtnFinish.setOnClickListener {

            // 수정 : 로그인한 사용자 닉네임 가져오기!
            val title = boardAddEtTitle.text.toString()
            val content = boardAddEtContent.text.toString()
            val date = Calendar.getInstance().timeInMillis
            val category = category

            if (title.isEmpty()) {

                MotionToast.darkColorToast(
                    this@BoardAdd, "CHECK", "제목을 입력해주세요!",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    font = null
                )
            } else if (content.isEmpty()) {

                MotionToast.darkColorToast(
                    this@BoardAdd, "CHECK", "내용을 입력해주세요!",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    font = null
                )
            } else if (category.isEmpty()) {

                MotionToast.darkColorToast(
                    this@BoardAdd, "CHECK", "옵션을 선택해주세요!",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    font = null
                )
            } else if (title.isNotEmpty() && content.isNotEmpty() && category.isNotEmpty()) {

                val key = FBRef.postRef.push().key.toString()

                // 로그인한 유저 UserModel 정보 가져오기!
                val newBoard = BoardModel(key, category, title, content, BoardSingletone.LoginUser().uid, date, mapOf(), 0)

                FBRef.postRef.child(key).setValue(newBoard).addOnSuccessListener {
                    MotionToast.darkColorToast(
                        this@BoardAdd, "", "게시글 추가!",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        font = null
                    )
                }.addOnFailureListener { e ->

                    MotionToast.darkColorToast(
                        this@BoardAdd, "", "게시글 추가 실패!" + e.message,
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        font = null
                    )
                }

                intent.putExtra("board", newBoard)
                setResult(RESULT_OK, intent)

                finish()
            }
        }
    }
}
