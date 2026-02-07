package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfExportService {

    public ByteArrayInputStream generateOfferPdf(Offer offer) {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("NABÍDKA", titleFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Zákazník: " + offer.getCustomerName(), bodyFont));
            document.add(new Paragraph("Email: " + offer.getCustomerEmail(), bodyFont));
            document.add(new Paragraph("Popis: " + offer.getDescription(), bodyFont));
            document.add(new Paragraph("Cena: " + offer.getTotalPrice() + " Kč", bodyFont));
            document.add(new Paragraph("Stav: " + offer.getStatus(), bodyFont));
            document.add(new Paragraph("Revize: " + offer.getRevision(), bodyFont));
            document.add(new Paragraph("Vytvořeno: " + offer.getCreatedAt(), bodyFont));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Chyba při generování PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}