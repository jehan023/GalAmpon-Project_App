package Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stejeetech.galampon.R;

import java.util.List;

import Fragments.PostDetailFragment;
import Fragments.ProfileFragment;
import Model.Notification;
import Model.Post;
import Model.User;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotifications;
    private FirebaseUser firebaseUser;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);

        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Notification notification = mNotifications.get(position);

        getUser(holder.imageProfile, holder.username, notification.getUserid());
        holder.comment.setText(notification.getText());
        holder.datetime.setText(notification.getDatetime());

        if (notification.isIsPost()) {
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage, notification.getPostid());
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        holder.imageProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit().putString("profileId", notification.getUserid()).apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(null).commit();
            }
        });

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notification.isIsPost()) {
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                            .edit().putString("postid", notification.getPostid()).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(String.valueOf(new PostDetailFragment())).commit();
                } else {
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                            .edit().putString("profileId", notification.getUserid()).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager()
                            .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).addToBackStack(String.valueOf(new PostDetailFragment())).commit();
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle("Do you want to delete this notification?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference().child("Notifications")
                                .child(firebaseUser.getUid()).child(notification.getNotifid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(mContext, "Notification deleted successfully!", Toast.LENGTH_SHORT).show();
                                    Log.i("DELETE NOTIFICATION", String.valueOf(notification.getNotifid()));
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

            return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageProfile;
        public ImageView postImage;
        public TextView username;
        public TextView comment;
        public TextView datetime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            datetime = itemView.findViewById(R.id.datetime);
        }
    }

    private void getPostImage(final ImageView imageView, String postId) {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.i("DATASNAPSHOT", dataSnapshot.getValue().toString());

                    Post post = dataSnapshot.getValue(Post.class);
                    if (post == null) throw new AssertionError();
                    if(post.getImageurl() != null){
                        Glide.with(mContext).load(post.getImageurl()).placeholder(R.mipmap.ic_launcher).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getUser(final ImageView imageView, final TextView textView, String userId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getImageurl() != null){
                    if (user.getImageurl().equals("default")) {
                        imageView.setImageResource(R.drawable.iconround);
                    } else {
                        Glide.with(mContext).load(user.getImageurl()).placeholder(R.drawable.iconround).into(imageView);
                    }
                    textView.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}