package com.irionna.eternalreturninfo.presentation.adapter.findduo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.irionna.eternalreturninfo.R
import com.irionna.eternalreturninfo.databinding.FindDuoListItemBinding
import com.irionna.eternalreturninfo.data.model.ERModel
import com.irionna.eternalreturninfo.retrofit.RetrofitInstance
import com.irionna.eternalreturninfo.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FindduoAdapter(
    private val context: Context,
    private val onClickUser: (Int, ERModel) -> Unit,
    private val onLongClickUser: (Int, ERModel) -> Unit
) : RecyclerView.Adapter<FindduoAdapter.ItemViewHolder>(
) {

    var items = ArrayList<ERModel>()
    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val binding =
            FindDuoListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemViewHolder(binding, onClickUser, onLongClickUser)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]

        holder.server.text = currentItem.server
        holder.name.text = currentItem.name
        holder.gender.text = currentItem.gender
        holder.tier.text = currentItem.tier

        ImgPatch().setCharacterImage(currentItem.most, holder.binding.fdliMost)
        ImgPatch2().setTierImage(currentItem.tier, holder.binding.fdliTierimage)
        StatePacth().setState(currentItem.name,holder.binding.fdliWinrate,holder.binding.fdliAvgrank)

        holder.binding.fdliContainer.setOnClickListener {
            onClickUser(
                position,
                currentItem
            )
        }

        holder.binding.fdliContainer.setOnLongClickListener {
            onLongClickUser(position, currentItem)
            true
        }
    }

    inner class ItemViewHolder(
        var binding: FindDuoListItemBinding,
        private val onClickUser: (Int, ERModel) -> Unit,
        private val onLongClickUser: (Int, ERModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root){

        var server = binding.fdliServer
        var name = binding.fdliName
        var gender = binding.fdliGender
        var tier = binding.fdliTier
        var most = binding.fdliMost
        var winrate = binding.fdliWinrate
        var avgrank = binding.fdliAvgrank
        var tierimage = binding.fdliTierimage

    }

    class ImgPatch {
        fun setCharacterImage(character: String?, imageView: ImageView) {
            val resources = imageView.context.resources
            val array = resources.getStringArray(R.array.characterName)
            val imageArray = arrayOf(
                R.drawable.ic_xiuk,
                R.drawable.character_1,
                R.drawable.character_2,
                R.drawable.character_3,
                R.drawable.character_4,
                R.drawable.character_5,
                R.drawable.character_6,
                R.drawable.character_7,
                R.drawable.character_8,
                R.drawable.character_9,
                R.drawable.character_10,
                R.drawable.character_11,
                R.drawable.character_12,
                R.drawable.character_13,
                R.drawable.character_14,
                R.drawable.character_15,
                R.drawable.character_16,
                R.drawable.character_17,
                R.drawable.character_18,
                R.drawable.character_19,
                R.drawable.character_20,
                R.drawable.character_21,
                R.drawable.character_22,
                R.drawable.character_23,
                R.drawable.character_24,
                R.drawable.character_25,
                R.drawable.character_26,
                R.drawable.character_27,
                R.drawable.character_28,
                R.drawable.character_29,
                R.drawable.character_30,
                R.drawable.character_31,
                R.drawable.character_32,
                R.drawable.character_33,
                R.drawable.character_34,
                R.drawable.character_35,
                R.drawable.character_36,
                R.drawable.character_37,
                R.drawable.character_38,
                R.drawable.character_39,
                R.drawable.character_40,
                R.drawable.character_41,
                R.drawable.character_42,
                R.drawable.character_43,
                R.drawable.character_44,
                R.drawable.character_45,
                R.drawable.character_46,
                R.drawable.character_47,
                R.drawable.character_48,
                R.drawable.character_49,
                R.drawable.character_50,
                R.drawable.character_51,
                R.drawable.character_52,
                R.drawable.character_53,
                R.drawable.character_54,
                R.drawable.character_55,
                R.drawable.character_56,
                R.drawable.character_57,
                R.drawable.character_58,
                R.drawable.character_59,
                R.drawable.character_60,
                R.drawable.character_61,
                R.drawable.character_62,
                R.drawable.character_63,
                R.drawable.character_64,
                R.drawable.character_65,
                R.drawable.character_66,
                R.drawable.character_67,
                R.drawable.character_68
            )

            val index = array.indexOf(character)
            if (index != -1 && index < imageArray.size) {
                imageView.setImageResource(imageArray[index])
            }
        }
    }

    class ImgPatch2 {
        fun setTierImage(tier: String?, imageView: ImageView) {
            val resources = imageView.context.resources
            val array = resources.getStringArray(R.array.tier)
            val imageArray = arrayOf(
                R.drawable.ic_xiuk,
                R.drawable.ic_unrank,
                R.drawable.ic_iron,
                R.drawable.ic_bronze,
                R.drawable.ic_silver,
                R.drawable.ic_gold,
                R.drawable.ic_platinum,
                R.drawable.ic_diamond,
                R.drawable.ic_mithril,
                R.drawable.ic_titan,
                R.drawable.ic_immortal
            )
            val index = array.indexOf(tier)
            if (index != -1 && index < imageArray.size) {
                imageView.setImageResource(imageArray[index])
            }
        }
    }

    class StatePacth {

        fun setState(name: String?, textView: TextView, textView2: TextView) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val nickname = name

                    val userID_call = RetrofitInstance.searchUserIDApi.getUserByNickname(
                        Constants.MAIN_APIKEY,
                        nickname
                    )
                    val userID_response = userID_call.execute()

                    if (userID_response.isSuccessful) {
                        val gameResponse = userID_response.body()
                        val userNum = gameResponse?.user?.userNum.toString()
                        val seasonId = "19"

                        val userstate_call = RetrofitInstance.searchUserStateApi.getUserStats(
                            Constants.MAIN_APIKEY, userNum, seasonId
                        )
                        val userstate_response = userstate_call.execute()

                        if (userstate_response.isSuccessful) {
                            val userStateResponse = userstate_response.body()

                            withContext(Dispatchers.Main) {

                                val user = userStateResponse?.userStats?.get(0)

                                textView.text = (user?.top1?.times(100) ?: 0).toString() + "%"
                                textView2.text = "#" + (user?.averageRank ?: 0).toString()
                            }

                        } else {
                            Log.d("userStateResponse", "${userstate_response}")
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}
