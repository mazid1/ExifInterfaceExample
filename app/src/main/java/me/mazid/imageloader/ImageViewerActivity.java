package me.mazid.imageloader;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageViewerActivity extends AppCompatActivity {

    private static Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(mBitmap);
    }

    public static void setmBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public static Bitmap getmBitmap() {
        return mBitmap;
    }
}
