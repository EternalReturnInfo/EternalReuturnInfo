package com.erionna.eternalreturninfo.ui.activity.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.BoardAddActivityBinding
import com.erionna.eternalreturninfo.model.BoardModel
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.retrofit.FBRef
import java.util.Calendar

class BoardAdd : AppCompatActivity() {

    private lateinit var binding: BoardAddActivityBinding
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BoardAddActivityBinding.inflate(layoutInflater)
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

            if(title.isEmpty()){
                Toast.makeText(this@BoardAdd, "제목을 입력해주세요!", Toast.LENGTH_SHORT).show()
            }else if(content.isEmpty()) {
                Toast.makeText(this@BoardAdd, "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
            }else if(category.isEmpty()){
                Toast.makeText(this@BoardAdd, "옵션을 선택해주세요!", Toast.LENGTH_SHORT).show()
            }
            else if(title.isNotEmpty() && content.isNotEmpty() && category.isNotEmpty()){

                val key = FBRef.postRef.push().key.toString()

                //로그인한 유저 UserModel 정보 가져오기!
                val newBoard = BoardModel(key, category, title, content, BoardSingletone.LoginUser().uid, date, mapOf(), 0)

                FBRef.postRef.child(key).setValue(newBoard).addOnSuccessListener {
                    Toast.makeText(this@BoardAdd, "게시글 추가!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this@BoardAdd, "게시글 추가 실패!" + e.message, Toast.LENGTH_SHORT).show()
                }

                intent.putExtra("board", newBoard)
                setResult(RESULT_OK, intent)

                finish()
            }
        }

    }

}
