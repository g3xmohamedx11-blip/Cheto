package com.vodka.cheto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 7001;
    private AnalysisView analysisView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 40, 28, 28);
        root.setBackgroundColor(0xFF0A0A0A);

        TextView title = new TextView(this);
        title.setText("VODKA | CHETO  •  TRAINING");
        title.setTextColor(0xFFD4AF37);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, 70));

        Button load = button("LOAD TABLE SCREENSHOT");
        load.setOnClickListener(v -> pickImage());
        root.addView(load);

        Button predict = button("SHOW PREDICTIONS");
        predict.setOnClickListener(v -> {
            if (analysisView == null) return;
            analysisView.setPredictionsEnabled(true);
        });
        root.addView(predict);

        Button clear = button("CLEAR");
        clear.setOnClickListener(v -> { if (analysisView != null) analysisView.clear(); });
        root.addView(clear);

        analysisView = new AnalysisView(this);
        root.addView(analysisView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(0xFF000000);
        b.setBackgroundColor(0xFFD4AF37);
        return b;
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (bmp == null) throw new IllegalStateException("Invalid image");
            analysisView.setImage(bmp);
        } catch (Exception e) {
            Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
        }
    }
}
