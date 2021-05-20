package com.stejeetech.galampon;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import Adapter.CommentAdapter;
import Model.Comment;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addComment;
    private CircleImageView imageProfile;
    private TextView post;

    private String postId;
    private String authorId;
    String commentid;
    String commentorUsername;

    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Log.d(">>> CommentActivity","Finish()");
            }
        });

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        authorId = intent.getStringExtra("authorId");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postId, authorId);

        recyclerView.setAdapter(commentAdapter);

        addComment = findViewById(R.id.add_comment);
        imageProfile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserImage();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(addComment.getText().toString())) {
                    Toast.makeText(CommentActivity.this, "No comment added!", Toast.LENGTH_SHORT).show();
                } else {
                    putComment();
                }
            }
        });

        getComment();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(">>> CommentActivity","onStop invoked");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(">>> CommentActivity","onDestroy invoked");
    }

    private void getComment() {

        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void putComment() {
        HashMap<String, Object> map = new HashMap<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);

        commentid = ref.push().getKey();

        map.put("commentid", commentid);
        map.put("comment", addComment.getText().toString());
        map.put("publisher", fUser.getUid());
        map.put("datetime", new SimpleDateFormat("h:mma dd MMM yyyy", Locale.getDefault()).format(new Date()));

        addComment.setText("");

        ref.child(commentid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CommentActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CommentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        addNotification(ref.getKey(), fUser.getUid(), commentid);
    }

    private void getUserImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getImageurl().equals("default")) {
                    imageProfile.setImageResource(R.drawable.iconround);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageurl()).placeholder(R.drawable.iconround).into(imageProfile);
                }
                commentorUsername = user.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addNotification(String postId, String publisherId, String notifId) {
        HashMap<String, Object> map = new HashMap<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(authorId);

        map.put("notifid", notifId); //to search for delete
        map.put("userid", publisherId);
        map.put("text", "comment on your post.");
        map.put("postid", postId);
        map.put("datetime", new SimpleDateFormat("h:mma dd MMM yyyy", Locale.getDefault()).format(new Date()));
        map.put("isPost", true);

        if(!publisherId.equals(authorId)){
            ref.child(notifId).setValue(map);
            DatabaseReference receiver = FirebaseDatabase.getInstance().getReference().child("Users").child(authorId);
            Query query = receiver.orderByChild("email");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child("email").exists()) {
                            String email = dataSnapshot.child("email").getValue().toString();
                            Log.i("RECEIVER EMAIL", email);
                            Log.i("COMMENTOR USERNAME", commentorUsername);
                            sendNotification(email,commentorUsername + " comment on your post.");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            addReplyNotification(postId, publisherId, notifId);
        }
    }

    private void addReplyNotification(String postId, String publisherId, String notifId) {
        HashMap<String, Object> reply_map = new HashMap<>();

        reply_map.put("notifid", notifId); //to search for delete
        reply_map.put("userid", publisherId);
        reply_map.put("text", "reply on your commented post.");
        reply_map.put("postid", postId);
        reply_map.put("datetime", new SimpleDateFormat("h:mma dd MMM yyyy", Locale.getDefault()).format(new Date()));
        reply_map.put("isPost", true);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications");
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        Query query = users.orderByChild("publisher");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()){
                    if (data.child("publisher").exists()) {
                        String ID = data.child("publisher").getValue().toString();
                        Log.i("ADD REPLY NOTIFICATION", ID);
                        if(!fUser.getUid().equals(ID)){
                            ref.child(ID).child(notifId).setValue(reply_map);
                            DatabaseReference receiver = FirebaseDatabase.getInstance().getReference().child("Users").child(ID);
                            Query search = receiver.orderByChild("email");
                            search.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.child("email").exists()) {
                                            String email = dataSnapshot.child("email").getValue().toString();
                                            Log.i("RECEIVER EMAIL", email);
                                            Log.i("COMMENTOR USERNAME", commentorUsername);
                                            sendNotification(email,commentorUsername + " reply on your commented post.");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String receiver, String Message) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = Build.VERSION.SDK_INT;
                if (SDK_INT > 8){
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic MzMyNWViMTQtMTlmNC00ZDcyLWI2YmMtNDdkMjA3OWI4YjQx");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"e9766fbd-cdb8-4f98-8015-80ca9add301d\","
                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \""+ receiver + "\"}],"
                                + "\"data\": {\"foo\": \"bar\"},"
                                + "\"contents\": {\"en\": \"" + Message +"\"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch(Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }
}