package com.example.mks.firebaseimguploadretrieve;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ImageView imageViewUp;
    private EditText etNameInput;
    private Uri imgUri;

    public static final String FB_STORAGE_PATH="image/";
    public static final String FB_DATABASE_PATH="image";
    public static final int REQUEST_CODE=1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef= FirebaseStorage.getInstance().getReference();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);

        imageViewUp= (ImageView) findViewById(R.id.imgViewUpload);
        etNameInput= (EditText) findViewById(R.id.etImageName);

    }
    public void btnBrowse_click(View view){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
       startActivityForResult(Intent.createChooser(intent,"Select Image"),REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK && data !=null && data.getData()!=null ){
           imgUri=data.getData();
            try{
                Bitmap bm= MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
                imageViewUp.setImageBitmap(bm);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getImageExt(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(contentResolver.getType(uri));

    }
    @SuppressWarnings("VisibleForTests")
    public void btnUpload_click(View view){
        if(imgUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setTitle("uploading image");
            dialog.show();
            // get the storeage refference
            StorageReference reference=mStorageRef.child(FB_STORAGE_PATH + System.currentTimeMillis() +"."+getImageExt(imgUri));
            // Adding file to refference
            reference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //dissmiss dialog when success
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),"image Uploaded",Toast.LENGTH_LONG).show();
                    //save to database
                    ImageUpload imageUpload=new ImageUpload(etNameInput.getText().toString(),taskSnapshot.getDownloadUrl().toString());

                    String uploaded=mDatabaseRef.getKey();
                    mDatabaseRef.child(uploaded).setValue(imageUpload);



                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();


                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // show dialog progress
                            double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                            dialog.setMessage("Uploaded"+(int)progress +"0");

                        }
                    });

        }else{
            Toast.makeText(getApplicationContext(),"Please select image",Toast.LENGTH_LONG).show();
        }


    }


}
