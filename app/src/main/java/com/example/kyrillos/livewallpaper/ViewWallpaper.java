package com.example.kyrillos.livewallpaper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kyrillos.livewallpaper.Common.Common;
import com.example.kyrillos.livewallpaper.Database.DataSource.RecentRepository;
import com.example.kyrillos.livewallpaper.Database.LocalDatabase.LocalDatabase;
import com.example.kyrillos.livewallpaper.Database.LocalDatabase.RecentsDataSource;
import com.example.kyrillos.livewallpaper.Database.Recents;
import com.example.kyrillos.livewallpaper.Helper.SaveImageHelper;
import com.example.kyrillos.livewallpaper.Model.WallpaperItem;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class ViewWallpaper extends AppCompatActivity {
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton floatingActionButton, floatdownload;
    ImageView imageView;
    CoordinatorLayout rootLayout;

    FloatingActionMenu mainFloating;
    com.github.clans.fab.FloatingActionButton fbShare;

    //Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //Room DB
    CompositeDisposable compositeDisposable;
    RecentRepository recentRepository;


    private Target facebookConvertBitmap = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (shareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                shareDialog.show(sharePhotoContent);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case Common.PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    AlertDialog dialog = new  SpotsDialog.Builder().setContext(ViewWallpaper.this).build();
                    dialog.show();
                    dialog.setMessage("Please Wait...");
                    String fileName = UUID.randomUUID().toString() + ".png";
                    Picasso.with(getBaseContext())
                            .load(Common.select_background.getImageUrl())
                            .into(new SaveImageHelper(getBaseContext(),dialog,getApplicationContext().getContentResolver(),
                                    fileName,"Live Wallpaper"));
                }else {
                    showMessage("You need to allow requested permissions");
                }
            }
            break;
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                    wallpaperManager.setBitmap(bitmap);
                Snackbar.make(rootLayout,"Wallpaper Set",Snackbar.LENGTH_LONG).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallpaper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);


        compositeDisposable = new CompositeDisposable();
        LocalDatabase database = LocalDatabase.getInstance(this);
        recentRepository = RecentRepository.getInstance(RecentsDataSource.getInstance(database.recentsDAO()));

        rootLayout = findViewById(R.id.rootLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);

        collapsingToolbarLayout.setTitle(Common.CATEGORY_SELECTED);
        imageView = findViewById(R.id.imageThumb);
        Picasso.with(this)
                .load(Common.select_background.getImageUrl())
                .into(imageView);

        mainFloating = findViewById(R.id.menu);
        fbShare = findViewById(R.id.menu_facebook);
        fbShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create Callback
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        showMessage("Share Successful!");
                    }

                    @Override
                    public void onCancel() {
                        showMessage("Share Canceled!");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        showMessage(""+ error.getMessage());
                    }
                });

                Picasso.with(getBaseContext())
                        .load(Common.select_background.getImageUrl())
                        .into(facebookConvertBitmap);
            }
        });



        addToRecents();

        floatingActionButton = findViewById(R.id.fabWallpaper);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.with(getBaseContext())
                        .load(Common.select_background.getImageUrl())
                        .into(target);

            }
        });

        floatdownload = findViewById(R.id.fabDownload);
        floatdownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check Permission
                if (ActivityCompat.checkSelfPermission(ViewWallpaper.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},Common.PERMISSION_REQUEST_CODE);
                }else {
                    AlertDialog dialog = new  SpotsDialog.Builder().setContext(ViewWallpaper.this).build();                    dialog.show();
                    dialog.setMessage("Please Wait...");
                    String fileName = UUID.randomUUID().toString() + ".png";
                    Picasso.with(getBaseContext())
                            .load(Common.select_background.getImageUrl())
                            .into(new SaveImageHelper(getBaseContext(),dialog,getApplicationContext().getContentResolver(),
                                    fileName,"Live Wallpaper"));
                }
            }
        });

        increaseViewCount();

    }

    private void increaseViewCount() {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER)
                .child(Common.select_background_key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("viewCount")){
                            WallpaperItem wallpaperItem = dataSnapshot.getValue(WallpaperItem.class);
                            long count = wallpaperItem.getViewCount() +1;

                            // Update
                            Map<String,Object> updateView = new HashMap<>();
                            updateView.put("viewCount", count);
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.STR_WALLPAPER)
                                    .child(Common.select_background_key)
                                    .updateChildren(updateView)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage("Cannot Update View Count");
                                }
                            });
                        }else  {// if view count isn't set then default will be 1
                            // Update
                            Map<String,Object> updateView = new HashMap<>();
                            updateView.put("viewCount", Long.valueOf(1));
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.STR_WALLPAPER)
                                    .child(Common.select_background_key)
                                    .updateChildren(updateView)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage("Cannot set default View Count");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void addToRecents() {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                Recents recents = new Recents(
                        Common.select_background.getImageUrl()
                        ,Common.select_background.getCategoryId(),
                        String.valueOf(System.currentTimeMillis())
                                ,Common.select_background_key
                );
                recentRepository.insertRecents(recents);
                emitter.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        showMessage("Error" + throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        Picasso.with(getBaseContext())
                .cancelRequest(target);
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
