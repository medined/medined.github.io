package com.affy.wildtuna.adrivers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.InetAddress;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan(basePackages = "com.affy.wildtuna")
@PropertySource("classpath:application.properties")
public class SearchAllDriver {

    @Autowired
    private Environment env;

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(SearchAllDriver.class, args);
        System.out.println("START");
        SearchAllDriver driver = new SearchAllDriver();
        driver.run();
        System.out.println("DONE");
    }

    private void run() throws IOException {
        String esHost = env.getProperty("es.host");
        int esPort = Integer.parseInt(env.getProperty("es.port"));

        try (TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)) {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));

            // MatchAll on the whole cluster with all default options
            SearchResponse response = client.prepareSearch().execute().actionGet();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(response.toString());
            String prettyJsonString = gson.toJson(je);
            System.out.println(prettyJsonString);
        }

    }

}
