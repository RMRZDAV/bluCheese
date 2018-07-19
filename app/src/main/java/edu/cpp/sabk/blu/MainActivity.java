package edu.cpp.sabk.blu;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static int IMG_RESULT = 1;
    private FirebaseAuth mAuth;
    String ImageDecode;
    int iNum = 0;
    ImageView imageViewLoad;
    Intent intent;
    String[] FILE;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        String title = getIntent().getStringExtra("Title");
        TextView place = findViewById(R.id.placeTitleMain);
        place.setText(title);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String placeId = place.getText().toString();
        String userId = user.getUid();

        StorageReference storageRef = storage.getReference();
        StorageReference textRef = storageRef.child("files/"+userId+"/"+placeId+".txt");

        final long ONE_MEGABYTE = 1024 * 1024;
        textRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                EditText placeInfo = findViewById(R.id.editText);
                String myString = new String(bytes);
                placeInfo.setText(myString);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


        StorageReference imagesRef = storageRef.child("images/"+userId+"/"+placeId);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear);
        final long SIX_MEGABYTE = 6 * 1024 * 1024;
        for(int index = 0; index < 20; index++) {
            StorageReference foodRef = storageRef.child("images/"+userId+"/"+placeId+"/"+index+".jpg");
            foodRef.getBytes(SIX_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    final Bitmap selectedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setId(iNum);
                    imageView.setImageBitmap(selectedImage);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    layout.addView(imageView);
                    iNum++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

    }

    public void onClickBack(View v)
    {
        Intent myIntent = new Intent(this, MapsActivity.class);
        startActivity(myIntent);
    }

    public void onClickMenu(View v)
    {
        ImageButton menuBtn = findViewById(R.id.imageButtonMenu);
        PopupMenu dropDownMenu = new PopupMenu(getApplicationContext(), menuBtn);
        dropDownMenu.getMenuInflater().inflate(R.menu.drop_down_menu, dropDownMenu.getMenu());
        dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item2:
                        //save info
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        TextView place = findViewById(R.id.placeTitleMain);
                        EditText placeInfo = findViewById(R.id.editText);
                        String placeId = place.getText().toString();
                        String userId = user.getUid();
                        StorageReference storageRef = storage.getReference();
                        StorageReference filesRef = storageRef.child("files/"+userId);
                        StorageReference textRef = storageRef.child("files/"+userId+"/"+placeId+".txt");

                        String mydata = placeInfo.getText().toString();
                        byte[] mybytes = mydata.getBytes();
                        UploadTask uploadTask = textRef.putBytes(mybytes);
                        return true;
                    case R.id.item1:
                        //add Image
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, IMG_RESULT);
                        return true;
                    case R.id.item3:
                        FirebaseAuth.getInstance().signOut();
                        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(myIntent);
                        return true;
                    default:
                        //do nothing
                }
                return true;
            }
        });
        dropDownMenu.show();
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                final LinearLayout layout = (LinearLayout) findViewById(R.id.linear);

                ImageView imageViewLoad = new ImageView(MainActivity.this);
                imageViewLoad.setId(iNum);
                imageViewLoad.setImageBitmap(selectedImage);
                layout.addView(imageViewLoad);

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                TextView place = findViewById(R.id.placeTitleMain);
                String placeId = place.getText().toString();
                String userId = user.getUid();
                StorageReference storageRef = storage.getReference();
                StorageReference imagesRef = storageRef.child("images/"+userId+"/"+placeId);
                StorageReference foodRef = storageRef.child("images/"+userId+"/"+placeId+"/"+iNum+".jpg");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] mydata = baos.toByteArray();

                UploadTask uploadTask = foodRef.putBytes(mydata);
                iNum++;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //Toast.makeText(PostImage.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            //Toast.makeText(PostImage.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
}
