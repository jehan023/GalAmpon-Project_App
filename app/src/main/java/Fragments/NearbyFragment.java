package Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stejeetech.galampon.R;

import java.util.ArrayList;
import java.util.List;

import Adapter.NearbyAdapter;
import Model.Post;

public class NearbyFragment extends Fragment {

    private RecyclerView recyclerViewNearby;
    private NearbyAdapter nearbyAdapter;
    private List<Post> nearbyList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        recyclerViewNearby = view.findViewById(R.id.recycler_view_nearby);
        recyclerViewNearby.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewNearby.setLayoutManager(linearLayoutManager);
        nearbyList = new ArrayList<>();
        nearbyAdapter = new NearbyAdapter(getContext(), nearbyList);
        recyclerViewNearby.setAdapter(nearbyAdapter);

        sortNearbyPost();


        return view;
    }

    private void sortNearbyPost() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nearbyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    nearbyList.add(post);
                }
                nearbyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            ;
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("NEARBY Fragment","On Stop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("NEARBY Fragment", "On Destroy View");
    }
}