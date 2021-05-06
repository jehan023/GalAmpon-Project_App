package com.stejeetech.galampon;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private String imageUrl;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int PLACE_PICKER_REQUEST = 1;

    public Double postLatitude;
    public Double postLongitude;
    public String postLocation;

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    private TextView addImage;
    private TextView txtlocation;
    private Button selectLocation;

    private TextView txtLatitude;
    private TextView txtLongitude;

    SocialAutoCompleteTextView description;
    String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        txtlocation = findViewById(R.id.location);
        selectLocation = findViewById(R.id.btnLocation);
        addImage = findViewById(R.id.addImage);

        txtLatitude = findViewById(R.id.postLatitude);
        txtLongitude = findViewById(R.id.postLongitude);

        //selectImage();

        selectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Choose location of the post
                if(!isConnected(PostActivity.this)){
                    buildDialog(PostActivity.this).show();
                } else {
                    Intent intent = new Intent(PostActivity.this, GoogleMapsActivity.class);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected(PostActivity.this)){
                    Toast.makeText(getApplicationContext(), "Unavailable, please connect on network.", Toast.LENGTH_SHORT).show();
                } else {
                    if (imageUri != null) {
                        upload();
                    } else {
                        Toast.makeText(PostActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                        selectImage();
                    }
                }
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }
    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setIcon(R.drawable.ic_nowifi);
        builder.setTitle("No Connection!");

        builder.setMessage("Unable to load map.");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        return builder;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("PostActivity", "FINISH");
        finish();
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void upload() {
        if ((!TextUtils.isEmpty(description.getText().toString()))   && (!TextUtils.isEmpty(txtlocation.getText().toString()))) {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading");
            pd.show();

            if (imageUri != null){
                final StorageReference filePath = FirebaseStorage.getInstance()
                        .getReference("Posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

                StorageTask uploadTask = filePath.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri downloadUri = task.getResult();
                        imageUrl = downloadUri.toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        String postId = ref.push().getKey();

                        HashMap<String , Object> map = new HashMap<>();
                        map.put("postid" , postId);
                        map.put("imageurl" , imageUrl);
                        map.put("description" , description.getText().toString());
                        map.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                        map.put("postlocation", postLocation);
                        map.put("postlatitude", postLatitude);
                        map.put("postlongitude", postLongitude);
                        map.put("date", currentDate);

                        ref.child(postId).setValue(map);

                        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                        List<String> hashTags = description.getHashtags();
                        if (!hashTags.isEmpty()){
                            for (String tag : hashTags){
                                map.clear();

                                map.put("tag" , tag.toLowerCase());
                                map.put("postid" , postId);

                                mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                            }
                        }

                        pd.dismiss();
                        startActivity(new Intent(PostActivity.this , MainActivity.class));
                        Toast.makeText(PostActivity.this, "Post Successful.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "No Image was selected!", Toast.LENGTH_SHORT).show();
            }
        } else{
            Toast.makeText(PostActivity.this, "Empty field not allowed!", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                if (data != null){
                    postLocation = data.getStringExtra("pickedLocation");
                    postLatitude = data.getDoubleExtra("pickedLat", 0.00000);
                    postLongitude = data.getDoubleExtra("pickedLong", 0.00000);

                    txtlocation.setText(postLocation);
                    txtLatitude.setText("Lat: " + postLatitude.toString());
                    txtLongitude.setText("Lng: " + postLongitude.toString());

                }
            }
        }

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageAdded.setImageBitmap(bitmap);
                addImage.setText("Choose other image");
            }
            catch (IOException e)
            {
                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hashtagAdapter.add(new Hashtag(snapshot.getKey() , (int) snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        description.setHashtagAdapter(hashtagAdapter);
    }
}