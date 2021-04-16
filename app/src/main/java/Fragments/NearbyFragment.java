package Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.stejeetech.galampon.R;

public class NearbyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby, container, false);
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