package com.affy.wildtuna.adrivers;

import com.affy.wildtuna.bean.BoundVolumeContentBuilder;
import com.affy.wildtuna.bean.SHA1Generator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.apache.pdfbox.pdmodel.PDDocument;

public class IndexSingleFileOnLocalStorageDriver {

    private static final SHA1Generator sha1Generator = new SHA1Generator();

    public static void main(final String[] args) throws Exception {
        System.out.println("START");
        IndexSingleFileOnLocalStorageDriver driver = new IndexSingleFileOnLocalStorageDriver();
        driver.run();
        System.out.println("DONE");
    }

    private void run() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        BoundVolumeContentBuilder bvcBuilder = new BoundVolumeContentBuilder();

        File pdfFile = new File("bound-volumes/545bv.pdf");
        String filenameSha1 = sha1Generator.of_string(pdfFile.getAbsolutePath());
        String contentsSha1 = sha1Generator.of_file(pdfFile);
        String content = pdfFile.getAbsolutePath();

        String docId = filenameSha1;
        Date indexDate = new Date();

        try (PDDocument doc = PDDocument.load(pdfFile)) {
            String numPages = Integer.toString(doc.getNumberOfPages());
            bvcBuilder.builder(docId, numPages, "href", "anchor", contentsSha1, content, indexDate);
        }

    }

}
