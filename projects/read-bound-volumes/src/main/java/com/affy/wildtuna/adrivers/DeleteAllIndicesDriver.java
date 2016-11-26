package com.affy.wildtuna.adrivers;

import java.io.IOException;
import static java.lang.StrictMath.log;
import java.net.InetAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class DeleteAllIndicesDriver {

    private static final Logger LOG = LogManager. getLogger("DeleteAllIndices");

    public static void main(final String[] args) throws Exception {
        System.out.println("START");
        DeleteAllIndicesDriver driver = new DeleteAllIndicesDriver();
        driver.run();
        System.out.println("DONE");
    }

    private void run() throws IOException {

        try (TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)) {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("45.56.107.79"), 14353));

            DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest("twitter")).actionGet();
            if (delete.isAcknowledged()) {
                LOG.error("Index: DELETED.");
            } else {
                LOG.error("Index: NOT DELETED.");
                
            }

        }

    }

}
