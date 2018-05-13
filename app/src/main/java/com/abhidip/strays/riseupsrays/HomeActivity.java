package com.abhidip.strays.riseupsrays;

import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

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
    private ImageView commentIcon;
    private ImageView attendIcon;

    private DatabaseReference reference;
    private List<ChatMessage> messageList;
    private FloatingActionButton fab;
    public static final String LATITUDE = "lattitude";
    public static final String LONGITUDE = "longitude";



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

        fab = (FloatingActionButton) findViewById(R.id.fab);

       // commentIcon = (ImageView) findViewById(R.id.commentIcon);
       // attendIcon = (ImageView) findViewById(R.id.attendIcon);

        messageList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Messages");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter = new RecyclerAdapter(HomeActivity.this, messageList);
                recylerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent myIntent = new Intent(HomeActivity.this,MapsActivity.class);
                        ChatMessage chatMessage = messageList.get(position);

                        myIntent.putExtra(LATITUDE, chatMessage.getLatitude());
                        myIntent.putExtra(LONGITUDE, chatMessage.getLongitude());
                        startActivity(myIntent);
                    }
                });

                for (DataSnapshot postDataSnapShot : dataSnapshot.getChildren()) {
                    messageList.add(postDataSnapShot.getValue(ChatMessage.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent myIntent = new Intent(HomeActivity.this,MessageActivity.class);
                startActivity(myIntent);
            }
        });

        /*commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomeActivity.this,
                        CommentsActivity.class);
                startActivity(myIntent);
            }
        });*/


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
