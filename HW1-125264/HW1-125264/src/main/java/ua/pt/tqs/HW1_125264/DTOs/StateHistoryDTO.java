package ua.pt.tqs.HW1_125264.DTOs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class StateHistoryDTO {
    private String state;
    private LocalDateTime timestamp;
    private String actor;
}
