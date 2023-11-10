package com.erionna.eternalreturninfo.ui.fragment.findduo

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.FindDuoFragmentBinding
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.ui.activity.chat.ChatActivity
import com.erionna.eternalreturninfo.ui.activity.main.MainActivity
import com.erionna.eternalreturninfo.ui.activity.board.BoardDialog
import com.erionna.eternalreturninfo.ui.activity.board.DialogListener
import com.erionna.eternalreturninfo.ui.adapter.findduo.FindduoAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class FindDuoFragment : Fragment() {
    companion object {
        fun newInstance() = FindDuoFragment()

    }

    private var _binding: FindDuoFragmentBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var mUID = ""
    private lateinit var findduoPopupWindow: FindduoPopupWindow


    private val adapter: FindduoAdapter by lazy {
        FindduoAdapter(
            requireContext(),
            onClickUser = { position, item ->
                if (item.uid != mAuth.uid) {
                    val customDialog = BoardDialog(requireContext(), item.name ?: "", object :
                        DialogListener {
                        override fun onOKButtonClicked() {
                            startActivity(
                                ChatActivity.newIntent(
                                    requireContext(),
                                    item
                                )
                            )
                        }
                    })
                    customDialog.show()
                } else {
                    val mainActivity = activity as MainActivity
                    mainActivity.binding.tabLayout.getTabAt(4)?.select()
                }
            },
            onLongClickUser = { position, item ->
                if (mAuth.currentUser?.uid != item.uid) {
                    Toast.makeText(requireContext(), "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    deleteSpecificFieldsFromDatabase(item)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FindDuoFragmentBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        mUID = mAuth.currentUser?.uid ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        binding.findduoRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.findduoRecyclerview.adapter = adapter
        initView()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun initView() = with(binding) {
        findduoPopupWindow = FindduoPopupWindow(requireContext())
        findduoRegisterBtn.setOnClickListener { findduoPopupWindow.showPopup(binding.root) }
        adapter.notifyDataSetChanged()
        loadAllUserDataFromFirebase()
    }

    private fun loadAllUserDataFromFirebase() {
        val databasePath = "user"

        mDbRef.child(databasePath).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filteredUsersList = ArrayList<ERModel>()

                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(ERModel::class.java)

                            if (user != null &&
                                !user.server.isNullOrEmpty() &&
                                !user.name.isNullOrEmpty() &&
                                !user.gender.isNullOrEmpty() &&
                                !user.tier.isNullOrEmpty()
                            ) {
                                filteredUsersList.add(user)
                            }
                        }

                        filteredUsersList.reverse()

                        adapter.items.clear()
                        adapter.items.addAll(filteredUsersList)
                        adapter.notifyDataSetChanged()

                        val filteredUserCount = filteredUsersList.size
                        binding.findduoTotalNumber.text =
                            filteredUserCount.toString()
                    } else {
                        Log.d(TAG, "No user data found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Data retrieval failed: $error")
                }
            })
    }

    private fun deleteSpecificFieldsFromDatabase(item: ERModel) {
        val dialogView = layoutInflater.inflate(R.layout.findduo_remove_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()

        val btnYes = dialogView.findViewById<Button>(R.id.findduo_remove_btn_yes)
        val btnNo = dialogView.findViewById<Button>(R.id.findduo_remove_btn_no)

        btnYes.setOnClickListener {
            deleteItem(item)
            alertDialog.dismiss()
        }

        btnNo.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun deleteItem(item: ERModel) {
        val userDatabaseRef = mDbRef.child("user")

        userDatabaseRef.orderByChild("name").equalTo(item.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val updates = HashMap<String, Any>()
                        updates["server"] = ""
                        updates["gender"] = ""
                        updates["tier"] = ""
                        updates["most"] = ""

                        childSnapshot.ref.updateChildren(updates)
                    }

                    loadAllUserDataFromFirebase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error deleting specific fields: $error")
                }
            })
    }
}