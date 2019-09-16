package com.example.dndapp._utils;

import android.view.View;

import com.example.dndapp.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class PdfViewer {
    private static PDFView pdfView = null;

    public static void setView(PDFView view) {
        setView(view, 0);
    }
    public static void setView(PDFView view, int page_number) {
        pdfView = view;
    }

    public static PDFView getView() {
        return pdfView;
    }
}
