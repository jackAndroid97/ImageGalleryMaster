package com.rintu.multyimagepicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.DeleteIconEvent;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.FlipHorizontallyEvent;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.xiaopo.flying.sticker.ZoomIconEvent;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.reactivex.functions.Consumer;

public class MainActivity2 extends AppCompatActivity {
    ImageView image,imageView;
    Button add;
    RecyclerView list;
    private static final int PICK_IMAGE_CODE=0;
   // private UriAdapter mAdapter;
    ViewPager imageSwitcher;
    List<Uri> imageUri;
    String ext="";
    int pos,random;
    EditText caption;
    BottomSheetDialog bottomSheetColour;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        image=findViewById(R.id.img);
        add=findViewById(R.id.add);


        imageSwitcher=findViewById(R.id.imageSwitch);
        imageUri=new ArrayList<>();
        list=findViewById(R.id.list);
        caption=findViewById(R.id.caption);
        final LinearLayoutManager gridLayoutManager2= new LinearLayoutManager(MainActivity2.this,LinearLayoutManager.HORIZONTAL,false);
        list.setHasFixedSize(true);
        list.setLayoutManager(gridLayoutManager2);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxPermissions rxPermissions = new RxPermissions(MainActivity2.this);
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    Matisse.from(MainActivity2.this)
                                            .choose(MimeType.ofAll(), false)
                                            .countable(true)
                                            .capture(false)
                                            .maxSelectable(9)
                                            .gridExpectedSize(
                                                    getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .thumbnailScale(0.85f)
                                            .imageEngine(new GlideEngine())
                                            .setOnSelectedListener((uriList, pathList) -> {
                                                Log.e("onSelected", "onSelected: pathList=" + pathList);
                                            })
                                            .showSingleMediaType(true)
                                            .originalEnable(true)
                                            .maxOriginalSize(10)
                                            .autoHideToolbarOnSingleTap(true)
                                            .setOnCheckedListener(isChecked -> {
                                                Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                                            })
                                            .forResult(PICK_IMAGE_CODE);
                                } else {
                                    Toast.makeText(MainActivity2.this, "Permission denied", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, Throwable::printStackTrace);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
        add.setVisibility(View.GONE);
            //mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
            imageUri = Matisse.obtainResult(data);
            ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity2.this,Matisse.obtainResult(data));
            imageSwitcher.setAdapter(viewPagerAdapter);

            ListAdapter listAdapter=new ListAdapter(MainActivity2.this,Matisse.obtainResult(data));
            list.setAdapter(listAdapter);
            caption.setVisibility(View.VISIBLE);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageUri.remove(pos);
                imageUri.add(0,resultUri);
                add.setVisibility(View.GONE);
                ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity2.this,imageUri);
                imageSwitcher.setAdapter(viewPagerAdapter);

                ListAdapter listAdapter=new ListAdapter(MainActivity2.this,imageUri);
                list.setAdapter(listAdapter);
                caption.setVisibility(View.VISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private Context context;
        //  private String[] imageUrls;
       List<Uri> imageUri;

        public ViewPagerAdapter(Context context, List<Uri> imageUri) {
            this.context = context;
            this.imageUri = imageUri;

        }

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }


        @Override
        public int getCount() {
            return imageUri.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.custom_viewpager, null);

            //mapping
            imageView = (ImageView) view.findViewById(R.id.imageView);
            final ImageView crop = (ImageView) view.findViewById(R.id.crop);
            final ImageView play = (ImageView) view.findViewById(R.id.play);
            final ImageView colour_pallet = (ImageView) view.findViewById(R.id.colour);
            final ImageView undo = (ImageView) view.findViewById(R.id.undo);
            final ImageView sticker = (ImageView) view.findViewById(R.id.sticker);
            final TextView save = (TextView) view.findViewById(R.id.done);
            final LinearLayout item = (LinearLayout) view.findViewById(R.id.item);

            final ImageView draw = (ImageView) view.findViewById(R.id.draw);
            final FrameLayout layout = (FrameLayout) view.findViewById(R.id.layout);
            final DrawingView mDrawingView=(DrawingView)view.findViewById(R.id.img_screenshot);

           crop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pos=position;

                   /* //convert bitmap to file
                    File filesDir = context.getFilesDir();
                    File imageFile = new File(filesDir, "test" + ".jpg");

                    OutputStream os;
                    try {
                        os = new FileOutputStream(imageFile);
                        imageUri.get(position).compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                    }
                    //convert file to uri
                    Uri yourUri = Uri.fromFile(imageFile);*/

                    //send image for cropping
                    CropImage.activity(imageUri.get(position))
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropMenuCropButtonTitle("Done")
                            .setActivityTitle("Crop Image")
                            .setFixAspectRatio(true)
                            .start(MainActivity2.this);

                }
            });
             draw.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    //chane tint colour
                    pos=position;
                    draw.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                    layout.setVisibility(View.VISIBLE);
                    colour_pallet.setVisibility(View.VISIBLE);
                    undo.setVisibility(View.VISIBLE);
                    save.setVisibility(View.VISIBLE);
                    crop.setVisibility(View.GONE);
                    sticker.setVisibility(View.GONE);

                    //set image for drawing
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity2.this.getContentResolver(), imageUri.get(position));
                        mDrawingView.loadImage(bitmap);
                        mDrawingView.initializePen();
                        mDrawingView.setPenSize(15);
                        mDrawingView.setPenColor(getColor(R.color.colorAccent));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }




                }
            });
            //undo drawing
            undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawingView.undo();
                }
            });
            //choose colour
            colour_pallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    colour_pallet.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);

                    bottomSheetColour = new BottomSheetDialog(MainActivity2.this);
                    final View parentview = (View) getLayoutInflater().inflate(R.layout.colour_bottom,null);
                    final CardView green=parentview.findViewById(R.id.green);
                    final CardView blue=parentview.findViewById(R.id.blue);
                    final CardView red=parentview.findViewById(R.id.red);

                    green.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            draw.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                            colour_pallet.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                            mDrawingView.setPenColor(getColor(R.color.colorAccent));
                            bottomSheetColour.dismiss();
                        }
                    });

                    blue.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            draw.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                            colour_pallet.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                            mDrawingView.setPenColor(getColor(R.color.colorPrimary));
                            bottomSheetColour.dismiss();
                        }
                    });

                    red.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            draw.setColorFilter(ContextCompat.getColor(context, R.color.colorRed), android.graphics.PorterDuff.Mode.MULTIPLY);
                            colour_pallet.setColorFilter(ContextCompat.getColor(context, R.color.colorRed), android.graphics.PorterDuff.Mode.MULTIPLY);
                            mDrawingView.setPenColor(getColor(R.color.colorRed));
                            bottomSheetColour.dismiss();
                        }
                    });

                    bottomSheetColour.setContentView(parentview);
                    bottomSheetColour.show();
                }
            });
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int min =100000000;
                    final int max =999999999;
                    random = new Random().nextInt((max - min) + 1) + min;

                    //convert bitmap to file
                    File filesDir = context.getFilesDir();
                    File imageFile = new File(filesDir, String.valueOf(random) + ".jpg");

                    OutputStream os;
                    try {
                        os = new FileOutputStream(imageFile);
                        mDrawingView.getImageBitmap().compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                    }
                    //convert file to uri
                    Uri yourUri = Uri.fromFile(imageFile);
                    imageUri.remove(pos);
                    imageUri.add(0,yourUri);
                    ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity2.this,imageUri);
                    imageSwitcher.setAdapter(viewPagerAdapter);

                    ListAdapter listAdapter=new ListAdapter(MainActivity2.this,imageUri);
                    list.setAdapter(listAdapter);

                    caption.setVisibility(View.VISIBLE);
                }
            });
            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            ext = mime.getExtensionFromMimeType(cR.getType(imageUri.get(position)));
           if(ext==null){
               play.setVisibility(View.GONE);
               item.setVisibility(View.VISIBLE);
           }else{
               if(ext.equals("mp4")){
                   play.setVisibility(View.VISIBLE);
                   item.setVisibility(View.GONE);
               }else{
                   play.setVisibility(View.GONE);
                   item.setVisibility(View.VISIBLE);
               }
           }




            Glide.with(context)
                    .load(imageUri.get(position))
                    .into(imageView);
            container.addView(view);

            return view;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }


    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Context context;
        List<Uri> ListModel;
        int index = -1;

        public ListAdapter(Context context, List<Uri> ListModel) {
            this.context = context;
            this.ListModel = ListModel;

        }


        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list, parent, false);
            return new ListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ListAdapter.ViewHolder viewHolder, final int position) {
            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            ext = mime.getExtensionFromMimeType(cR.getType(ListModel.get(position)));
            if(ext==null){
                viewHolder.play.setVisibility(View.GONE);
            }else{
                if(ext.equals("mp4")){
                    viewHolder.play.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.play.setVisibility(View.GONE);
                }
            }

            Glide.with(context)
                    .load(ListModel.get(position))
                    .into(viewHolder.imageView);

            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    index = position;
                    notifyDataSetChanged();
                    imageSwitcher.setCurrentItem(position, true);
                }
            });

            if(imageSwitcher.getCurrentItem()==position){
                viewHolder.border.setVisibility(View.VISIBLE);
            }else{
                if(index==position){
                    viewHolder.border.setVisibility(View.VISIBLE);
                }
                else{
                    viewHolder.border.setVisibility(View.GONE);
                }
            }
            //  viewHolder.border.setVisibility(viewpagerPos);
        }

        @Override
        public int getItemCount() {
            return ListModel.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {


            ImageView imageView;
            ImageView border;
            ImageView play;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView=itemView.findViewById(R.id.img);
                border=itemView.findViewById(R.id.border);
                play = (ImageView) itemView.findViewById(R.id.play);




            }
        }
    }



    /*private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }
    }*/

}