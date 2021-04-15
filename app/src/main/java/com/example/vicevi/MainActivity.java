package com.example.vicevi;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mJokeList;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseCurrentUser;
    private boolean mProcessLike=false;
    private Query mQueryCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){

                    Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(loginIntent);

                }
            }
        };

        mDatabase=FirebaseDatabase.getInstance().getReference().child("Joke");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);
        LinearLayoutManager layoutMenager= new LinearLayoutManager(this);
        layoutMenager.setReverseLayout(true);
        layoutMenager.setStackFromEnd(true);

        mJokeList=(RecyclerView)findViewById(R.id.joke_list);
        mJokeList.setHasFixedSize(true);
        mJokeList.setLayoutManager(layoutMenager);

        checkUserExist();
    }

    private void checkUserExist(){

        if(mAuth.getCurrentUser()!=null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerOptions<Joke> options=new FirebaseRecyclerOptions.Builder<Joke>().setQuery(mDatabase,Joke.class).setLifecycleOwner(this).build();

        FirebaseRecyclerAdapter<Joke,JokeViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Joke, JokeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull JokeViewHolder holder, int position, @NonNull Joke model) {

                final String post_key=getRef(position).getKey();
                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDesc());
                holder.setUsername(model.getUsername() );
                holder.setLikeBtn(post_key);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent singleJokeIntent=new Intent(MainActivity.this,JokeSingleActivity.class);
                        singleJokeIntent.putExtra("joke_id",post_key);
                        startActivity(singleJokeIntent);

                    }
                });

                holder.mLikebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike=true;
                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Value");
                                        mProcessLike = false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }

            @NonNull
            @Override
            public JokeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.joke_row,viewGroup,false);
                return new JokeViewHolder(view);
            }
        };
        mJokeList.setAdapter(firebaseRecyclerAdapter);

    }
    public static class JokeViewHolder  extends RecyclerView.ViewHolder{

        View mView;
        ImageButton mLikebtn;
        int countLikes;
        TextView mNumLike;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public JokeViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            mLikebtn=(ImageButton)mView.findViewById(R.id.like_btn);
            mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth=FirebaseAuth.getInstance();
            mNumLike=(TextView)mView.findViewById(R.id.numOfLikes);
            mDatabaseLike.keepSynced(true);
        }
        public void setLikeBtn(final String post_key){

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        countLikes=(int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikebtn.setImageResource(R.mipmap.icons8_good_quality_filled_50);
                        mNumLike.setText(Integer.toString(countLikes)+(" Likes"));
                    }else{
                        countLikes=(int) dataSnapshot.child(post_key).getChildrenCount();
                        mLikebtn.setImageResource(R.mipmap.icons8_good_quality_50);
                        mNumLike.setText(Integer.toString(countLikes)+(" Likes"));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        public void setTitle(String title){

            TextView post_title=(TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }
        public  void setDesc(String desc){

            TextView post_desc=(TextView)mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setUsername(String username){

            TextView post_usernmae=(TextView)mView.findViewById(R.id.post_username);
            post_usernmae.setText(username);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.action_add){
            startActivity(new Intent(MainActivity.this,PostActivity.class));
        }else if(item.getItemId()==R.id.action_logout){
            logout();
        }else if(item.getItemId()==R.id.action_settings){
            startActivity(new Intent(MainActivity.this,SetupActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        sentToLogin();
    }
    private void sentToLogin() {
        Intent loginIntent=new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }
}
