package cz.voho.shitorrent;

import cz.voho.shitorrent.model.internal.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by vojta on 13/01/16.
 */
@SpringBootApplication
@ComponentScan(value = "")
public class Launcher {
    private static int port = 7890;

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            SpringApplication.run(Launcher.class, args);
        }
    }

    @Bean
    public Configuration configurationCustomizer() {
        Configuration configuration = new Configuration();
        configuration.setLocalPort(port++);
        return configuration;
    }

    @Bean
    public JettyEmbeddedServletContainerFactory containerCustomizer() {
        return new JettyEmbeddedServletContainerFactory(port);
    }
}
