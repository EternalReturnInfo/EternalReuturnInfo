package com.erionna.eternalreturninfo.presentation.adapter.myprofile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.MyprofileItemRecyclerviewBinding
import com.erionna.eternalreturninfo.retrofit.CharacterStats

class MyprofileListAdapter(val dataList:MutableList<CharacterStats>, val characterList:Array<String>) :RecyclerView.Adapter<MyprofileListAdapter.Holder>()  {

    interface ItemClick{
        fun onClick(view: View, position: Int)
    }
    var itemClick: ItemClick? = null

    inner class Holder(val binding:MyprofileItemRecyclerviewBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.myprofilelistCharacterImg
        val usages = binding.myprofileUsagesTv
        val character = binding.myprofilelistMycharacterTv
        val top3 = binding.myprofilelistTvTop3
        val avRank = binding.myprofilelistTvAverageRank
        val maxKill = binding.myprofilelistTvMaxKill

    }
    // 사진, 이름, 주소, 타이틀, 내용
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = MyprofileItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }


    override fun onBindViewHolder(holder: Holder, position: Int) {
        var characterCode = dataList[position].characterCode
        when(characterCode){
            1 -> holder.image.setImageResource(R.drawable.character_1)
            2 -> holder.image.setImageResource(R.drawable.character_2)
            3 -> holder.image.setImageResource(R.drawable.character_3)
            4 -> holder.image.setImageResource(R.drawable.character_4)
            5 -> holder.image.setImageResource(R.drawable.character_5)
            6 -> holder.image.setImageResource(R.drawable.character_6)
            7 -> holder.image.setImageResource(R.drawable.character_7)
            8 -> holder.image.setImageResource(R.drawable.character_8)
            9 -> holder.image.setImageResource(R.drawable.character_9)
            10 -> holder.image.setImageResource(R.drawable.character_10)
            11 -> holder.image.setImageResource(R.drawable.character_11)
            12 -> holder.image.setImageResource(R.drawable.character_12)
            13 -> holder.image.setImageResource(R.drawable.character_13)
            14 -> holder.image.setImageResource(R.drawable.character_14)
            15 -> holder.image.setImageResource(R.drawable.character_15)
            16 -> holder.image.setImageResource(R.drawable.character_16)
            17 -> holder.image.setImageResource(R.drawable.character_17)
            18 -> holder.image.setImageResource(R.drawable.character_18)
            19 -> holder.image.setImageResource(R.drawable.character_19)
            20 -> holder.image.setImageResource(R.drawable.character_20)
            21 -> holder.image.setImageResource(R.drawable.character_21)
            22 -> holder.image.setImageResource(R.drawable.character_22)
            23 -> holder.image.setImageResource(R.drawable.character_23)
            24 -> holder.image.setImageResource(R.drawable.character_24)
            25 -> holder.image.setImageResource(R.drawable.character_25)
            26 -> holder.image.setImageResource(R.drawable.character_26)
            27 -> holder.image.setImageResource(R.drawable.character_27)
            28 -> holder.image.setImageResource(R.drawable.character_28)
            29 -> holder.image.setImageResource(R.drawable.character_29)
            30 -> holder.image.setImageResource(R.drawable.character_30)
            31 -> holder.image.setImageResource(R.drawable.character_31)
            32 -> holder.image.setImageResource(R.drawable.character_32)
            33 -> holder.image.setImageResource(R.drawable.character_33)
            34 -> holder.image.setImageResource(R.drawable.character_34)
            35 -> holder.image.setImageResource(R.drawable.character_35)
            36 -> holder.image.setImageResource(R.drawable.character_36)
            37 -> holder.image.setImageResource(R.drawable.character_37)
            38 -> holder.image.setImageResource(R.drawable.character_38)
            39 -> holder.image.setImageResource(R.drawable.character_39)
            40 -> holder.image.setImageResource(R.drawable.character_40)
            41 -> holder.image.setImageResource(R.drawable.character_41)
            42 -> holder.image.setImageResource(R.drawable.character_42)
            43 -> holder.image.setImageResource(R.drawable.character_43)
            44 -> holder.image.setImageResource(R.drawable.character_44)
            45 -> holder.image.setImageResource(R.drawable.character_45)
            46 -> holder.image.setImageResource(R.drawable.character_46)
            47 -> holder.image.setImageResource(R.drawable.character_47)
            48 -> holder.image.setImageResource(R.drawable.character_48)
            49 -> holder.image.setImageResource(R.drawable.character_49)
            50 -> holder.image.setImageResource(R.drawable.character_50)
            51 -> holder.image.setImageResource(R.drawable.character_51)
            52 -> holder.image.setImageResource(R.drawable.character_52)
            53 -> holder.image.setImageResource(R.drawable.character_53)
            54 -> holder.image.setImageResource(R.drawable.character_54)
            55 -> holder.image.setImageResource(R.drawable.character_55)
            56 -> holder.image.setImageResource(R.drawable.character_56)
            57 -> holder.image.setImageResource(R.drawable.character_57)
            58 -> holder.image.setImageResource(R.drawable.character_58)
            59 -> holder.image.setImageResource(R.drawable.character_59)
            60 -> holder.image.setImageResource(R.drawable.character_60)
            61 -> holder.image.setImageResource(R.drawable.character_61)
            62 -> holder.image.setImageResource(R.drawable.character_62)
            63 -> holder.image.setImageResource(R.drawable.character_63)
            64 -> holder.image.setImageResource(R.drawable.character_64)
            65 -> holder.image.setImageResource(R.drawable.character_65)
            66 -> holder.image.setImageResource(R.drawable.character_66)
            67 -> holder.image.setImageResource(R.drawable.character_67)
            68 -> holder.image.setImageResource(R.drawable.character_68)
        }
        holder.usages.text = dataList[position].usages.toString()+"게임"
        holder.character.text = characterList[dataList[position].characterCode]
        holder.top3.text = dataList[position].top3.toString()
        holder.avRank.text = "#"+dataList[position].averageRank.toString()
        holder.maxKill.text = dataList[position].maxKillings.toString()+"킬"

    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}