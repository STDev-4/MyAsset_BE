package io.api.myasset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MyAssetApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyAssetApplication.class, args);
	}

}
