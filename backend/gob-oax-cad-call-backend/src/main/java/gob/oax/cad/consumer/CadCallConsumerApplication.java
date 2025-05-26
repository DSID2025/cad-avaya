package gob.oax.cad.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "gob.oax.cad")
public class CadCallConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CadCallConsumerApplication.class, args);
	}

}
