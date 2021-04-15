package com.example.vicevi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mSetupImageBtn;
    private EditText mNameField;
    private Button mSubmitBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageImage;
    private ProgressDialog mProgres;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mNameField = (EditText) findViewById(R.id.setupNameField);
        mSubmitBtn = (Button) findViewById(R.id.setupSubmitBtn);

        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();

        mProgres=new ProgressDialog(this);

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String post_name=(String) dataSnapshot.child("name").getValue();
                mNameField.setText(post_name);

              /*  String post_image=(String)dataSnapshot.child("image").getValue();
                Glide.with(SetupActivity.this).load(post_image).into(mSetupImageBtn);
*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });
    }
    private void startSetupAccount() {
        final String name=mNameField.getText().toString().trim();

        final String user_id=mAuth.getCurrentUser().getUid();
        if(!TextUtils.isEmpty(name)){
            mProgres.setMessage("Finishing setup...");
            mProgres.show();
            mDatabaseUsers.child(user_id).child("name").setValue(name);

            Intent mainIntent=new Intent(SetupActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();



        }


    }

}
