package com.rinzler.myjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import Util.JournalApi;

//todo : use onscuccess instead of on complete if we want to use some values of the result of the whole document(document reference)
//todo : use on complete to get information on a single object(document snapshot)
public class CreateAccountActivity extends AppCompatActivity {

    private Button createAccountButton;
    private ProgressBar progressBar;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    //firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //each user will have their own collection
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        getSupportActionBar().setElevation(0);

        //initialize the firebase auth
        firebaseAuth  =  FirebaseAuth.getInstance();

        createAccountButton = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.create_account_progressbar);
        usernameEditText = findViewById(R.id.username_account);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);

        //authstatelistener is called when there is a change in the authentication state.
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null){
                    //user is already logged in...

                }else{
                    //no user yet
                }
            }
        };

        //create account
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(usernameEditText.getText().toString())
                && !TextUtils.isEmpty(emailEditText.getText().toString())
                && !TextUtils.isEmpty(passwordEditText.getText().toString())){

                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String username = usernameEditText.getText().toString();

                    if (password.length() <= 6){
                        Toast.makeText(CreateAccountActivity.this, "Password cannot be less than 6 characters", Toast.LENGTH_SHORT).show();
                    }else{
                        createUserEmailPasswordAccount(email,password,username);
                    }
                }else{
                    Toast.makeText(CreateAccountActivity.this,
                            "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void createUserEmailPasswordAccount(String email,String password, String username){
        if (!TextUtils.isEmpty(email) &&  !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

           progressBar.setVisibility(View.VISIBLE);


            //Creating user inside authentication
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        //todo the user account is created already : here we want the user details for further use so we the below code
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                //take user to add journal to activity
                                currentUser = firebaseAuth.getCurrentUser();
                                //we already create the user and we are inside on successful we get the user id
                                String currentUserId = currentUser.getUid();

                                //create a user Map so we can create a user in the collection
                                //the user map is created so it will be passed to post journal activity
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId",currentUserId);
                                userObj.put("username", username );

                                //save to our firestore database
                                //storing the user data in a collection
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            //document reference referes to the whole document inside the collection
                                            //todo : the userid and username is added to the users collection
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        //using completelistener to get a single user information (document snapshot)
                                                        //document snapshot helps us to see if the added document exists
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.getResult().exists()){
                                                                    //after completion make the progress invisible again
                                                                    progressBar.setVisibility(View.INVISIBLE);

                                                                    String name = task.getResult().getString("username");

                                                                    //todo : storing user name and id in the api and passing to the next activity
                                                                    JournalApi journalApi = JournalApi.getApiInstance();//Global api
                                                                    journalApi.setUsername(name);
                                                                    journalApi.setUserId(currentUserId);

                                                                    //pass the user name and userid to the next activity
                                                                    Intent intent = new Intent(CreateAccountActivity.this,
                                                                            PostJournalActivity.class);
                                                                    intent.putExtra("username", name);
                                                                    intent.putExtra("userId", currentUserId);
                                                                    startActivity(intent);
                                                                }else{
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CreateAccountActivity.this, "Please check the details entered..", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }else{
                                //something went wrong
                              //  Toast.makeText(CreateAccountActivity.this, , Toast.LENGTH_SHORT).show();
                                View contextView = findViewById(R.id.createaccount);
                                Snackbar.make(contextView,"Please provide a valid E-mail",Snackbar.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //initialize the
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}