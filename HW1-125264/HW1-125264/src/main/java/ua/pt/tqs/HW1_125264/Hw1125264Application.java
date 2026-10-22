package ua.pt.tqs.HW1_125264;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Hw1125264Application {

	public static void main(String[] args) {
		SpringApplication.run(Hw1125264Application.class, args);
	}

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
