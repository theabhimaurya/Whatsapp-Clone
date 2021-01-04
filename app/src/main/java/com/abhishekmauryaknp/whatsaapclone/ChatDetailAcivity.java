package com.abhishekmauryaknp.whatsaapclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.abhishekmauryaknp.whatsaapclone.Adapter.ChatAdaper;
import com.abhishekmauryaknp.whatsaapclone.Models.MessageModel;
import com.abhishekmauryaknp.whatsaapclone.databinding.ActivityChatDetailAcivityBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailAcivity extends AppCompatActivity {

    ActivityChatDetailAcivityBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailAcivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

       database = FirebaseDatabase.getInstance();
       auth = FirebaseAuth.getInstance();

       final String senderId = auth.getUid();
       String recieveId = getIntent().getStringExtra("userId");
       String userName = getIntent().getStringExtra("userName");
       String profilepic = getIntent().getStringExtra("profilepic");

       binding.userNameChat.setText(userName);
        Picasso.get().load(profilepic).placeholder(R.drawable.ic_user).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailAcivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

       final ArrayList<MessageModel> messageModels = new ArrayList<>();

        final ChatAdaper chatAdaper = new ChatAdaper(messageModels, this);
        binding.chatRecyclerView2.setAdapter(chatAdaper);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView2.setLayoutManager(layoutManager);

        final String senderRoom = senderId+recieveId;
        final String recieverRoom = recieveId+senderId;

        database.getReference().child("Chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                     messageModels.clear();

                     for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                         MessageModel model = dataSnapshot1.getValue(MessageModel.class);

                         messageModels.add(model);
                     }
                     chatAdaper.notifyDataSetChanged();
                   }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.etMsg.getText().toString();
                final MessageModel model = new MessageModel(senderId, message);
                model.setTimestamp(new Date().getTime());
                binding.etMsg.setText("");

                database.getReference().child("Chats").child(senderRoom)
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        database.getReference().child("Chats").child(recieverRoom)
                                .push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                    }
                });

            }
        });

    }
}