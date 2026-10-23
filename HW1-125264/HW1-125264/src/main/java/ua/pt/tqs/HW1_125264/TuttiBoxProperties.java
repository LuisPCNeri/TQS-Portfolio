package ua.pt.tqs.HW1_125264;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;

@Configuration
@ConfigurationProperties(prefix = "tuttibox")
@Getter
@Data
public class TuttiBoxProperties {
    private int pickupDeadlineHours = 48;
    private int returnDeadlineHours = 72;
}
