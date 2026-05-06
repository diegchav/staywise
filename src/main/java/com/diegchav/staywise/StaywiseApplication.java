package com.diegchav.staywise;

import com.diegchav.staywise.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class StaywiseApplication {
	public static void main(String[] args) {
		SpringApplication.run(StaywiseApplication.class, args);
	}
}
