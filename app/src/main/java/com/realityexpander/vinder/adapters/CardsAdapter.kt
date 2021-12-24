package com.realityexpander.vinder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.realityexpander.vinder.R
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.isNotNullAndNotEmpty
import com.realityexpander.vinder.utils.loadUrl

class CardsAdapter(context: Context?, resourceId: Int, users: List<User>):
    ArrayAdapter<User>(context!!, resourceId, users)
{

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val user = getItem(position)
        val finalView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)

        val userInfoLayout = finalView.findViewById<LinearLayout>(R.id.cardUserInfoLayout)
        val profileImageIv = finalView.findViewById<ImageView>(R.id.cardProfileImageIv)
        val nameInfoTv = finalView.findViewById<TextView>(R.id.cardNameTV)

        val nameInfoString = "${user?.username}" +
                if(user?.age.isNotNullAndNotEmpty()) ", ${user?.age}" else ""
        nameInfoTv.text = nameInfoString
        profileImageIv.loadUrl(user?.profileImageUrl)

        userInfoLayout.setOnClickListener {
//            finalView.context.startActivity(UserInfoActivity.newIntent(finalView.context, user.uid))
        }

        return finalView
    }
}