package com.example.campaignplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

// TODO: PDF Viewer functionality disabled due to deprecated library
// The original library (com.github.barteksc:android-pdf-viewer) is no longer maintained
// TODO: Migrate to a modern PDF viewer library when needed
// Possible alternatives:
// - AndroidX PDF Viewer (if available)
// - Google's PDF Viewer library
// - Custom WebView-based PDF viewer

import android.widget.RelativeLayout;

public class PdfViewerActivity extends AppCompatActivity {

    private static final String TAG = "PdfViewerActivity";
    // PDFView pdfView; // Commented out - TODO: Replace with modern PDF viewer
    private SharedPreferences sharedPreferences;
    private int currentPage;
    private Toolbar toolbar;
    private RelativeLayout pdfWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // pdfView = findViewById(R.id.pdfView); // Commented out
        // pdfWrapper = findViewById(R.id.pdf_wrapper);
        // pdfView.setSaveEnabled(true);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pdf Viewer");
        setSupportActionBar(toolbar);

        currentPage = getIntent().getIntExtra("REQUESTED_PAGE_NUMBER", 0);

        // TODO: Implement PDF loading with modern PDF viewer library
        // loadPdf();
        
        Toast.makeText(this, "PDF Viewer functionality is currently disabled. TODO: Migrate to modern PDF viewer.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pdf_viewer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_set_pdf) {
            getLocalPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    private OnPageScrollListener pdfScrollHideToolbar() {
        return new OnPageScrollListener() {
            float oldPos = 0.0f;

            @Override
            public void onPageScrolled(int page, float positionOffset) {

                float diff = oldPos - pdfView.getCurrentYOffset();
                float newY;
                if (diff > 0) { // Scrolled downwards
                    newY = Math.min(0, toolbar.getY() + diff);
                } else { // Scrolled upwards / diff is negative
                    newY = Math.max(-toolbar.getHeight(), toolbar.getY() + diff);
                }

                toolbar.setY(newY);

                oldPos = pdfView.getCurrentYOffset();
            }
        };
    }

    private OnPageChangeListener pdfScrollChangeTitle() {
        return new OnPageChangeListener() {
            @Override
            public void onPageChanged(int page, int pageCount) {
                toolbar.setTitle("Pdf Viewer (" + page + "/" + pageCount + ")");
            }
        };
    }
    */

    private void getLocalPdf() {
        // TODO: Implement with modern PDF viewer
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            // TODO: Implement PDF loading with modern PDF viewer
            // pdfView.fromUri(uri)
            //         .pages(0, currentPage)
            //         .showMinimap(false)
            //         .showErrors(true)
            //         .load();
        }
    }
}
