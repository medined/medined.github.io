package com.affy.wildtuna.bean;

import com.affy.wildtuna.model.VolumeInfo;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class URLFetcher {
    
    public URLFetcher() {
    }

    public String fetch(final String url) throws IOException {
        return IOUtils.toString(new URL(url).openStream());
    }

    public List<VolumeInfo> fetchBoundVolumeAnchors(final String url) throws IOException {
        ArrayList rv = new ArrayList();
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.body().select("li a");

        elements.stream().forEach((element) -> {
            String href = element.attr("abs:href");
            if (href.contains("boundvolumes/")) {
                String anchor = element.ownText();
                rv.add(new VolumeInfo(href, anchor));
            }
        });
        
        return rv;
    }

}
