package com.erionna.eternalreturninfo.ui.fragment.myprofile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.MyprofileCharacterDialogBinding
import com.erionna.eternalreturninfo.databinding.MyprofileFragmentBinding
import com.erionna.eternalreturninfo.databinding.MyprofileRecordFragmentBinding
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.ui.activity.login.LoginActivity
import com.erionna.eternalreturninfo.ui.activity.login.SignUpActivity.Companion.collection
import com.erionna.eternalreturninfo.ui.activity.main.MainActivity
import com.erionna.eternalreturninfo.ui.adapter.myprofile.MyProfileViewPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MyProfileFragment : Fragment() {
    private val binding get() = _binding!!
    private var _binding: MyprofileFragmentBinding? = null
    private var auth: FirebaseAuth? = null
    var db = Firebase.firestore
    private lateinit var characterBinding: MyprofileCharacterDialogBinding
    private val pickImage = 1111
    val storage = Firebase.storage
    var email: String? = null
    private lateinit var database: DatabaseReference


    companion object {
        fun newInstance() = MyProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyprofileFragmentBinding.inflate(inflater, container, false)
        characterBinding = MyprofileCharacterDialogBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        var uid = auth?.uid.toString()
        Patch(uid)
        setOnClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyProfileViewPagerAdapter(requireActivity())
        binding.myprofileViewpager2.adapter = adapter

        TabLayoutMediator(
            binding.myprofileTabLayout,
            binding.myprofileViewpager2
        ) { tab, position ->
            tab.setText(adapter.getTitle(position))
        }.attach()

        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxScroll = appBarLayout.totalScrollRange
            val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

            if (percentage == 1.0f) {
                binding.myprofileTvUsername.visibility = View.VISIBLE
                binding.myprofileBtnSetting.visibility = View.INVISIBLE
                binding.myprofileTvUsername.text = BoardSingletone.LoginUser().name
            } else if (percentage == 0.0f) {
                binding.myprofileBtnSetting.visibility = View.VISIBLE
                binding.myprofileTvUsername.visibility = View.INVISIBLE
            } else {
                binding.myprofileBtnSetting.visibility = View.INVISIBLE
                binding.myprofileTvUsername.visibility = View.INVISIBLE
            }

        })

        binding.myprofileBtnSetting.setOnClickListener {
            val popup = PopupMenu(binding.root.context, binding.myprofileBtnSetting) // View 변경
            popup.menuInflater.inflate(R.menu.menu_myprofile, popup.menu)
            popup.setOnMenuItemClickListener { menu ->
                when (menu.itemId) {
                    R.id.menu_myprofile_update -> {

                        val dialogView =
                            layoutInflater.inflate(R.layout.myprofile_character_dialog, null)
                        val alertDialog = AlertDialog.Builder(requireActivity())
                            .setView(dialogView)
                            .create()

                        val characterSpinner =
                            dialogView.findViewById<Spinner>(R.id.myprofile_character_sp)
                        val button = dialogView.findViewById<Button>(R.id.myprofile_select_btn)
                        val characterlist = resources.getStringArray(R.array.character)

                        val adapter = ArrayAdapter<String>(
                            requireContext(),
                            R.layout.signup_spinner,
                            R.id.spinner_tv,
                            characterlist
                        )


                        var selectCharacter = characterlist[0]
                        characterSpinner.adapter = adapter

                        characterSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    selectCharacter = characterlist[position]
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                }
                            }
                        // 프로필 변경버튼
                        button.setOnClickListener {
                            auth?.uid?.let { it1 ->
                                db.collection(collection)
                                    .document(it1)
                                    .update(
                                        mapOf(
                                            "character" to selectCharacter
                                        )
                                    ).addOnCompleteListener {
                                        var uid = auth?.uid.toString()
                                        Patch(uid)
                                    }
                            }
                            auth?.uid?.let { it1 ->
                                database.child("user").child(it1).updateChildren(
                                    mapOf(
                                        "character" to selectCharacter
                                    )
                                )
                            }
                            alertDialog.dismiss()
                            Handler(Looper.getMainLooper()).postDelayed({
                                var uid = auth?.uid.toString()
                                Patch(uid)
                            }, 2000)
                        }
                        alertDialog.show()
                    }

                    R.id.menu_logout -> {
                        Firebase.auth.signOut()
                        var intent = Intent(activity, LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }

                    R.id.menu_withdraw -> {
                        val deleteDialogView = layoutInflater.inflate(R.layout.delete_dialog, null)
                        val deleteDialog = AlertDialog.Builder(requireActivity())
                            .setView(deleteDialogView)
                            .create()

                        var yesBtn = deleteDialogView.findViewById<Button>(R.id.delete_yes_btn)
                        var noBtn = deleteDialogView.findViewById<Button>(R.id.delete_no_btn)

                        yesBtn.setOnClickListener {
                            email = auth?.currentUser?.email

                            // storage 참조
                            val storageRef = storage.getReference("image")
                            // storage에서 삭제 할 파일명
                            val fileName = email.toString()
                            val mountainsRef = storageRef.child("${fileName}.jpg")
                            mountainsRef.delete()
                            auth?.uid?.let { it1 ->
                                database.child("user").child(it1).removeValue()
                            }
                            auth?.uid?.let { it1 ->
                                db.collection(collection)
                                    .document(it1)
                                    .delete()
                            }
                            val user = Firebase.auth.currentUser!!
                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("계정삭제", "User account deleted.")
                                    }
                                }
                            deleteDialog.dismiss()
                            var intent = Intent(activity, LoginActivity::class.java)
                            (context as MainActivity).finish()
                            startActivity(intent)
                        }
                        noBtn.setOnClickListener {
                            deleteDialog.dismiss()
                        }
                        deleteDialog.show()
                    }

                    R.id.menu_pwchange -> {
                        var emailCheck = auth?.currentUser?.providerData?.get(0)?.providerId
                        Log.d("로그인 방식 체크", emailCheck!!)
                        if (emailCheck == "password") {
                            val pwChangeDialogView =
                                layoutInflater.inflate(R.layout.myprofile_pwchange_dialog, null)
                            val pwChangeDialog = AlertDialog.Builder(requireActivity())
                                .setView(pwChangeDialogView)
                                .create()

                            val changeBtn =
                                pwChangeDialogView.findViewById<Button>(R.id.myprofile_pwChange_btn)
                            val firstPw =
                                pwChangeDialogView.findViewById<EditText>(R.id.myprofile_pwChange_et)
                            val secondPw =
                                pwChangeDialogView.findViewById<EditText>(R.id.myprofile_pwChange2_et)

                            changeBtn.setOnClickListener {

                                val pw = firstPw.text.toString()
                                val pwCheck = secondPw.text.toString()
                                if (pw.isNotEmpty() && pwCheck.isNotEmpty()) {
                                    if (pw == pwCheck) {
                                        PwChange(pw, pwCheck)
                                        pwChangeDialog.dismiss()
                                    } else {
                                        Toast.makeText(
                                            requireActivity(),
                                            "동일한 비밀번호를 입력하세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        requireActivity(),
                                        "비밀번호를 입력하세요",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                            }
                            pwChangeDialog.show()
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                "SNS 로그인은 비밀번호를 변동 할 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                false
            }
            popup.show()
        }

    }

    private fun setOnClickListener() {
        val profileBtn = binding.myprofileProfileImg
        profileBtn.setOnClickListener {
            selectProfile()
        }
    }

    // 마이페이지 생성
    fun Patch(uid: String) {
        val docRef = db.collection(collection).document("$uid")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var uri = Uri.parse(document["profile"].toString())
                    binding.myprofileEmailTv.text = document["email"].toString()
                    binding.myprofileNicknameTv.text = document["nickName"].toString()
                    Glide.with(this).load(uri).into(binding.myprofileProfileImg);
                    email = document["email"].toString()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        var uid = auth?.uid.toString()
        Patch(uid)
    }

    fun selectProfile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        startActivityForResult(intent, pickImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage) {
            val uri: Uri? = data?.data
            if (uri != null) {
                email?.let { upload(uri, it) }
            }
        }
    }

    fun upload(
        uri: Uri,
        email: String,
    ) {
        val storageRef = storage.reference
        val fileName = email + "_profile"
        val riversRef = storageRef.child("/$fileName")

        riversRef.putFile(uri)
            .addOnProgressListener { taskSnapshot ->
                riversRef.downloadUrl.addOnSuccessListener { uri ->
                    auth?.uid?.let {
                        db.collection(collection)
                            .document(it)
                            .update("profile", uri.toString()).addOnCompleteListener {
                                var uid = auth?.uid.toString()
                                Patch(uid)
                            }
                    }
                    auth?.uid?.let {
                        database.child("user").child(it).updateChildren(
                            mapOf(
                                "profilePicture" to uri.toString()
                            )
                        )
                    }
                }
            }
    }

    fun PwChange(pw: String, pwCheck: String) {
        val user: FirebaseUser? = auth?.currentUser
        user?.updatePassword(pw)
        auth?.uid?.let {
            db.collection(collection).document(it).update(
                mapOf(
                    "pw" to pw
                )
            )
        }
        auth?.uid?.let {
            database.child("user").child(it).updateChildren(
                mapOf(
                    "password" to pw
                )
            )
        }
    }
}