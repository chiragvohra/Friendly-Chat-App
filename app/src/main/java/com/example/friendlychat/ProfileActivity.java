package com.example.friendlychat;

//import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,currentState,senderUserId;
    private CircleImageView userProfileImage;
    TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;

    private DatabaseReference UserRef,chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName = (TextView)findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView)findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = (Button)findViewById(R.id.send_message_request_button);
        declineMessageRequestButton =(Button)findViewById(R.id.decline_message_request_button);

        currentState = "new";

        retrieveUserInfo();

    }

    private void retrieveUserInfo()
    {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists() )
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    if(dataSnapshot.hasChild("image"))
                    {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(ProfileActivity.this).load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    }

                    manageChatRequests();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequests()
    {
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(receiverUserId))
                {
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent"))
                    {
                        currentState = "request_sent";
                        sendMessageRequestButton.setText("CANCEL FRIEND REQUEST");
                    }
                    else if(request_type.equals("received"))
                    {
                        currentState = "request_received";
                        sendMessageRequestButton.setText("ACCEPT FRIEND REQUEST");
                        sendMessageRequestButton.setBackgroundColor(Color.GREEN);

                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);

                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                cancelChatRequest();
                            }
                        });
                    }

                }
                else
                {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId))
                            {
                                currentState = "friends";
                                sendMessageRequestButton.setText("UNFRIEND THIS USER");
                                sendMessageRequestButton.setEnabled(true);
                                sendMessageRequestButton.setBackgroundColor(Color.RED);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        if(!senderUserId.equals(receiverUserId))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if(currentState.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if (currentState.equals("friends"))
                    {
                        removeSpecificContact();
                    }
                }
            });

        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void removeSpecificContact()
    {
        contactsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful())
                {
                    contactsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendMessageRequestButton.setEnabled(true);
                                currentState = "new";
                                sendMessageRequestButton.setText("SEND FRIEND REQUEST");
                                sendMessageRequestButton.setBackgroundColor(R.drawable.buttons);

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest()
    {
        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful())
                {
                    contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {

                                                        currentState = "friends";
                                                        sendMessageRequestButton.setText("UNFRIEND THIS USER");
                                                        sendMessageRequestButton.setBackgroundColor(Color.RED);
                                                        sendMessageRequestButton.setEnabled(true);

                                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                        declineMessageRequestButton.setEnabled(false);


                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    private void cancelChatRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendMessageRequestButton.setEnabled(true);
                                currentState = "new";
                                sendMessageRequestButton.setText("SEND FRIEND REQUEST");
                                sendMessageRequestButton.setBackgroundColor(R.drawable.buttons);

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {

                if(task.isSuccessful())
                {
                    chatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful())
                            {

                                HashMap<String,String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from",senderUserId);
                                chatNotificationMap.put("type","request");

                                notificationRef.child(receiverUserId).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            sendMessageRequestButton.setEnabled(true);
                                            currentState = "request_sent";
                                            sendMessageRequestButton.setText("CANCEL FRIEND REQUEST");
                                        }
                                    }
                                });





                            }
                        }
                    });
                }
            }
        });


    }
}
