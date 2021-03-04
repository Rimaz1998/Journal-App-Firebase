package com.rinzler.myjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rinzler.myjournal.model.Journal;

import java.util.Date;

import Util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int ACCESS_GALLERY_CODE = 1;
    private static final int ACCESS_CAMERA_CODE = 2;
    private ImageView post_camerabutton;
    private ImageView post_imageView;

    private EditText titleeditext;
    private EditText thoughtedittext;
    private ProgressBar progressBar;
    private Button saveButton;


    private TextView currentUserTextview;

    private String currentUserId;
    private String  currentUsername;

    //get firebase authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser currentUser;
    //todo : we save images to the storage in the firestore, for it we need reference to the storage
    private StorageReference storageReference;
    //connection to firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //we already have a reference to the users collection now we create a collection for the journal we need to add
    private CollectionReference collectionReference = db.collection("Journal");

    //immage uri - Uniform resource identifier used to identify resources
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        getSupportActionBar().setElevation(0);

        //instantiate the storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        titleeditext = findViewById(R.id.post_title_edittext);
        thoughtedittext = findViewById(R.id.post_description_edittext);
        progressBar = findViewById(R.id.post_progressBar);
        currentUserTextview = findViewById(R.id.post_username_textview);

        post_imageView = findViewById(R.id.post_imageView);

        post_camerabutton = findViewById(R.id.post_camera_button);
        saveButton = findViewById(R.id.post_save_journal_button);

        post_camerabutton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        //getting the current username and id from the create account activity which was
        // todo passed through the journal api class we created and set the user name textview
        if (JournalApi.getApiInstance() != null){
            currentUserId = JournalApi.getApiInstance().getUserId();
            currentUsername = JournalApi.getApiInstance().getUsername();

            currentUserTextview.setText(currentUsername);
        }

        //if you attach an AuthStateListener you will get a callback every time the underlying token state changes.
                //This can be useful to react to edge cases like those mentioned above.

        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {

                } else {

                }

            }
        };

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
                    startActivity(new Intent(PostJournalActivity.this, MainActivity.class));
                    //  finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post_save_journal_button:
                //save journal
                saveJournal();
                break;
            case R.id.post_camera_button:
                //go to gallery and add an image
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //Intent galleryIntent = new Intent(Intent.ACTION_VIEW);
                //Show type image
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,ACCESS_GALLERY_CODE);
                break;

        }

    }

    //todo handle gallery access
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCESS_GALLERY_CODE && resultCode == RESULT_OK){
            if (data != null){
                //todo : save the image to the imageUri
                imageUri = data.getData();
                // Bitmap photo = (Bitmap) data.getExtras().get("data");
                // post_imageView.setImageBitmap(photo);
                post_imageView.setImageURI(imageUri);

            }else{
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveJournal() {
        String title = titleeditext.getText().toString().trim();
        String thought = thoughtedittext.getText().toString().trim();

       // progressBar.setVisibility(View.VISIBLE);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Journal");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();


        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought)
            && imageUri != null){

            //todo:defining a file path and adding the image to the storage entity in firestore
            StorageReference  filepath = storageReference
                    //storage reference in firestore(folder name)
                    .child("journal_images")
                    //this stores the images in different names unique id (file name)
                    .child("my_image" + Timestamp.now().getNanoseconds());

            //todo: put the image uri in the filepath we created
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //todo get the image url and store in the collection journal
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                @Override
                                public void onSuccess(Uri uri) {
                                    //get the image using the uri (image link)
                                    String imageUrl = uri.toString();
                                    //TODO: Create a journal object - model
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thought);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    //these were passed from the create account actibity journal api
                                    journal.setUserName(currentUsername);
                                    journal.setUserId(currentUserId);

                                    //Todo: invoke collection reference  for journal
                                    //todo : the journal details as well as the images url which is stored in the storage is stored
                                    //with the journal
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    //progressBar.setVisibility(View.INVISIBLE);
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PostJournalActivity.this, "Adding Journal not Successful..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //todo : and save a journal instance
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(PostJournalActivity.this, "Failed to add journal", Toast.LENGTH_SHORT).show();
                    Toast.makeText(PostJournalActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
           // progressBar.setVisibility(View.INVISIBLE);
            progressDialog.dismiss();
           if (TextUtils.isEmpty(title) && TextUtils.isEmpty(thought)){
               Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
           }
           if (imageUri == null){
               Toast.makeText(this, "Please add an image to continue", Toast.LENGTH_SHORT).show();
           }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        //always listen to the user state
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //kill the listener on stop
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(listener);
        }
    }
}