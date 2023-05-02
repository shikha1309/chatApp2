package com.shikha.chatapp.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shikha.chatapp.AdapterClasses.UserAdapter
import com.shikha.chatapp.ModelClasses.Chatlist
import com.shikha.chatapp.ModelClasses.Users
import com.shikha.chatapp.R


class ChatFragment : Fragment() {
    private var userAdapter: UserAdapter? =null
    private var mUsers:List<Users>? = null
    private  var usersChatList:List<Chatlist>? =null
    lateinit var recycler_view_chatList:RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      val view  = inflater.inflate(R.layout.fragment_chat, container, false)
        recycler_view_chatList = view.findViewById(R.id.recycler_view_chatList)
        recycler_view_chatList.setHasFixedSize(true)
        recycler_view_chatList.layoutManager =LinearLayoutManager(context )

        firebaseUser =FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()
        // 1. adding users in the chat list
        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                (usersChatList as ArrayList).clear()
                 for (dataSnapshot in p0.children)
                 {
                     val chatlist = dataSnapshot.getValue(Chatlist::class.java)
                     (usersChatList as ArrayList) .add(chatlist!!)
                 }
                retrieveChatLists()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        return  view
    }


private fun retrieveChatLists(){
    // 1. Need an array list to retrieve and store chat in chatList
    // 2. add user in array List
    // 3. Retrieving users
    mUsers = ArrayList()
    val ref = FirebaseDatabase.getInstance().reference.child("Users")
    ref!!.addValueEventListener(object : ValueEventListener{
        override fun onDataChange(p0: DataSnapshot)
        {
           if (p0.exists())
           {
               (mUsers as ArrayList).clear()
               for ( dataSnapshot in p0.children)
               {
                   val  user = dataSnapshot.getValue(Users::class.java)
                   // eachChatList is single variable in userChatList
                   for (eachChatList in usersChatList!!)
                   {
                       if (user!!.getUID().equals(eachChatList.getId()))
                       {
                           (mUsers as ArrayList ).add (user!!)
                       }
                   }
               }

               userAdapter = UserAdapter(context!! ,(mUsers as ArrayList<Users>), true)
               recycler_view_chatList.adapter  = userAdapter

           }


        }

        override fun onCancelled(p0: DatabaseError) {

        }

    })


}




}