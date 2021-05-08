package com.stejeetech.galampon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ViewImageActivity extends AppCompatActivity {
    private Context context;
    private ImageView postImage;
    private ImageView like;
    private ImageView comment;

    private TextView noOfLikes;
    private TextView noOfComments;

    private String postId, authorId, imageUrl;

    private FirebaseUser firebaseUser;

    String notifId;
    String existNotifId;

    String currentDateTime = new SimpleDateFormat("h:mma dd MMM yyyy", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GalAmpon");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(">>> ViewImageActivity", "FINISH");
                finish();
            }
        });

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        authorId = intent.getStringExtra("authorId");
        imageUrl = intent.getStringExtra("imageUrl");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        postImage = findViewById(R.id.post_image);
        like = findViewById(R.id.like);
        comment = findViewById(R.id.comment);
        postImage = findViewById(R.id.post_image);
        noOfLikes = findViewById(R.id.no_of_likes);
        noOfComments = findViewById(R.id.no_of_comments);

        getPostImage();
        isLiked(postId, like);
        noOfLikes(postId, noOfLikes);
        getComments(postId, noOfComments);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(postId).child(firebaseUser.getUid()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("LikedByUser")
                            .child(firebaseUser.getUid()).child(postId).setValue(true);

                    addNotification(postId, authorId);

                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(postId).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("LikedByUser")
                            .child(firebaseUser.getUid()).child(postId).removeValue();
                    removeNotification(postId, authorId, firebaseUser.getUid(), "liked your post.");
                }
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewImageActivity.this, CommentActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("authorId", authorId);
                ViewImageActivity.this.startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(">>> ViewImageActivity", "FINISH");
        finish();
    }

    private void getPostImage() {
        /*FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Post post = snapshot.getValue(Post.class);
                publisherId = post.getPublisher();
                if (post.getImageurl().equals("default")){
                    postImage.setImageResource(R.mipmap.ic_launcher_round);
                } else{
                    Glide.with(getApplicationContext()).load(post.getImageurl()).into(postImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        if (imageUrl.equals("default")){
            postImage.setImageResource(R.mipmap.ic_launcher_round);
        } else{
            Glide.with(getApplicationContext()).load(imageUrl).into(postImage);
        }


    }

    private void isLiked(String postId, final ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void noOfLikes (String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments (String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeNotification (String postId, String publisherId, String userId, String content){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(publisherId);

        Query query = ref.orderByChild("postid").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if(data.child("postid").exists() && data.child("userid").exists() && data.child("text").exists()) {
                            if(data.child("postid").getValue().toString().equals(postId)
                                    && data.child("userid").getValue().toString().equals(firebaseUser.getUid())
                                    && data.child("text").getValue().toString().equals("liked your post.")) {
                                existNotifId = data.getRef().getKey().toString();
                                Log.i(">>> REMOVE NOTIF DATA", existNotifId);
                                ref.child(existNotifId).removeValue();
                                break;
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotification(String postId, String publisherId) {
        HashMap<String, Object> map = new HashMap<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(publisherId);

        notifId = ref.push().getKey();

        map.put("notifid", notifId);
        map.put("userid", firebaseUser.getUid()); //to search if exist
        map.put("text", "liked your post."); //to search if exist
        map.put("postid", postId); //to search if exist
        map.put("datetime", currentDateTime);
        map.put("isPost", true);


        if (!firebaseUser.getUid().equals(publisherId)){
            Log.i("<<<ADD NOTIF DATA", notifId);
            ref.child(notifId).setValue(map);
        }
    }
}