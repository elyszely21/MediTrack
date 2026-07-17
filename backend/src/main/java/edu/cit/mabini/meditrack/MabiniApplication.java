package edu.cit.mabini.meditrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MabiniApplication {

	public static void main(String[] args) {
		SpringApplication.run(MabiniApplication.class, args);
	}

}
