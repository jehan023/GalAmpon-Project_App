package Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stejeetech.galampon.MainActivity;
import com.stejeetech.galampon.R;

import java.util.ArrayList;
import java.util.List;

import Adapter.NearbyAdapter;
import Model.Post;

public class NearbyFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private RecyclerView recyclerViewNearby;
    private NearbyAdapter nearbyAdapter;
    private List<Post> nearbyList;
    private FirebaseUser firebaseUser;

    private Double userLatitude;
    private Double userLongitude;
    private double range;

    private ImageView gps;
    private TextView locationName;

    private Spinner spinner;
    private static final String[] paths = {"1 kilometer","2 kilometers", "3 kilometers","5 kilometers","8 kilometers","10 kilometers"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        locationName = view.findViewById(R.id.locationInfo);
        MainActivity main = (MainActivity) getActivity();
        locationName.setText(main.getCurrentLocationName());
        userLatitude = main.getCurrentLatitude();
        userLongitude = main.getCurrentLongitude();

        recyclerViewNearby = view.findViewById(R.id.recycler_view_nearby);
        recyclerViewNearby.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewNearby.setLayoutManager(linearLayoutManager);
        nearbyList = new ArrayList<>();
        nearbyAdapter = new NearbyAdapter(getContext(), nearbyList);
        recyclerViewNearby.setAdapter(nearbyAdapter);

        spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        gps = view.findViewById(R.id.gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.checkPermission();
                Toast.makeText(getContext(), "Current Location Fetching.", Toast.LENGTH_SHORT).show();
                locationName.setText(main.getCurrentLocationName());
            }
        });

        sortNearbyPost();

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 1:
                range = 2.0;
                sortNearbyPost();
                break;
            case 2:
                range = 3.0;
                sortNearbyPost();
                break;
            case 3:
                range = 5.0;
                sortNearbyPost();
                break;
            case 4:
                range = 8.0;
                sortNearbyPost();
                break;
            case 5:
                range = 10.0;
                sortNearbyPost();
                break;
            case 0:
            default:
                range = 1.0;
                sortNearbyPost();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }
    
    private void sortNearbyPost() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nearbyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if(!post.getPublisher().equals(firebaseUser.getUid())){
                        if(locationName != null && userLatitude != null && userLongitude != null){
                            if(CalculationByDistance(userLatitude,userLongitude,post.getPostlatitude(),post.getPostlongitude()) <= range){
                                nearbyList.add(post);
                            }
                        } else {
                            getFragmentManager().beginTransaction().detach(NearbyFragment.this).commit();
                            getFragmentManager().beginTransaction().attach(new NearbyFragment()).commit();
                        }
                    }
                }
                nearbyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    //On Cancelled code
            }
        });
    }

    public double CalculationByDistance(double userLatitude, double userLongitude,
                                        double postLat, double postLong){
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(postLat - userLatitude);
        double dLon = toRadians(postLong - userLongitude);
        userLatitude = toRadians(userLatitude);
        postLat = toRadians(postLat);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(userLatitude) * Math.cos(postLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (double) R * c;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI/180);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(">>> NearbyFragment","On Stop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(">>> NearbyFragment", "On Destroy View");
    }
}