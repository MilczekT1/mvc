package pl.konradboniecki.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.chassis.ChassisApplication;

@ChassisApplication
public class FrontEndMVC {

	public static void main(String[] args) {
		SpringApplication.run(FrontEndMVC.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

