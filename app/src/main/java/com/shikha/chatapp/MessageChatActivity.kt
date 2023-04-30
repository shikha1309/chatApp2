package com.shikha.chatapp

import   android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.shikha.chatapp.AdapterClasses.ChatsAdapter
import com.shikha.chatapp.ModelClasses.Chat
import com.shikha.chatapp.ModelClasses.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*

class   MessageChatActivity : AppCompatActivity() {
    var useridVisit:String=" "
    var firebaseUser :FirebaseUser?=null
    var chatsAdapter : ChatsAdapter? = null
    var mChatList :List<Chat>?= null
    lateinit var recycler_view_chats :RecyclerView
    var reference: DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)
        intent =intent
        useridVisit= intent.getStringExtra("visit id")!!   //Receiver id by using the intent


        firebaseUser=FirebaseAuth.getInstance().currentUser  // Sender Id
        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var  linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd= true
        recycler_view_chats.layoutManager  = linearLayoutManager




         // Retrieving the User name  and Image in message chat activity
                 val reference= FirebaseDatabase.getInstance().reference
             .child("Users").child(useridVisit)
         reference.addValueEventListener(object: ValueEventListener{

             override fun onDataChange(p0: DataSnapshot) {
                 val  user : Users?= p0.getValue(Users::class.java)

                 if (user != null) {
                     username_messageChat.text= user.getUserName()
                 }
                 if (user != null) {
                     Picasso.get().load(user.getProfile()).into(profile_image_message_chat)
                 }


                 // For retrieve the messages to the message chat activity
             retrieveMessages(firebaseUser!!.uid ,useridVisit , user?.getProfile())

             }

             override fun onCancelled(p0: DatabaseError) {

             }

         })



        send_message_btn.setOnClickListener {

            val message = text_message.text.toString()
            if (message == "") {
                Toast.makeText(
                    this@MessageChatActivity,
                    " Please write a message, first... ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendMessageToUser( firebaseUser!!.uid, useridVisit ,message)

            }
            text_message.setText(" ")
          }


        attach_image_file.setOnClickListener {
            val intent = Intent()
            intent.action =Intent.ACTION_GET_CONTENT
            intent.type ="image/*"
            startActivityForResult( Intent.createChooser( intent,"Pick Image") ,438  )


        }
    }



    @SuppressLint("SuspiciousIndentation")
    private fun sendMessageToUser(senderId: String, receiverId: String, message: String)
    {
        val reference  = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

       // All these are the sub child for storing the messages
        val messageHashMap=HashMap<String,Any?>()
        messageHashMap["sender"]= senderId
        messageHashMap["message"]= message
        messageHashMap["receiver"]= receiverId
        messageHashMap["isseen"]= false
        messageHashMap["url"]= ""
        messageHashMap["messageId"]= messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener {  task->  // if the task is successful
                if (task.isSuccessful)
                {
                 val chatsListSenderReference = FirebaseDatabase.getInstance()
                     .reference
                     .child("ChatList")
                     .child(firebaseUser!!.uid)  // storing the sender Id
                     .child(useridVisit)     // storing the Receiver Id

                    chatsListSenderReference.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            // if user is not exist before we will add user to chatlist
                            if (!p0.exists()) {
                         chatsListSenderReference.child("id") .setValue(useridVisit)

                            }
                            val chatsListReceiverReference = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(useridVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverReference.child("id").setValue(firebaseUser!!.uid )
                        }
                        override fun onCancelled(p0: DatabaseError) {

                        }

                    })




                    // Implementing the Push Notifications
                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid )
                }

            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data!=null && data!!.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is Sending....")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }

                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()


                    val messageHashMap=HashMap<String,Any?>()
                    messageHashMap["sender"]= firebaseUser!!.uid
                    messageHashMap["message"]= "send you an image"
                    messageHashMap["receiver"]= useridVisit
                    messageHashMap["isseen"]= false
                    messageHashMap["url"]= url
                    messageHashMap["messageId"]= messageId
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                    progressBar.dismiss()


                }

            }

        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?)
     {
         mChatList = ArrayList()
         val reference =FirebaseDatabase.getInstance().reference.child("Chats")
         reference.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(p0: DataSnapshot)
             {
                 (mChatList as ArrayList<Chat>).clear()
                 for ( snapshot in p0.children )
                 {
                      val chat =snapshot.getValue(Chat::class.java)
                     // condition is these message belongs to sender and receiver only not anybody Else
                     if (chat!!.getReceiver().equals(senderId) && chat!!.getSender().equals(receiverId)
                         || chat!!.getReceiver().equals(receiverId)  &&  chat!!.getSender().equals(senderId))
                     // then add the chat in chatList one by one
                     {
                         (mChatList as ArrayList<Chat>).add(chat!! )
                     }

                     chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
//                     chatsAdapter = ChatsAdapter(this@MessageChatActivity ,  (mChatList as ArrayList<Chat>) ,
//                         receiverImageUrl.toString()
                 recycler_view_chats.adapter = chatsAdapter

                 }

             }

             override fun onCancelled(p0: DatabaseError) {

             }

         })


    }

}