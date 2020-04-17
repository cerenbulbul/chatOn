package com.example.chatton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_request.*

class RequestActivity : AppCompatActivity() {
    private lateinit var UserRef: DatabaseReference
    private lateinit var ChatRequestRef: DatabaseReference
    private lateinit var userName: TextView
    private lateinit var userStatus: TextView
    private lateinit var current_state:String
    private lateinit var currentUserID:String
    private lateinit var auth: FirebaseAuth
    private lateinit var receiverUserID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        receiverUserID = intent.extras?.get("ID").toString()

        userName = findViewById<TextView>(R.id.visit_user_name)
        userStatus = findViewById<TextView>(R.id.visit_user_status)
        val senRequestButon:Button= findViewById<Button>(R.id.send_request_buton)
        current_state = "new"
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        UserRef = FirebaseDatabase.getInstance().reference.child("Users")
        ChatRequestRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        UserRef.child(receiverUserID).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                        val name:String= dataSnapshot.child("name").getValue().toString()
                        val status:String= dataSnapshot.child("status").getValue().toString()
                        userName.setText(name)
                        userStatus.setText(status)
                        ManageChatRequest()
                    } } })
    }

    fun ManageChatRequest(){

        ChatRequestRef.child(currentUserID).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val Request_Type:String= dataSnapshot.child(receiverUserID).child("Request_Type").getValue().toString()
                if (Request_Type.equals("sent")) {
                    current_state = "request_sent"
                    send_request_buton.setText("Cancel Chat Request")
                }
            }

        })

        if (!receiverUserID.equals(currentUserID)) {
            send_request_buton.setOnClickListener{
                send_request_buton.isEnabled= false
                if (current_state == "new") {
                    sendChatRequest()
                }
                if (current_state == "request_sent") {
                    CancelChatRequest()
                }
            }
        } else{
            send_request_buton.visibility= View.INVISIBLE
        }
    }

    fun sendChatRequest() {
        ChatRequestRef.child(currentUserID).child(receiverUserID).child("Request_Type").setValue("sent")
            .addOnCompleteListener(this, OnCompleteListener<Void> { task ->

                if (task.isSuccessful){
                    ChatRequestRef.child(receiverUserID).child(currentUserID).child("Request_Type").setValue("received")
                        .addOnCompleteListener(this, OnCompleteListener<Void> { task ->
                            if (task.isSuccessful) {
                                send_request_buton.isEnabled = true
                                current_state = "request_sent"
                                send_request_buton.setText("Cancel Chat Request")
                            }
                        })
                }
            })
    }

    fun CancelChatRequest(){
        ChatRequestRef.child(currentUserID).child(receiverUserID).removeValue().addOnCompleteListener(this, OnCompleteListener<Void> { task ->
            if (task.isSuccessful) {
                ChatRequestRef.child(receiverUserID).child(currentUserID).removeValue().addOnCompleteListener(this, OnCompleteListener<Void> { task ->
                    if (task.isSuccessful) {
                        send_request_buton.isEnabled =true
                        current_state = "new"
                        send_request_buton.setText("Send Message")
                    }
                })
            }
        })
    }
}