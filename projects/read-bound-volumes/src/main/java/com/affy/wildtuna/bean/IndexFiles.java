package com.affy.wildtuna.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class IndexFiles {

    public static void main(final String[] args) throws Exception {
        System.out.println("START");
        IndexFiles driver = new IndexFiles();
        driver.run();
        System.out.println("DONE");
    }

    private void run() throws IOException, FileNotFoundException, NoSuchAlgorithmException {

        try (TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)) {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("45.56.107.79"), 14353));

            File pdfFile = new File("502bv.pdf");
            String filenameSha1 = calculate_sha1_of_string(pdfFile.getAbsolutePath());
            String contentsSha1 = calculate_sha1_of_file(pdfFile);
            try (PDDocument doc = PDDocument.load(pdfFile)) {
                IndexResponse response = client.prepareIndex("bound-volumes", "us-supreme-court", filenameSha1)
                        .setSource(jsonBuilder().startObject()
                                .field("documentId", doc.getDocumentId())
                                .field("numPages", doc.getNumberOfPages())
                                .field("sourceFile", pdfFile.getAbsolutePath())
                                .field("contentSha1", contentsSha1)
                                .field("filenameSha1", filenameSha1)
                                .field("user", "kimchy")
                                .field("postDate", new Date())
                                .field("message", "trying out Elasticsearch")
                                .endObject()
                        )
                        .get();
            }

        }

    }

    /**
     * Read the file and calculate the SHA-1 checksum
     *
     * @param file the file to read
     * @return the hex representation of the SHA-1 using uppercase chars
     * @throws FileNotFoundException if the file does not exist, is a directory
     * rather than a regular file, or for some other reason cannot be opened for
     * reading
     * @throws IOException if an I/O error occurs
     * @throws NoSuchAlgorithmException should never happen
     */
    private String calculate_sha1_of_file(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }

    private String calculate_sha1_of_string(String s) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(s.getBytes(), 0, s.length());
        return new HexBinaryAdapter().marshal(sha1.digest());
    }
}
