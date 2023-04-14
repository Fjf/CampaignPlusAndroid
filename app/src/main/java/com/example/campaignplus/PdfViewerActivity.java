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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.campaignplus._utils.PdfViewer;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class PdfViewerActivity extends AppCompatActivity {

    private static final String TAG = "PDFViewerActivity";
    private PDFView pdfView;
    private SharedPreferences sharedPreferences;
    private int currentPage;
    private Toolbar toolbar;
    private RelativeLayout pdfWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfView = findViewById(R.id.pdfView);
        pdfWrapper = findViewById(R.id.pdf_wrapper);
        pdfView.setSaveEnabled(true);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pdf Viewer");
        setSupportActionBar(toolbar);

        currentPage = getIntent().getIntExtra("REQUESTED_PAGE_NUMBER", 0);

        loadPdf();
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        currentPage = intent.getIntExtra("REQUESTED_PAGE_NUMBER", 0);

        if (pdfView == null) {
            loadPdf();
        } else {
            pdfView.jumpTo(currentPage);
        }
    }

    private void loadPdf() {
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String pdfString = sharedPreferences.getString("phb_uri", null);

        if (pdfString == null) {
            Toast.makeText(this, "Please select a pdf to use this feature.", Toast.LENGTH_SHORT).show();

            getLocalPdf();
        } else {
            preloadPdf(Uri.parse(pdfString));
        }
    }

    private void getLocalPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        startActivityForResult(intent, 0);
    }

    private void preloadPdf(Uri pdfUri) {
        pdfView.fromUri(pdfUri)
                .defaultPage(currentPage)
                .scrollHandle(new DefaultScrollHandle(this))
                .onPageScroll(pdfScrollHideToolbar())
                .onPageChange(pdfScrollChangeTitle())
                .load();

        PdfViewer.setView(pdfView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == 0) {
            assert data != null;
            Uri uri = data.getData();
            assert uri != null;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("phb_uri", uri.toString());
            editor.apply();
            preloadPdf(uri);
        }
    }

//    @Override
//    public void onBackPressed() {
//        moveTaskToBack(true);
//    }
}
