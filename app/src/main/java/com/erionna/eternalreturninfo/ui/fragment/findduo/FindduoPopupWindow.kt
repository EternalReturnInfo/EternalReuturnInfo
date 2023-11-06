package com.erionna.eternalreturninfo.ui.fragment.findduo

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import android.widget.Toast
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.FindduoPopupWindowActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FindduoPopupWindow(private val context: Context) {
    private var popupWindow: PopupWindow? = null
    private var _binding: FindduoPopupWindowActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var mUID = ""

    //파이어스터오랑 연동하기위한 코드
    private val firestore = FirebaseFirestore.getInstance()

    fun showPopup(anchorView: View) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _binding = FindduoPopupWindowActivityBinding.inflate(inflater, null, false)
        mAuth = Firebase.auth
        mDbRef = Firebase.database.reference

        // PopupWindow
        popupWindow = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow?.apply {
            elevation = 10.0f // 고도
            val alphaColor = Color.argb(80, 0, 0, 0) // 여기서 80은 투명도를 나타냄 (0은 완전 투명, 255는 완전 불투명)
            setBackgroundDrawable(ColorDrawable(alphaColor))
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_InputMethod // 애니메이션
        }

        //서버 스피너

        val serverList = context.resources.getStringArray(R.array.server)
        val serverAdpater = ArrayAdapter<String>(
            context,
            R.layout.findduo_spinner,
            R.id.findduo_spinner_tv,
            serverList
        )
        var selectServer: String? = "선택"

        binding.findduoServerBtn.adapter = serverAdpater

        binding.findduoServerBtn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectServer = serverList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectServer = "선택"
            }
        }

        //티어 스피너

        val tierList = context.resources.getStringArray(R.array.tier)
        val tierAdapter =
            ArrayAdapter<String>(context, R.layout.findduo_spinner, R.id.findduo_spinner_tv, tierList)
        var selectTier: String? = "선택"

        binding.findduoTierBtn.adapter = tierAdapter

        binding.findduoTierBtn.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectTier = tierList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectServer = "선택"
                }
            }

        //성별 스피너

        val genderList = context.resources.getStringArray(R.array.gender)
        val genderAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context,
            R.layout.findduo_spinner,
            R.id.findduo_spinner_tv,
            genderList
        )
        var selectGender : String? = "선택"

        binding.findduoGenderBtn.adapter = genderAdapter

        binding.findduoGenderBtn.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectGender = genderList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectServer = "선택"
                }
            }

        //선호 실험체 스피너

        val mostList = context.resources.getStringArray(R.array.characterName)
        val mostAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context,
            R.layout.findduo_spinner,
            R.id.findduo_spinner_tv,
            mostList
        )
        var selectMost: String? = "선택"

        binding.findduoMostBtn.adapter = mostAdapter

        binding.findduoMostBtn.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectMost = mostList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectServer = "선택"

                }

            }

        binding.findduoYesBtn.setOnClickListener {
            if (selectServer != "선택" && selectTier != "선택" && selectGender != "선택" && selectMost != "선택") {
                updateUserInFirebase(selectServer, "server")
                updateUserInFirebase(selectTier, "tier")
                updateUserInFirebase(selectGender, "gender")
                updateUserInFirebase(selectMost, "most")
                addTimestampToFirebase()
              //  updateMostInFirestore(selectMost)
                dismissPopup()
            } else {
                Toast.makeText(context, "모든 옵션을 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // PopupWindow 표시
        popupWindow?.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }

    private fun addTimestampToFirebase() {
        val timestamp = System.currentTimeMillis() // 현재 시간을 밀리초로 얻기

        updateUserInFirebase(timestamp.toString(), "timestamp")
    }

    fun dismissPopup() {
        popupWindow?.dismiss()
    }

    private fun updateUserInFirebase(finalSelection: String?, s: String) {

        // 사용자의 Firebase UID
        val userId = mAuth.currentUser?.uid

        // 사용자의 Firebase UID가 없으면 함수를 종료
        if (userId == null) {
            return
        }

        // 업데이트할 데이터를 맵으로 구성
        val updateData = mapOf(
            s to finalSelection
        )

        // Firebase Realtime Database 경로
        val databasePath = "user/$userId"

        // 데이터베이스에 업데이트할 내용을 설정
        mDbRef.child(databasePath).updateChildren(updateData)
    }

    private fun updateMostInFirestore(finalSelection: String?) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val userRef = firestore.collection("EternalReturnInfo").document(userId)

            // Firestore의 'most' 필드 업데이트
            userRef.update("character", finalSelection)
        }
    }
}