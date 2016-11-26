package com.affy.wildtuna.bean;

import java.io.IOException;
import java.util.Date;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.springframework.stereotype.Component;

@Component
public class BoundVolumeContentBuilder {

    public XContentBuilder builder(final String docId, final String numPages, final String href, final String anchor, final String cSha1, final String content, final Date indexDate) throws IOException {
        return jsonBuilder().startObject()
                .field("documentId", docId)
                .field("numPages", numPages)
                .field("sourceAnchor", anchor)
                .field("contentSha1", cSha1)
                .field("content", content)
                .field("indexDate", indexDate)
                .endObject();
    }

}
