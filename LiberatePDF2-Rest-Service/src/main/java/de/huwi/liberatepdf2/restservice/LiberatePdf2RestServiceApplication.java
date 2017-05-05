package de.huwi.liberatepdf2.restservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import de.huwi.liberatepdf2.restservice.storage.StorageProperties;
import de.huwi.liberatepdf2.restservice.storage.StorageService;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties(StorageProperties.class)
public class LiberatePdf2RestServiceApplication {

	public static void main(final String[] args) {
		SpringApplication.run(LiberatePdf2RestServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner init(final StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}
}
