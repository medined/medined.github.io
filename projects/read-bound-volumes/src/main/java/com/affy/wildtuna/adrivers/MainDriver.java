package com.affy.wildtuna.adrivers;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.Splitter;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class MainDriver {

    public static void main(final String[] args) throws Exception {
        System.out.println("START");
        MainDriver driver = new MainDriver();
        driver.run();
        System.out.println("DONE");
    }

    private void run() throws IOException {
        File pdfFile = new File("502bv.pdf");
        PDDocument doc = PDDocument.load(pdfFile);
        Splitter splitter = new Splitter();
        List<PDDocument> splittedDocuments = splitter.split(doc);
        doc.close();
        
        try (TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)) {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("45.56.107.79"), 14353));

            int page = 0;
            for (PDDocument pddDocument : splittedDocuments) {
                System.out.println("\n**** PAGE " + page + " ****\n");
                String content = new PDFTextStripper().getText(pddDocument);
                System.out.println(content);
                page++;
            }
        }

    }

}
