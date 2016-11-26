package com.affy.wildtuna.bean;

import com.affy.wildtuna.model.VolumeInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexBoundVolumes {

    @Autowired
    private AppProperties properties;

    @Autowired
    private URLFetcher fetcher;

    @Autowired
    private SHA1Generator sha1Generator;

    @Autowired
    private BoundVolumeContentBuilder bvcBuilder;

    public void process() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        List<VolumeInfo> anchors = fetcher.fetchBoundVolumeAnchors(properties.getBoundVolumesUrl());
        try (TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)) {
            client.addTransportAddress(properties.getEsInetSocketTransportAddress());
            Date indexDate = new Date();
            for (VolumeInfo vi : anchors) {
                String href = null;
                try {
                    href = vi.getHref();
                    String anchor = vi.getAnchor();
                    String hrefSha1 = sha1Generator.of_string(href);

                    try {
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            IOUtils.copy(new URL(href).openStream(), baos);
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                                try (PDDocument doc = PDDocument.load(bais)) {
                                    String content = new PDFTextStripper().getText(doc);
                                    String contentsSha1 = sha1Generator.of_string(content);
                                    String numPages = Integer.toString(doc.getNumberOfPages());
                                    XContentBuilder builder = bvcBuilder.builder(href, numPages, href, anchor, contentsSha1, content, indexDate);
                                    client.prepareIndex("bound-volumes", "us-supreme-court", hrefSha1).setSource(builder).get();
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Exception loading " + href);
                        e.printStackTrace();
                        continue;
                    }
                    System.out.println("Indexed: " + href);
                } catch (Exception e) {
                    System.out.println("Exception [" + e.getMessage() + "] thrown processing " + href);
                    e.printStackTrace();
                }
            }
        }
    }
}
