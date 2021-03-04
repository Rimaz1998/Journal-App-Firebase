package com.rinzler.myjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import Util.JournalApi;

public class MainActivity extends AppCompatActivity {
    private Button getStartedButton;

    //firebase variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser currentUser;

    //firebase connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       getSupportActionBar().setElevation(0);

        getStartedButton = findViewById(R.id.get_started_button);

        firebaseAuth = FirebaseAuth.getInstance();

        //authstatelistener is called when there is a change in the authentication state.
        //keeping the user logged in until they signout of the app
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null){
                    currentUser = firebaseAuth.getCurrentUser();
                    final String currentUserId = currentUser.getUid();

                    collectionReference
                            //if the user id is already there in current userid go straight to the list
                            .whereEqualTo("userId",currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                    if (error != null){
                                        return;
                                    }
                                    String name;

                                    if (!queryDocumentSnapshots.isEmpty()){
                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                            JournalApi journalApi = JournalApi.getApiInstance();
                                            journalApi.setUserId(snapshot.getString("userId"));

                                            startActivity(new Intent(MainActivity.this,JournalListActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                }else {

                }
            }
        };

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to login page/ login activity
                startActivity(new Intent(MainActivity.this,loginActivity.class));
                //todo getting rid of the activity as soon the user moves forward
                finish();
            }
        });
    }

    //start the listener
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }

    //stop the listener as soon as the app stops
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(listener);
        }
    }
}