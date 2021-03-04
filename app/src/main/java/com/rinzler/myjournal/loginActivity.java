package com.rinzler.myjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rinzler.myjournal.model.Journal;

import Util.JournalApi;

public class loginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button signUpButton;

    private AutoCompleteTextView email;
    private EditText password;
    private ProgressBar loginProgressBar;

    //get the firebase instance variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStatelistener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setElevation(0);

        loginProgressBar = findViewById(R.id.login_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.signup_button);

        email = findViewById(R.id.email_edittext);
        password = findViewById(R.id.password_edittext);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this,CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEmailPasswordUser(email.getText().toString().trim(), password.getText().toString().trim());
            }
        });

        //authstatelistener is called when there is a change in the authentication state.

        authStatelistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
    }

    private void loginEmailPasswordUser(String email, String password) {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
      // loginProgressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            //todo user login
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        //todo : after successfully loggining in
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert  user != null;
                            String currentUserId = user.getUid();

                            //Todo : checking if the autenticated user is the user we have in the users collection reference
                            collectionReference
                                    .whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                            if (error != null){
                                                Toast.makeText(loginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                            assert queryDocumentSnapshots != null;

                                            if (!queryDocumentSnapshots.isEmpty()){
                                                //loginProgressBar.setVisibility(View.INVISIBLE);
                                                progressDialog.dismiss();
                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                                    JournalApi journalApi = JournalApi.getApiInstance();
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    journalApi.setUserId(snapshot.getString("userId"));

                                                    //go to listActivity
                                                    startActivity(new Intent(loginActivity.this,JournalListActivity.class));

                                                }
                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                   // loginProgressBar.setVisibility(View.INVISIBLE);
                    progressDialog.dismiss();
                }
            });
        }else{
            Toast.makeText(this, "Fields Cannot be Empty", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Enter email and password to login", Toast.LENGTH_SHORT).show();
           // loginProgressBar.setVisibility(View.INVISIBLE);
            progressDialog.dismiss();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStatelistener);
    }
}