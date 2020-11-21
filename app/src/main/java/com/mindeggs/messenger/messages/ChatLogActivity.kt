package com.mindeggs.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mindeggs.messenger.R
import com.mindeggs.messenger.model.ChatMessage
import com.mindeggs.messenger.model.User
import com.mindeggs.messenger.views.ChatFromItem
import com.mindeggs.messenger.views.ChatToItem
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

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

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        //Adding Username as the Title
        if (toUser != null) {
            supportActionBar?.title = toUser?.username
        }

        listenForMessages()

        send_button_chat_log.setOnClickListener{

            val message = edittext_chat_log.text.toString()
            Log.d(TAG, message)

            if(message != "") {
                performSendMessage()
            }
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
            override fun onCancelled(snapshot: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {

        //Create a node in the database
        val message = edittext_chat_log.text.toString()

        //Getting the person the message is from
        val fromId = FirebaseAuth.getInstance().uid

        //Getting the person the message is to
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.uid

        if(fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        //Reference for the user we are sending the message to
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

        val latestMessageRefFrom = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        val latestMessageRefTo = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        latestMessageRefFrom.setValue(chatMessage)

        latestMessageRefTo.setValue(chatMessage)
    }
}