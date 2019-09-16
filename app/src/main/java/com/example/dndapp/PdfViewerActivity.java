package com.example.dndapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.dndapp._utils.PdfViewer;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class PdfViewerActivity extends AppCompatActivity {

    private static final String TAG = "PDFViewerActivity";
    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int page_number = getIntent().getIntExtra("REQUESTED_PAGE_NUMBER", 1);

        setContentView(R.layout.activity_pdf_viewer);

        loadPdf(page_number);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int page_number = intent.getIntExtra("REQUESTED_PAGE_NUMBER", 1);

        if (this.pdfView == null) {
            loadPdf(page_number);
        } else {
            this.pdfView.jumpTo(page_number);
        }
    }

    private void loadPdf(int page_number) {
        this.pdfView = findViewById(R.id.pdfView);
        this.pdfView.setSaveEnabled(true);


        pdfView.fromAsset("pdf/5e_PHB_1.pdf")
                .defaultPage(page_number)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();

        PdfViewer.setView(pdfView);
    }



    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }
}
