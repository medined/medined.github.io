package com.affy.wildtuna.bean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class AppProperties {

    @Autowired
    private Environment env;

    @Getter
    private String esHost;
    
    @Getter
    private int esPort;

    @Getter
    private String boundVolumesUrl;
    
    @Getter
    private InetAddress esInetAddress;
    
    @Getter
    private InetSocketTransportAddress esInetSocketTransportAddress;

    @PostConstruct
    public void post() throws UnknownHostException {
        boundVolumesUrl = env.getProperty("bound.volumes.url");
        esHost = env.getProperty("es.host");
        esPort = Integer.parseInt(env.getProperty("es.port"));
        esInetAddress = InetAddress.getByName(getEsHost());
        esInetSocketTransportAddress = new InetSocketTransportAddress(getEsInetAddress(), getEsPort());
    }

}
