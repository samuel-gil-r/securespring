package co.edu.escuelaing.securespring;

import java.util.Collections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Securespring {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Securespring.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "6700"));
        app.run(args);
    }
}