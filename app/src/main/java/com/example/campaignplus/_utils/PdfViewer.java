package com.example.campaignplus._utils;

import com.github.barteksc.pdfviewer.PDFView;

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
