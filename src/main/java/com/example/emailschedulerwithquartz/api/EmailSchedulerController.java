package com.example.emailschedulerwithquartz.api;

import com.example.emailschedulerwithquartz.dto.EmailRequestDTO;
import com.example.emailschedulerwithquartz.dto.EmailResponseDTO;
import com.example.emailschedulerwithquartz.quartz.job.EmailJob;
import com.sun.mail.iap.Response;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController {

    private final Scheduler scheduler;

    public EmailSchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/schedule/email")
    public ResponseEntity<?> scheduleEmail(@Valid @RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequestDTO.getDateTime(), emailRequestDTO.getTimeZone());
            if (dateTime.isBefore(ZonedDateTime.now())) {
                EmailResponseDTO emailResponseDTO = new EmailResponseDTO(false, "dateTime must be after current time");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(emailResponseDTO);
            }

            JobDetail jobDetail = buildJobDetail(emailRequestDTO);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponseDTO emailResponseDTO = new EmailResponseDTO(true, jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email scheduled successfully");
            return ResponseEntity.ok(emailResponseDTO);

        } catch (SchedulerException se) {
            log.error("Erroe while scheduling email: ", se);
            EmailResponseDTO emailResponseDTO = new EmailResponseDTO(false, "Error while scheduling email. Please try again later");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(emailResponseDTO);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getApiTest(){
        return ResponseEntity.ok("Get API Test - Pass");
    }


    private JobDetail buildJobDetail(EmailRequestDTO schedularEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", schedularEmailRequest.getEmail());
        jobDataMap.put("subject", schedularEmailRequest.getSubject());
        jobDataMap.put("body", schedularEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
