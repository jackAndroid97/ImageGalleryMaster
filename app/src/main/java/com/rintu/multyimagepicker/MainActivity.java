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
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
        ViewPager imageSwitcher;
        Button pre,take,next;
        ArrayList<Bitmap> imageUri;

        RecyclerView list;
        private static final int PICK_IMAGE_CODE=0;
        int position=0;
        int viewpagerPos=0;
    BottomSheetDialog bottomSheetColour;
    ImageView imageView;
    Uri uri;
    int pos;
    StickerView stickerView;
    public static final int PERM_RQST_CODE = 110;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSwitcher=findViewById(R.id.imageSwitch);
        list=findViewById(R.id.list);
        pre=findViewById(R.id.pre);
        take=findViewById(R.id.take);
        next=findViewById(R.id.next);
        imageUri=new ArrayList<>();
        final LinearLayoutManager gridLayoutManager2= new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false);
        list.setHasFixedSize(true);
        list.setLayoutManager(gridLayoutManager2);


        imageSwitcher.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                // Check if this is the page you want.
               viewpagerPos=position;
            }
        });

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageIntent();
            }
        });


    }
    private void pickImageIntent(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select images"),PICK_IMAGE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_CODE){
            if(resultCode== Activity.RESULT_OK){
                //multipal
                if(data.getClipData() !=null){
                    int count=data.getClipData().getItemCount();
                    for(int i=0; i<count; i++){
                       uri=data.getClipData().getItemAt(i).getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), uri);
                            imageUri.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                    }
                    take.setVisibility(View.GONE);
                    ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity.this,imageUri);
                    imageSwitcher.setAdapter(viewPagerAdapter);
                    ListAdapter listAdapter=new ListAdapter(MainActivity.this,imageUri);
                    list.setAdapter(listAdapter);
                   // imageSwitcher.setImageURI(imageUri.get(0));
                    //position=0;
                }
                //single
                else{

                    uri=data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), uri);
                        imageUri.add(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    take.setVisibility(View.GONE);
                    ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity.this,imageUri);
                    imageSwitcher.setAdapter(viewPagerAdapter);

                    ListAdapter listAdapter=new ListAdapter(MainActivity.this,imageUri);
                    list.setAdapter(listAdapter);
                    //imageSwitcher.setImageURI(imageUri.get(0));
                   // position=0;

                }
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageUri.remove(pos);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), resultUri);
                    imageUri.add(0,bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                take.setVisibility(View.GONE);
                ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity.this,imageUri);
                imageSwitcher.setAdapter(viewPagerAdapter);

                ListAdapter listAdapter=new ListAdapter(MainActivity.this,imageUri);
                list.setAdapter(listAdapter);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class ViewPagerAdapter extends PagerAdapter {
        private Context context;
        //  private String[] imageUrls;
        ArrayList<Bitmap> imageUri;

        public ViewPagerAdapter(Context context, ArrayList<Bitmap> imageUri) {
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
            final ImageView colour_pallet = (ImageView) view.findViewById(R.id.colour);
            final ImageView undo = (ImageView) view.findViewById(R.id.undo);
            final ImageView sticker = (ImageView) view.findViewById(R.id.sticker);
            final TextView save = (TextView) view.findViewById(R.id.done);
            stickerView = (StickerView) findViewById(R.id.sticker_view);
            final ImageView draw = (ImageView) view.findViewById(R.id.draw);
            final FrameLayout layout = (FrameLayout) view.findViewById(R.id.layout);
            final DrawingView mDrawingView=(DrawingView)view.findViewById(R.id.img_screenshot);

            crop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pos=position;

                    //convert bitmap to file
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
                    Uri yourUri = Uri.fromFile(imageFile);

                    //send image for cropping
                    CropImage.activity(yourUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropMenuCropButtonTitle("Done")
                            .setActivityTitle("Crop Image")
                            .setFixAspectRatio(true)
                            .start(MainActivity.this);

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
                        mDrawingView.loadImage(imageUri.get(position));
                        mDrawingView.initializePen();
                        mDrawingView.setPenSize(15);
                        mDrawingView.setPenColor(getColor(R.color.colorAccent));


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

                    bottomSheetColour = new BottomSheetDialog(MainActivity.this);
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
                    imageUri.remove(pos);
                    imageUri.add(0,mDrawingView.getImageBitmap());
                    ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(MainActivity.this,imageUri);
                    imageSwitcher.setAdapter(viewPagerAdapter);

                    ListAdapter listAdapter=new ListAdapter(MainActivity.this,imageUri);
                    list.setAdapter(listAdapter);
                }
            });

         /*   sticker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, ""+position, Toast.LENGTH_SHORT).show();

                    if(position==0){

                        Drawable drawable1 =
                                ContextCompat.getDrawable(MainActivity.this, R.drawable.haizewang_23);

                        stickerView.addSticker(new DrawableSticker(drawable1), Sticker.Position.BOTTOM | Sticker.Position.RIGHT);

                    }

                }
            });*/
            Glide.with(context)
                    .load(imageUri.get(position))
                    .into(imageView);
            container.addView(view);
           // mDrawingView.loadImage(imageUri.get(position));
            return view;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Context context;
        List<Bitmap> ListModel;
        int index = -1;

        public ListAdapter(Context context, List<Bitmap> ListModel) {
            this.context = context;
            this.ListModel = ListModel;

        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {

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

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView=itemView.findViewById(R.id.img);
                border=itemView.findViewById(R.id.border);




            }
        }
    }


}