package com.example.vicevi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class JokeSingleActivity extends AppCompatActivity {

    private String mPost_key=null;
    private DatabaseReference mDatabase;
    private Button mSingleEditBtn;
    private Button mSingleRemoveBtn;
    private EditText mSingleJokeTitle;
    private EditText mSingleJokeDesc;
    private FirebaseAuth mAuth;

    private DatabaseReference  mDatabaseUser;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke_single);

        mAuth=FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance().getReference().child("Joke");
        mStorage= FirebaseStorage.getInstance().getReference();
        mProgress=new ProgressDialog(this);
        mPost_key=getIntent().getExtras().getString("joke_id");
        mAuth=FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();

        mSingleJokeDesc=(EditText)findViewById(R.id.singleJokeDesc);
        mSingleJokeTitle=(EditText)findViewById(R.id.singleJokeTitle);
        mDatabaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mSingleEditBtn=(Button)findViewById(R.id.singleEditBtn);
        mSingleRemoveBtn=(Button)findViewById(R.id.singleRemoveBtn);
        final DatabaseReference editPost=mDatabase.push();
        final String testtitle = "testTitle";
        final String testdescription = "testDesc";

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                final String post_title= (String) dataSnapshot.child("title").getValue();
                final String post_desc= (String) dataSnapshot.child("desc").getValue();
                String post_uid= (String) dataSnapshot.child("uid").getValue();
                mSingleJokeTitle.setText(post_title);
                mSingleJokeDesc.setText(post_desc);

                if(mAuth.getCurrentUser().getUid().equals(post_uid)){
                    mSingleEditBtn.setVisibility(View.VISIBLE);
                    mSingleRemoveBtn.setVisibility(View.VISIBLE);
                    mSingleRemoveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mDatabase.child(mPost_key).removeValue();
                            Intent mainIntent =new Intent(JokeSingleActivity.this,MainActivity.class);
                            startActivity(mainIntent);
                        }
                    });
                    mSingleEditBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String title_val = mSingleJokeTitle.getText().toString().trim();
                            String desc_val = mSingleJokeDesc.getText().toString().trim();

                            editPost.child("title").setValue(title_val);
                            editPost.child("desc").setValue(desc_val);
                            editPost.child("uid").setValue(mCurrentUser.getUid());

                            mDatabase.child(mPost_key).removeValue();
                            editPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(JokeSingleActivity.this,MainActivity.class));
                                    }
                                }
                            });

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
