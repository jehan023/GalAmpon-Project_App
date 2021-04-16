package Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stejeetech.galampon.EditProfileActivity;
import com.stejeetech.galampon.OptionsActivity;
import com.stejeetech.galampon.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapter.PhotoAdapter;
import Model.Post;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerViewLiked;
    private PhotoAdapter postAdapterLiked;
    private List<Post> myLikedPosts;

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private CircleImageView imageProfile;
    private ImageView options;
    private TextView posts;
    private TextView name;
    private TextView bio;
    private TextView username;
    private LinearLayout postCount;

    private ImageView myPictures;
    private ImageView likedPictures;

    private Button editProfile;

    private FirebaseUser fUser;

    String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");
        if (data.equals("none")) {
            profileId = fUser.getUid();
        } else {
            profileId = data;
            getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().clear().apply();
        }

        imageProfile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        name = view.findViewById(R.id.name);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        myPictures = view.findViewById(R.id.my_pictures);
        likedPictures = view.findViewById(R.id.saved_pictures);
        editProfile = view.findViewById(R.id.edit_profile);
        postCount = view.findViewById(R.id.post_count);

        recyclerView = view.findViewById(R.id.recycler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPhotoList);
        recyclerView.setAdapter(photoAdapter);

        recyclerViewLiked = view.findViewById(R.id.recycler_view_liked);
        recyclerViewLiked.setHasFixedSize(true);
        recyclerViewLiked.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myLikedPosts = new ArrayList<>();
        postAdapterLiked = new PhotoAdapter(getContext(), myLikedPosts);
        recyclerViewLiked.setAdapter(postAdapterLiked);

        userInfo();
        getPostCount();
        myPhotos();
        getLikedPosts();

        if (profileId.equals(fUser.getUid())) {
            editProfile.setVisibility(View.VISIBLE);
        } else {
            editProfile.setVisibility(View.INVISIBLE);
            postCount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnText = editProfile.getText().toString();

                if (btnText.equals("EDIT PROFILE")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }
            }
        });

        recyclerView.setVisibility(View.VISIBLE);
        recyclerViewLiked.setVisibility(View.GONE);

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewLiked.setVisibility(View.GONE);
            }
        });

        likedPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerViewLiked.setVisibility(View.VISIBLE);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OptionsActivity.class));
            }
        });

        return view;
    }

    private void getLikedPosts() {

        final List<String> likedIds = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("LikedByUser").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myLikedPosts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    likedIds.add(snapshot.getKey());
                }

                FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                        myLikedPosts.clear();

                        for (DataSnapshot snapshot1 : dataSnapshot1.getChildren()) {
                            Post post = snapshot1.getValue(Post.class);

                            for (String id : likedIds) {
                                if (post.getPostid().equals(id)) {
                                    myLikedPosts.add(post);
                                }
                            }
                        }

                        postAdapterLiked.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void myPhotos() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPhotoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) {
                        myPhotoList.add(post);
                    }
                }

                Collections.reverse(myPhotoList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getPostCount() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) counter ++;
                }

                posts.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void userInfo() {

        Context context;

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Picasso.get().load(user.getImageurl()).into(imageProfile);
                username.setText(user.getUsername());
                name.setText(user.getName());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void onStop() {
        super.onStop();
        final String TAG = "MyActivity";
        Log.i(TAG,"On Stop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final String TAG = "MyActivity";
        Log.i(TAG, "On Destroy View");
    }
}