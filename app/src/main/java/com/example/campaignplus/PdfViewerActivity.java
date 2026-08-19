package com.example.campaignplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfViewerActivity extends AppCompatActivity {

    private static final String TAG = "PdfViewerActivity";
    private static final int REQUEST_PICK_PDF = 1;
    private static final String PREFS_NAME = "PdfViewerPrefs";
    private static final String KEY_PDF_PATH = "selected_pdf_path";
    
    private TextView pdfStatusText;
    private Toolbar toolbar;
    private File currentPdfFile;
    private boolean hasViewedPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_pdf_viewer);

            pdfStatusText = findViewById(R.id.pdf_status_text);
            toolbar = findViewById(R.id.toolbar);
            
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle(R.string.pdf_viewer_title);
                }
            }

            // Check if a PDF path was passed via intent
            Intent intent = getIntent();
            if (intent != null) {
                String pdfPath = intent.getStringExtra("pdf_path");
                if (pdfPath != null && !pdfPath.isEmpty()) {
                    File pdfFile = new File(pdfPath);
                    if (pdfFile.exists()) {
                        currentPdfFile = pdfFile;
                        hasViewedPdf = true;
                    }
                }
            }
            // Load saved PDF path only if no file was passed via intent
            if (currentPdfFile == null) {
                loadSavedPdf();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                showNoFileSelected();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    private void loadSavedPdf() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedPath = prefs.getString(KEY_PDF_PATH, null);
        
        if (savedPath != null && !savedPath.isEmpty()) {
            File savedFile = new File(savedPath);
            if (savedFile.exists()) {
                currentPdfFile = savedFile;
                return;
            }
        }
        showNoFileSelected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        // View the PDF if we have one and haven't already viewed it
        if (!hasViewedPdf && currentPdfFile != null && currentPdfFile.exists()) {
            viewPdf(currentPdfFile);
        }
    }

    private void showNoFileSelected() {
        if (pdfStatusText != null && !isFinishing() && !isDestroyed()) {
            pdfStatusText.setVisibility(View.VISIBLE);
            pdfStatusText.setText("No PDF file selected\n\nTap the menu icon (Upload new pdf) to select a PDF file from your device. The selected PDF will be remembered for next time.");
        }
    }

    private void viewPdf(File file) {
        if (file == null || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            Uri uri = FileProvider.getUriForFile(this, 
                getPackageName() + ".fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(intent);
            hasViewedPdf = true;
        } catch (Exception e) {
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, 
                    "No PDF viewer installed. Please install a PDF viewer app.", 
                    Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        if (!isFinishing() && !isDestroyed()) {
            getMenuInflater().inflate(R.menu.menu_pdf_viewer, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        try {
            if (isFinishing() || isDestroyed()) {
                return true;
            }
            
            int id = item.getItemId();

            if (id == R.id.action_set_pdf) {
                openPdfPicker();
                return true;
            } else if (id == android.R.id.home) {
                finish();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_PICK_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_PICK_PDF && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && !isFinishing() && !isDestroyed()) {
                try {
                    // Copy PDF to app's files directory (persists across app restarts)
                    File pdfFile = new File(getFilesDir(), "selected_document.pdf");
                    copyUriToFile(uri, pdfFile);
                    
                    currentPdfFile = pdfFile;
                    
                    // Save the path to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putString(KEY_PDF_PATH, pdfFile.getAbsolutePath()).apply();
                    
                    if (!isFinishing() && !isDestroyed()) {
                        viewPdf(currentPdfFile);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, 
                        "Error loading PDF: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyUriToFile(Uri uri, File destFile) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
              OutputStream outputStream = new FileOutputStream(destFile)) {
            if (inputStream == null) {
                throw new IOException("Cannot open input stream for URI: " + uri);
            }
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasViewedPdf = false;
        currentPdfFile = null;
    }
}
