package Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import com.stejeetech.galampon.R;
import com.stejeetech.galampon.ViewImageActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Fragments.ProfileFragment;
import Model.Post;
import Model.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;
    String notifId;
    String existNotifId;
    String removePostId;
    String likedUserId;

    String currentDateTime = new SimpleDateFormat("h:mma dd MMM yyyy", Locale.getDefault()).format(new Date());

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getImageurl()).placeholder(R.drawable.ic_loading).into(holder.postImage);
        holder.description.setText(post.getDescription());
        holder.date.setText(post.getDate());
        holder.location.setText("- at " + post.getPostlocation());

        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user.getImageurl().equals("default")){
                    holder.imageProfile.setImageResource(R.mipmap.ic_launcher_round);
                } else{
                    Glide.with(mContext).load(user.getImageurl()).into(holder.imageProfile);
                }
                holder.username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // code here
            }
        });

        isLiked(post.getPostid(), holder.like);
        noOfLikes(post.getPostid(), holder.noOfLikes);
        getComments(post.getPostid(), holder.noOfComments);

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Posts").child(post.getPublisher());
                if (post.getPublisher().equals(firebaseUser.getUid())) {
                    removePostId = post.getPostid().toString();
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setIcon(R.drawable.ic_delete);
                    alertDialog.setTitle("Delete Post");
                    alertDialog.setMessage("Do you want to delete?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {

                            removeOnStorage(post.getImageurl());
                            removeOnReport(removePostId);
                            removeComment(removePostId);
                            removePostLikes(removePostId);
                            removePostLikedByUser(removePostId);
                            removeAllPostNotification(removePostId, post.getPublisher());

                            FirebaseDatabase.getInstance().getReference().child("Posts")
                                    .child(post.getPostid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(mContext, "Post deleted successfully!", Toast.LENGTH_SHORT).show();
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
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setIcon(R.drawable.ic_report);
                    alertDialog.setTitle("Report Post");
                    alertDialog.setMessage("Do you want to report this post?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("ReportPosts").child(post.getPostid())
                                    .child(firebaseUser.getUid()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(mContext, "Post reported successfully!", Toast.LENGTH_SHORT).show();
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

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("LikedByUser")
                            .child(firebaseUser.getUid()).child(post.getPostid()).setValue(true);

                    addNotification(post.getPostid(), post.getPublisher());

                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("LikedByUser")
                            .child(firebaseUser.getUid()).child(post.getPostid()).removeValue();

                    if(!post.getPublisher().equals(firebaseUser.getUid())){
                        removeNotification(post.getPostid(), post.getPublisher(), firebaseUser.getUid(), "liked your post.");
                    }
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostid());
                intent.putExtra("authorId", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.noOfComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostid());
                intent.putExtra("authorId", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit().putString("profileId", post.getPublisher()).apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit().putString("profileId", post.getPublisher()).apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ViewImageActivity.class);
                intent.putExtra("postId", post.getPostid());
                intent.putExtra("authorId", post.getPublisher());
                intent.putExtra("imageUrl", post.getImageurl());
                mContext.startActivity(intent);
            }
        });
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

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageProfile;
        public ImageView postImage;
        public ImageView like;
        public ImageView comment;
        public ImageView more;

        public TextView username;
        public TextView date;
        public TextView noOfLikes;
        public TextView noOfComments;
        public TextView location;
        SocialTextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            more = itemView.findViewById(R.id.more);

            username = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            noOfLikes = itemView.findViewById(R.id.no_of_likes);
            noOfComments = itemView.findViewById(R.id.no_of_comments);
            description = itemView.findViewById(R.id.description);
            location = itemView.findViewById(R.id.location);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                ViewOutlineProvider provider = new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        int curveRadius = 24;
                        outline.setRoundRect(0, 0, view.getWidth(), (view.getHeight()), curveRadius);
                    }
                };
                postImage.setOutlineProvider(provider);
                postImage.setClipToOutline(true);
            }
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
                Log.i("<<<INSERT DATA", notifId);
                ref.child(notifId).setValue(map);
        }
    }

}

