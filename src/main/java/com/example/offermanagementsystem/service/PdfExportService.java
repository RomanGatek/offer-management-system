package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.Offer;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
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

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Nabídka #" + offer.getId(), titleFont));
            document.add(new Paragraph("Zákazník: " + offer.getCustomerName(), bodyFont));
            document.add(new Paragraph("Popis: " + offer.getDescription(), bodyFont));
            document.add(new Paragraph("Cena celkem: " + offer.getTotalPrice() + " Kč", bodyFont));
            document.add(new Paragraph("Vytvořeno: " + offer.getCreatedDate(), bodyFont));

            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}