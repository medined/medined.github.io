package com.affy.wildtuna.adrivers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

public class ShowInvalidDistancesSetException {

    public static void main(final String[] args) throws Exception {
        String url = "https://www.supremecourt.gov/opinions/boundvolumes/545bv.pdf";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            IOUtils.copy(new URL(url).openStream(), baos);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                try (PDDocument doc = PDDocument.load(bais)) {
                    System.out.println("NumPages: " + doc.getNumberOfPages());
                }
            }
        }
        System.out.println("Loading from BAIS works.");
    }
}
