package Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.stejeetech.galampon.R;

import java.util.List;

import Model.Comment;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComments;

    String postId;
    String authorId;

    private FirebaseUser fUser;

    public CommentAdapter(Context mContext, List<Comment> mComments , String postId, String authorId) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.postId = postId;
        this.authorId = authorId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        final Comment comment = mComments.get(position);

        holder.comment.setText(comment.getComment());
        holder.datetime.setText(comment.getDatetime());

        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                holder.username.setText(user.getUsername());
                if (user.getImageurl().equals("default")) {
                    holder.imageProfile.setImageResource(R.drawable.iconround);
                } else {
                    Glide.with(mContext).load(user.getImageurl()).placeholder(R.drawable.iconround).into(holder.imageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", comment.getPublisher()).apply();
                mContext.startActivity(intent);
            }
        });*/

        /*holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", comment.getPublisher()).apply();
                mContext.startActivity(intent);
            }
        });*/

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comment.getPublisher().equals(fUser.getUid())) {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Do you want to delete this comment?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            final ProgressDialog pd = new ProgressDialog(mContext);
                            pd.setMessage("Deleting comment...");
                            pd.show();
                            FirebaseDatabase.getInstance().getReference().child("Comments")
                                    .child(postId).child(String.valueOf(comment.getCommentid())).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        removeCommentNotification(comment.getCommentid());
                                        //FirebaseDatabase.getInstance().getReference().child("Notifications").child(authorId).child(String.valueOf(comment.getCommentid())).removeValue();
                                        Toast.makeText(mContext, "Comment deleted successfully!", Toast.LENGTH_SHORT).show();
                                        Log.i("DELETE COMMENT ID", String.valueOf(comment.getCommentid()));
                                        Log.i("DELETE POST PUB", authorId);
                                        pd.dismiss();
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

                return true;
            };
        });

    }

    private void removeCommentNotification(String commentID) {
        DatabaseReference commentNotif = FirebaseDatabase.getInstance().getReference().child("Notifications");
        Query query = commentNotif.orderByChild(commentID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    if (data.child(commentID).exists()){
                        commentNotif.child(data.getKey()).child(commentID).removeValue();
                        Log.i("DELETE COMMENT NOTIF", data.getKey().toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imageProfile;
        public TextView username;
        public TextView comment;
        public TextView datetime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            datetime = itemView.findViewById(R.id.datetime);
        }
    }

}
