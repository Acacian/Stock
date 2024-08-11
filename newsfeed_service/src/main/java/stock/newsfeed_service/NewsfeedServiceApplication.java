package stock.newsfeed_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class NewsfeedServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsfeedServiceApplication.class, args);
	}

}
