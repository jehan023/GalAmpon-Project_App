package Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.stejeetech.galampon.R;

import java.util.List;

import Adapter.NearbyAdapter;
import Model.Post;

public class NearbyFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private NearbyAdapter nearbyAdapter;
    private List<Post> postList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


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