package com.example.emailschedulerwithquartz.dto;

import lombok.Data;

@Data
public class EmailResponseDTO {

    private boolean success;

    private String jobid;

    private String jobGroup;

    private String message;

    public EmailResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public EmailResponseDTO(boolean success, String jobid, String jobGroup, String message) {
        this.success = success;
        this.jobid = jobid;
        this.jobGroup = jobGroup;
        this.message = message;
    }
}
