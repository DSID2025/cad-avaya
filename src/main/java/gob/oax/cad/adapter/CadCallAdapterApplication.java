package gob.oax.cad.adapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CadCallAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CadCallAdapterApplication.class, args);
	}

}
