package com.erionna.eternalreturninfo.retrofit

import android.util.Log
import com.erionna.eternalreturninfo.model.ERModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

object BoardSingletone {

    private var loginUser: ERModel = ERModel()
    private var seasonID: String = ""

    fun LoginUser(): ERModel {
        return loginUser
    }

    fun seasonID(): String{
        return seasonID
    }

    fun manager(): ERModel{
        return ERModel(uid="j2JTWMpZdEUWxao4mYoYC3Acheg1")
    }

    fun Login(){

        val uid = FirebaseAuth.getInstance().uid

        if (uid != null) {
            FBRef.userRef.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        val user = snapshot.getValue<ERModel>()
                        if (user != null) {
                            loginUser = user
                            Log.d("user.name",user.name.toString())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        FBRef.seasonRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val season = snapshot.getValue<String>()
                    if (season != null) {
                        seasonID = season
                        Log.d("seasonID", season)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


}