package com.example.hp.pchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //listing all the fields that we have in our layout (i.e display_name  , status , etc)
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;

    private StorageReference mImageStorage;

    private static final int GALLERY_PICK = 1;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName=(TextView) findViewById(R.id.settings_name);
        mStatus=(TextView) findViewById(R.id.settings_status);

        mStatusBtn=(Button) findViewById(R.id.settings_status_btn);
        mImageBtn=(Button) findViewById(R.id.settings_image_button);

        mImageStorage = FirebaseStorage.getInstance().getReference(); //it is pointing towards the root of firebase storage

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid); //we are pointing to the uid

        mUserDatabase.keepSynced(true); //here it can only keep those data synced which are retrieved from Firebase , NOTE : from firebase we are only retrieving imageuri so the image loading will still take time .. so we need offline capabilities of Picasso .

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //this method used when we retrieve the data or the data is changed
                //datasnapshot contains all the fields of data(name ,  image , status , etc) that we have uploaded
                String name=dataSnapshot.child("name").getValue().toString();
                final String image= dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                if(!image.equals("default")){
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage); //here the job of placeholder is that it is used to show the default avatar until our user image loads
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //this method used to handle the errors
            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = mStatus.getText().toString();
                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);
                startActivity(status_intent);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() { //for seelcting the image from gallery
            @Override
            public void onClick(View view) {
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery_intent , "SELECT IMAGE"),GALLERY_PICK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== GALLERY_PICK && resultCode== RESULT_OK){
            Uri imageUri= data.getData();

            CropImage.activity(imageUri).setAspectRatio(1,1).start(this); //to crop the image int the aspect ratio of 1:1

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image ...");
                mProgressDialog.setMessage("Please wait while the image is being uploaded !!!");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();


                Uri resultUri = result.getUri(); // we get out image uri stored in the resulturi

                final File thumb_filePath = new File(resultUri.getPath());

                String current_user_id = mCurrentUser.getUid();


                final Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();   //these following three lines helps to convert data to byte form
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG , 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg"); // we give our image a random name from a random string generator function that we have used below and filepath variable stores the path to the image stored in the firebase database

                final StorageReference thumb_filepath=  mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            //Toast.makeText(SettingsActivity.this, "Working",Toast.LENGTH_LONG).show();
                            final String download_url= task.getResult().getDownloadUrl().toString();//to download the URL of the address where the image is being stored in the firebase

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    //Task thumb_task = null;//= null;
                                    String thumb_downloadUrl= thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image" , download_url);
                                        update_hashMap.put("thumb_image" , thumb_downloadUrl);

                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Successfully Uploaded.",Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        });

                                    }
                                    else
                                    {
                                        Toast.makeText(SettingsActivity.this, "ERROR in uploading Thumbnail.",Toast.LENGTH_LONG).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });


                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "ERROR",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();

                        }
                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
