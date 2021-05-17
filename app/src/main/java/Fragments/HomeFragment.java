package Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stejeetech.galampon.R;

import java.util.ArrayList;

import Adapter.PostAdapter;
import Model.Post;

public class HomeFragment extends Fragment {

    public ImageView notificationBell, notificationDot;

    public RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    public int newNotifCount;
    public int oldNotifCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        notificationBell = view.findViewById(R.id.notification_bell);
        notificationDot = view.findViewById(R.id.notification_dot);
        oldNotifCount = getContext().getSharedPreferences("NOTIFICATION", Context.MODE_PRIVATE).getInt("oldNotifCount", 0);

        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerViewPosts.setAdapter(postAdapter);

        readPost();
        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newNotifCount = (int)snapshot.getChildrenCount();
                Log.i("OLD NOTIF COUNT", String.valueOf(oldNotifCount));
                Log.i("NEW NOTIF COUNT", String.valueOf(newNotifCount));
                if (newNotifCount > oldNotifCount){
                    notificationDot.setVisibility(View.VISIBLE);
                } else{
                    notificationDot.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        notificationBell.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                notificationDot.setVisibility(View.GONE);
                oldNotifCount = newNotifCount;
                getContext().getSharedPreferences("NOTIFICATION", Context.MODE_PRIVATE).edit().putInt("oldNotifCount", oldNotifCount).apply();
                Log.i("OLD NOTIF COUNT", String.valueOf(oldNotifCount));
                Log.i("NEW NOTIF COUNT", String.valueOf(newNotifCount));

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, new NotificationFragment());
                fragmentTransaction.addToBackStack(String.valueOf(new NotificationFragment())).commit();
            }
        });

        return view;
    }

    private void readPost() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().getSharedPreferences("NOTIFICATION", Context.MODE_PRIVATE).edit().putInt("oldNotifCount", oldNotifCount).apply();
    }
}