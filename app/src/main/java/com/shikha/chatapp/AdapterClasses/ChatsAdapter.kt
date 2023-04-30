package com.shikha.chatapp.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shikha.chatapp.ModelClasses.Chat
import com.shikha.chatapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter  (
    mContext: Context,
    mChatList:List<Chat>,
    imageUrl:String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?> ()

{
    private val mContext :Context
    private val mChatList:List<Chat>
    private val imageUrl :String
    var firebaseUser :FirebaseUser =FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList =mChatList
        this.mContext =mContext
        this.imageUrl =imageUrl
    }
//position== Layout positions
    override fun onCreateViewHolder(parent: ViewGroup, position : Int): ViewHolder
    {
           return  if (position == 1){
               val view:View=LayoutInflater.from(mContext).inflate(R.layout.message_item_right_side_row,parent,false)
             ViewHolder(view)
           }
          else{
               val view:View=LayoutInflater.from(mContext).inflate(R.layout.message_item_left_side_row,parent,false)
               ViewHolder(view)
           }
    }

    override fun getItemCount(): Int {
   return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val chat:Chat =mChatList[position]

        Picasso.get().load(imageUrl).into(holder.profileImage)

        // 1. Check the message is image message or not

        if (chat.getMessage().equals("send you an image ") &&  !chat.getUrl().equals(""))
        {
            //if it is sender and image messages will be on Right Side
            if (chat.getSender().equals(firebaseUser!!.uid))
            {
                holder.show_text_message!!.visibility = View.GONE
                holder.right_side_image_view!!.visibility=View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.right_side_image_view)
            }
            // else it is receiver and Image message will be on Left Side
            else if (!chat.getSender().equals(firebaseUser!!.uid))
            {
                holder.show_text_message!!.visibility = View.GONE
                holder.left_side_image_view!!.visibility=View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.left_side_image_view)
            }
        }
         // 1.2 )  otherwise it will be text messages  // which is by default visible

        else
        {
          holder.show_text_message!!.text = chat.getMessage()
        }
      // 2. Implementation of send and seen  text  messages
        if ( position == mChatList.size-1)
        {
          if (chat.getIsSeen() )
          {
              holder.text_seen!!.text= "Seen"
              if(chat.getMessage().equals("send you an Image .") && !chat.getUrl().equals(""))
              {
                  val layoutParams : RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams
                  layoutParams !!.setMargins(0,245,10,0)
                  holder.text_seen !!.layoutParams = layoutParams
              }
          }
            else
            {
                holder.text_seen!!.text= "Sent"
                if(chat.getMessage().equals("send you an Image .") && !chat.getUrl().equals(""))
                {
                    val layoutParams : RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams
                    layoutParams !!.setMargins(0,245,10,0)
                    holder.text_seen !!.layoutParams = layoutParams
                }
            }
        }
       else
        {
             holder.text_seen!!.visibility = View.GONE
        }


    }


 inner class  ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
 {
     var profileImage: CircleImageView? = null
     var show_text_message: TextView? = null
     var left_side_image_view: ImageView? =null
     var text_seen: TextView? =null
     var right_side_image_view: ImageView? =null

     init {
         profileImage = itemView.findViewById(R.id.profileImage)
         show_text_message = itemView.findViewById(R.id.show_text_message )
         left_side_image_view = itemView.findViewById(R.id.left_side_image_view)
         text_seen = itemView.findViewById(R.id.text_seen)
         right_side_image_view = itemView.findViewById(R.id.right_side_image_view )

     }
 }

    override fun getItemViewType(position: Int): Int {
         // 1 for sender 0 for receiver
        //position == layout position of left or Right
        firebaseUser =FirebaseAuth.getInstance().currentUser!!
        return if (mChatList[position].getSender() .equals(firebaseUser!!.uid))
        {
           1
        }
        else
        {
              0
        }

    }

}