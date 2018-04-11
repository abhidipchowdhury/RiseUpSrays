package com.abhidip.strays.riseupsrays;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.abhidip.strays.model.ChatMessage;
import com.abhidip.strays.util.RecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar1;
    private RecyclerView recylerView;
    private RecyclerAdapter adapter;

    private DatabaseReference reference;
    private List<ChatMessage> messageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar1 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar1);
       // toolbar1.setOverflowIcon(R.drawable.ic_format_list_bulleted );
          getSupportActionBar().setTitle("Home Page");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar1.setElevation(10f);
        }

        recylerView = (RecyclerView) findViewById(R.id.recylerView);
        recylerView.setHasFixedSize(true);
        recylerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Messages");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postDataSnapShot : dataSnapshot.getChildren()) {
                    messageList.add(postDataSnapShot.getValue(ChatMessage.class));
                }
             adapter = new RecyclerAdapter(HomeActivity.this, messageList);
                recylerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    // Opens the new activity for message
    public void openNewActivity(MenuItem item) {
        Intent myIntent = new Intent(HomeActivity.this,
                MessageActivity.class);
        startActivity(myIntent);
    }
}
