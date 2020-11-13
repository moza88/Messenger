package com.mindeggs.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mindeggs.messenger.messages.LatestMessagesActivity
import com.mindeggs.messenger.messages.NewMessageActivity
import com.mindeggs.messenger.model.ChatMessage
import com.mindeggs.messenger.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_chat_log.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLogActivity"
    }

    var toUser: User? = null

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        //Adding the username as the page title
        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        if (toUser != null) {
            supportActionBar?.title = toUser?.username
        }

        listenForMessages()

        send_button_chat_log.setOnClickListener{

            val message = edittext_chat_log.text.toString()

            Log.d(TAG, message)
            performSendMessage()

        }

    }

    private fun listenForMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.message)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {

                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.message, currentUser))

                    } else {
                        adapter.add(ChatToItem(chatMessage.message, toUser!!))
                    }
                }

            }
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {

        //Send the message to firebase and present message

        //Create a node in the database
        val message = edittext_chat_log.text.toString()

        //Getting the person the message is from
        val fromId = FirebaseAuth.getInstance().uid

        //Getting the person the message is to
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.uid

        if(fromId == null || toId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val timestamp = System.currentTimeMillis()/1000

        val chatMessage = ChatMessage(reference.key!!, message, fromId, toId, timestamp)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")

                //Clear out the text from the text box after the message is sent
                edittext_chat_log.text.clear()

                //Automatically scroll to the last message
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            .addOnFailureListener{
                Log.e(TAG, it.toString())
            }
        toReference.setValue(chatMessage)
    }
}


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