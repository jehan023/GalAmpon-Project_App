package Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stejeetech.galampon.R;

import java.util.ArrayList;
import java.util.List;

import Adapter.NearbyAdapter;
import Model.Post;

public class NearbyFragment extends Fragment {

    private RecyclerView recyclerViewNearby;
    private NearbyAdapter nearbyAdapter;
    private List<Post> postList;

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
        postList = new ArrayList<>();
        nearbyAdapter = new NearbyAdapter(getContext(), postList);
        recyclerViewNearby.setAdapter(nearbyAdapter);


        return view;
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