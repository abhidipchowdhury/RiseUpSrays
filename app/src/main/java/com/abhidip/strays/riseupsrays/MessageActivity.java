package com.abhidip.strays.riseupsrays;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhidip.strays.model.ChatMessage;
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
    Toolbar messageActivityToolbar;

    private ImageView sendIcon, photoContent, cameraIcon, galleryIcon;
    private EditText textContent;
    private StorageReference mStorageRef;
    private Uri uri;
    private int count = 0;

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
        super.onActivityResult(requestCode, resultCode, data);
        String filePath = "sdcard/rise-up-strays/" + System.currentTimeMillis()+".jpg";
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                photoContent.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photoContent.setImageDrawable(Drawable.createFromPath(filePath));
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
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //File file = getFile();
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {

                }
                if (photoFile!= null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    };


    private File getFile() {
        File folder = new File("sdcard/rise-up-strays");
        if (!folder.exists()) {
            folder.mkdir();
        }

        File image = new File(folder, System.currentTimeMillis()+".jpg");
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

        }
    };


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

}