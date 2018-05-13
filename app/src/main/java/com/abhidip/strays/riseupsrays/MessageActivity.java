package com.abhidip.strays.riseupsrays;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhidip.strays.model.ChatMessage;
import com.abhidip.strays.util.GPSUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public final String APP_TAG = "RiseUpStrays";
    Toolbar messageActivityToolbar;

    private ImageView sendIcon, photoContent, cameraIcon, galleryIcon;
    private EditText textContent;
    private StorageReference mStorageRef;
    private Uri uri;
    private Bitmap bitmap;
    private int count = 0;

    // Util Class for GPS
    private GPSUtil gpsUtil;

    // Firebase real time database
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ChatMessage chatMessage;
    private String mCurrentPhotoPath;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageActivityToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(messageActivityToolbar);
        // toolbar1.setOverflowIcon(R.drawable.ic_format_list_bulleted );
        getSupportActionBar().setTitle("Write something...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            messageActivityToolbar.setElevation(10f);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Messages");
        chatMessage = new ChatMessage();
        sendIcon = (ImageView) findViewById(R.id.sendIcon);
        photoContent = (ImageView) findViewById(R.id.photoContent);
        cameraIcon = (ImageView) findViewById(R.id.cameraIcon);
        cameraIcon.setOnClickListener(cameraIconListener);
        galleryIcon = (ImageView) findViewById(R.id.galleryIcon);
        textContent = (EditText) findViewById(R.id.textContent);
        progressDialog = new ProgressDialog(this);

        galleryIcon.setOnClickListener(galleryIconListener);
        sendIcon.setOnClickListener(sendIconListener);
        count ++;
        gpsUtil = new GPSUtil(getApplicationContext());

    }

    /**
     * This method opens a new window with file chooser
     */
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(
                intent, "Select an image"), PICK_IMAGE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // If user selects a photo from gallery.
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                photoContent.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If the user has taken a new snap using the camera.
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            uri = Uri.fromFile(f);
            photoContent.setImageURI(uri);
            mediaScanIntent.setData(uri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    /**
     *
     * @param uri
     * @return returns the extension of the image file.
     */
    private String getFileExtension (Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private View.OnClickListener galleryIconListener = new View.OnClickListener() {
        public void onClick(View v) {
            showFileChooser();
        }
    };

    private View.OnClickListener cameraIconListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                }
            }
            requestPermission();
        }
    };

    void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CAMERA);
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        }
        takePicture();
    }

    void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.abhidip.strays.riseupsrays", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
            //takePictureIntent.putExtra("return-data", true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                Log.i("Camera", "G : " + grantResults[0]);
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(MessageActivity.this, "Storage Permission not granted..", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private View.OnClickListener sendIconListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (uri != null) {

                progressDialog.setTitle("Posting...");
                progressDialog.show();

                StorageReference riversRef = mStorageRef.child("useruploads/"+ System.currentTimeMillis()+"."+getFileExtension(uri));
                riversRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Location location = gpsUtil.getLocation();
                        if (location != null) {
                            chatMessage.setLatitude(location.getLatitude());
                            chatMessage.setLongitude(location.getLongitude());
                        }

                        chatMessage.setPhotoUrl(downloadUrl.toString());
                        chatMessage.setDescription(textContent.getText().toString());

                        // Storing the snippet in real time database
                        String uploadId = reference.push().getKey();
                        reference.child(uploadId).setValue(chatMessage);
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Posted Successfully", Toast.LENGTH_LONG);
                        // Redirect user to home page
                        Intent myIntent = new Intent(MessageActivity.this,HomeActivity.class);
                        startActivity(myIntent);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG);
                                exception.printStackTrace();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (taskSnapshot.getBytesTransferred() * 100.0)/taskSnapshot.getTotalByteCount();
                                progressDialog.setMessage(((int) progress) + "% Uploaded");
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                             @Override
                             public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                             }
                         });

            }
            else
                Toast.makeText(getApplicationContext(), "Select an image...", Toast.LENGTH_LONG);

        }
    };

}