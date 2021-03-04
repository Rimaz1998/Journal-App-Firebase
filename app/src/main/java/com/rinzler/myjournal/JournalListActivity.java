package com.rinzler.myjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.rinzler.myjournal.model.Journal;

import java.util.ArrayList;
import java.util.List;

import Util.JournalApi;
import ui.JournalRecyclerAdapter;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    //recycler view instance variables
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter recyclerAdapter;

    private CollectionReference collectionReference = db.collection("Journal");
    private TextView noJournalEntry;

    private FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        getSupportActionBar().setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        noJournalEntry = findViewById(R.id.list_no_thoughts);
        journalList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //take users to post journal activity
                if (currentUser != null && firebaseAuth != null) {
                    startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                    //finish();
                }
            }
        });
    }


    //inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //handling menu items click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
           /* case R.id.action_add:
                //take users to post journal activity
                if (currentUser != null && firebaseAuth != null){
                    startActivity(new Intent(JournalListActivity.this,PostJournalActivity.class));
                    //finish();
                }
                break;*/
            case R.id.logout_user:
                if (currentUser != null){
                    firebaseAuth.signOut();
                    //go to main page after logging out
                    startActivity(new Intent(JournalListActivity.this, MainActivity.class));
                  //  finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //get our all journals
        //get the all the journals thats under the selected userid
        collectionReference.whereEqualTo("userId", JournalApi.getApiInstance()
                .getUserId())
                // .orderBy("timeAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //todo we will get all the journals under the same userid
                        if (!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot journals : queryDocumentSnapshots){
                                Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal);
                            }
                            recyclerAdapter  = new JournalRecyclerAdapter(JournalListActivity.this,journalList);
                            recyclerView.setAdapter(recyclerAdapter);
                            recyclerAdapter.notifyDataSetChanged();
                        }else {
                            noJournalEntry.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(JournalListActivity.this, "Error Retreving journals at this time..", Toast.LENGTH_SHORT).show();
            }
        });


    }
}