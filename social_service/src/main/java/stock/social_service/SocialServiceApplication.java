package stock.social_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableKafka
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SocialServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialServiceApplication.class, args);
	}

}
