package com.mindeggs.messenger.views

import com.mindeggs.messenger.R
import com.mindeggs.messenger.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatFromItem(val text: String, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatLog_from_messageContent.text = text

        val profileImage_chatlog = viewHolder.itemView.chatLog_from_chatIcon

        //load image
        Picasso.get().load(user.profileImageUrl).into(profileImage_chatlog)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatToLog_to_messageContent.text = text

        val profileImage_chatlog = viewHolder.itemView.chatToLog_to_chatIcon
        //load our image
        Picasso.get().load(user.profileImageUrl).into(profileImage_chatlog)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}