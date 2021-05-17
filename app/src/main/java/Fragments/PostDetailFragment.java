package Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.stejeetech.galampon.CommentActivity;
import com.stejeetech.galampon.MainActivity;
import com.stejeetech.galampon.R;
import com.stejeetech.galampon.ViewImageActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import Model.Post;
import Model.User;

public class PostDetailFragment extends Fragment {

    private String postId, profileID, publisherID, postImageUrl;
    private ImageView close, imageProfile, postImage, more, like, comment;
    private TextView username, date, location, noOfLikes, noOfComments;
    SocialTextView description;
    String notifId;
    String existNotifId;
    String removePostId;
    String likedUserId;
    String liker;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        postId = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).getString("postid", "none");
        profileID = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");

        close = view.findViewById(R.id.close);
        more = view.findViewById(R.id.more);
        like = view.findViewById(R.id.like);
        comment = view.findViewById(R.id.comment);
        imageProfile = view.findViewById(R.id.image_profile);
        postImage = view.findViewById(R.id.post_image);
        postImage = view.findViewById(R.id.post_image);
        username = view.findViewById(R.id.username);
        date = view.findViewById(R.id.date);
        location = view.findViewById(R.id.location);
        noOfLikes = view.findViewById(R.id.no_of_likes);
        noOfComments = view.findViewById(R.id.no_of_comments);
        description = view.findViewById(R.id.description);

        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);

                Glide.with(getContext()).load(post.getImageurl()).placeholder(R.drawable.ic_loading).into(postImage);
                date.setText(post.getDate());
                description.setText(post.getDescription());
                location.setText(post.getPostlocation());
                publisherID = post.getPublisher();
                postImageUrl = post.getImageurl();
                getUserInfo(post.getPublisher());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //getUserInfo();
        isLiked(postId, like);
        noOfLikes(postId, noOfLikes);
        getComments(postId, noOfComments);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
                Log.i(">>> PostDetailFragment", "Closed");
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Posts").child(publisherID);
                if (publisherID.equals(firebaseUser.getUid())) {
                    removePostId = postId;
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setIcon(R.drawable.ic_delete);
                    alertDialog.setTitle("Delete Post");
                    alertDialog.setMessage("Do you want to delete?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {

                            removeOnStorage(postImageUrl);
                            removeOnReport(removePostId);
                            removeComment(removePostId);
                            removePostLikes(removePostId);
                            removePostLikedByUser(removePostId);
                            removeAllPostNotification(removePostId, publisherID);

                            FirebaseDatabase.getInstance().getReference().child("Posts")
                                    .child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Post deleted successfully!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setIcon(R.drawable.ic_report);
                    alertDialog.setTitle("Report Post");
                    alertDialog.setMessage("Do you want to report this post?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("ReportPosts").child(postId)
                                    .child(firebaseUser.getUid()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Post reported successfully!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(postId).child(firebaseUser.getUid()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("LikedByUser")
                            .child(firebaseUser.getUid()).child(postId).setValue(true);

                    addNotification(postId, publisherID);

                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(postId).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("LikedByUser")
                            .child(firebaseUser.getUid()).child(postId).removeValue();

                    if(!publisherID.equals(firebaseUser.getUid())){
                        removeNotification(postId, publisherID, firebaseUser.getUid(), "liked your post.");
                    }
                }
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CommentActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("authorId", publisherID);
                getContext().startActivity(intent);
            }
        });

        noOfComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CommentActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("authorId", publisherID);
                getContext().startActivity(intent);
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewImageActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("authorId", publisherID);
                intent.putExtra("imageUrl", postImageUrl);
                getContext().startActivity(intent);
            }
        });

        return view;
    }

    private void removePostLikedByUser(String postId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("LikedByUser");
        Query query = ref.orderByChild(postId).equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if(data.child(postId).exists()) {
                            if(data.child(postId).getValue().equals(true)) {
                                likedUserId = data.getRef().getKey().toString();
                                Log.i("REMOVE LIKED DATA", likedUserId);
                                ref.child(likedUserId).child(postId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                        Log.d("DELETE LIKED POST ID", postId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d("DELETE LIKED POST ID", "onFailure: did not delete file");
                                    }
                                });
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

    private void removePostLikes(String postId) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d("DELETE POST LIKES", postId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d("DELETE POST LIKES", "onFailure: did not delete file");
            }
        });
    }

    private void removeComment(String postId) {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d("DELETE POST COMMENTS", postId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d("DELETE POST COMMENTS", "onFailure: did not delete file");
            }
        });
    }

    private void removeAllPostNotification(String postId, String publisherId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(publisherId);

        Query query = ref.orderByChild("postid").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if(data.child("postid").exists()) {
                            if(data.child("postid").getValue().toString().equals(postId)) {
                                existNotifId = data.getRef().getKey().toString();
                                Log.i("REMOVE NOTIF DATA", existNotifId);
                                ref.child(existNotifId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                        Log.d("DELETE POST NOTIFICATIONS", existNotifId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @SuppressLint("LongLogTag")
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d("DELETE POST NOTIFICATIONS", "onFailure: did not delete file");
                                    }
                                });
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

    private void removeOnReport(String postId) {
        FirebaseDatabase.getInstance().getReference().child("ReportPosts").child(postId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d("DELETE POST REPORTS", postId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d("DELETE POST REPORTS", "onFailure: did not delete file");
            }
        });
    }

    private void removeOnStorage(String imageUrl) {
        final StorageReference filePath = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        filePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d("DELETE ON STORAGE", imageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d("DELETE ON STORAGE", "onFailure: did not delete file");
            }
        });
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
                // code here
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
                // code here
            }
        });
    }

    private void getUserInfo(String publisherID) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(publisherID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                username.setText(user.getUsername());
                if (user.getImageurl().equals("default")){
                    imageProfile.setImageResource(R.drawable.iconround);
                } else{
                    Glide.with(getContext()).load(user.getImageurl()).placeholder(R.drawable.iconround).into(imageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeNotification (String postId, String publisherId, String userId, String content){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(publisherId);

        Query query = ref.orderByChild("postid");
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
        map.put("datetime", new SimpleDateFormat("h:mma dd MMM yyyy", Locale.getDefault()).format(new Date()));
        map.put("isPost", true);


        if (!firebaseUser.getUid().equals(publisherId)){
            Log.i("<<<INSERT DATA", notifId);
            ref.child(notifId).setValue(map);

            // GET THE CURRENT USER'S USERNAME
            DatabaseReference sender = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
            Query queryUSER = sender.orderByChild("username");
            queryUSER.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    liker = snapshot.child("username").getValue().toString();
                    Log.i("LIKER USERNAME", liker);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // GET THE PUBLISHER'S EMAIL

            DatabaseReference receiver = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId);
            Query query = receiver.orderByChild("email");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child("email").exists()) {
                            MainActivity main = new MainActivity();
                            String email = dataSnapshot.child("email").getValue().toString();
                            Log.i("RECEIVER EMAIL", email);
                            sendNotification(email,liker + " liked your post.");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
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
                                + "\"category\": \"Notification\","
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

    @Override
    public void onStop() {
        super.onStop();
        getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", profileID).apply();
        Log.i(">>> PostDetailFragment","ON STOP");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", profileID).apply();
        Log.i(">>> PostDetailFragment", "ON DESTROY VIEW");
    }
}