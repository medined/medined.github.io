package com.affy.wildtuna.adrivers;

import com.affy.wildtuna.bean.IndexBoundVolumes;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IndexBoundVolumesDriver {

    public static void main(final String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("com.affy.wildtuna"); 
        ctx.refresh();

        System.out.println("START");
        IndexBoundVolumesDriver driver = new IndexBoundVolumesDriver();
        driver.process(ctx);
        System.out.println("DONE");
    }

    private void process(ApplicationContext ctx) throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        IndexBoundVolumes o = ctx.getBean(IndexBoundVolumes.class);
        o.process();
    }
}
