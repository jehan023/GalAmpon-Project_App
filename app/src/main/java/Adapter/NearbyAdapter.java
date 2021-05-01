package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.stejeetech.galampon.R;

import java.util.List;

import Fragments.PostDetailFragment;
import Model.Post;

public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;

    private FirebaseUser firebaseUser;

    public NearbyAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public NearbyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.nearby_item, parent, false);


        return new NearbyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyAdapter.ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getImageurl()).into(holder.postImage);
        holder.description.setText(post.getDescription());
        holder.date.setText(post.getDate());
        holder.location.setText(post.getPostlocation());

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fragCount = ((FragmentActivity)mContext).getSupportFragmentManager().getBackStackEntryCount();

                if (fragCount < 1){
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostid()).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(String.valueOf(new PostDetailFragment())).commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView postImage;
        public TextView location;
        public TextView date;
        SocialTextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImage = itemView.findViewById(R.id.post_image);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            description = itemView.findViewById(R.id.description);
        }
    }

}
