package co.edu.escuelaing.securespring;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @CrossOrigin(origins = "*")
    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}