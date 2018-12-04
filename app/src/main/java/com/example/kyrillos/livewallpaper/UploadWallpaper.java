package com.example.kyrillos.livewallpaper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kyrillos.livewallpaper.Common.Common;
import com.example.kyrillos.livewallpaper.Model.CategoryItem;
import com.example.kyrillos.livewallpaper.Model.WallpaperItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UploadWallpaper extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageReference;

    ImageView image_preview;
    Button btn_broweser,btn_upload;
    MaterialSpinner spinner;

    Map<String,String> spinnerData = new HashMap<>();



    private Uri filePath ;
    String categoryIdSelect="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_wallpaper);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        image_preview = findViewById(R.id.image_preview);
        btn_broweser = findViewById(R.id.btn_broweser);
        btn_upload = findViewById(R.id.btn_upload);
        spinner = findViewById(R.id.spinner);

        loadCategoryToSpinner();


        btn_broweser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedIndex() == 0) {
                    showMessage("Please choose category");
                }else {
                    upload();
                }
            }
        });

    }

    private void upload() {
        if (filePath!=null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference reference = storageReference.child(new StringBuilder("images/").append(UUID.randomUUID().toString())
                    .toString());

            reference.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            saveUrlToCategory(categoryIdSelect,taskSnapshot.getDownloadUrl().toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    showMessage("" + e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount() );
                    progressDialog.setMessage("Uploaded: " + (int)progress + "%");
                }
            });

        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveUrlToCategory(String categoryIdSelect, String imageUrl) {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER)
                .push()
                .setValue(new WallpaperItem(imageUrl,categoryIdSelect))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("Success!");
                        finish();
                    }
                });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"Select Picture: "),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                image_preview.setImageBitmap(bitmap);
                btn_upload.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadCategoryToSpinner() {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_CATEGORY_BACKGROUND)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot :dataSnapshot.getChildren()){
                            CategoryItem item = snapshot.getValue(CategoryItem.class);
                            String Key = snapshot.getKey();
                            spinnerData.put(Key,item.getName());
                        }
                        Object[] valueArray = spinnerData.values().toArray();
                        List<Object> valueList = new ArrayList<>();
                        valueList.add(0,"Categories");
                        valueList.addAll(Arrays.asList(valueArray));
                        spinner.setItems(valueList);
                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                                Object[] keyArray = spinnerData.keySet().toArray();
                                List<Object> keyList = new ArrayList<>();
                                keyList.add(0,"Category_Key");
                                keyList.addAll(Arrays.asList(keyArray));
                                categoryIdSelect = keyList.get(position).toString();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
