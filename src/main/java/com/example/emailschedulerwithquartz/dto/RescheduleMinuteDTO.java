package com.example.emailschedulerwithquartz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RescheduleMinuteDTO {
    private String minute;
}
