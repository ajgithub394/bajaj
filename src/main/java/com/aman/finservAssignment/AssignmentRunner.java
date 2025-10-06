package com.aman.finservAssignment;

import com.aman.finservAssignment.dto.RegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aman.finservAssignment.dto.SolutionRequest;
import com.aman.finservAssignment.dto.WebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AssignmentRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;

    public AssignmentRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("----- Task Starting -----");

        String registrationUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        RegistrationRequest registrationRequest = new RegistrationRequest(
                "Aman Jain",
                "112216006",
                "112216006@ece.iiitp.ac.in"
        );

        System.out.println("Step 1: Sending registration request to " + registrationUrl);
        System.out.println("Request Body: " + registrationRequest);

        ResponseEntity<WebhookResponse> webhookResponseEntity = restTemplate.postForEntity(
                registrationUrl,
                registrationRequest,
                WebhookResponse.class
        );

        if (webhookResponseEntity.getStatusCode().is2xxSuccessful() && webhookResponseEntity.getBody() != null) {
            WebhookResponse webhookResponse = webhookResponseEntity.getBody();
            System.out.println("Step 2: Successfully received webhook details.");
            System.out.println("Webhook URL: " + webhookResponse.webhookUrl());
            System.out.println("Access Token: " + webhookResponse.accessToken());

            String sqlQuery = """
            WITH EmployeeAgeRank AS (
                SELECT
                    e.emp_ID,
                    e.first_name,
                    e.last_name,
                    d.department_name,
                    (COUNT(*) OVER (partition BY e.department) - 1) -
                    (COUNT(*) OVER (partition BY e.department ORDER BY e.DOB) - 1) AS YOUNGER_EMPLOYEES_COUNT
                FROM
                    EMPLOYEE e
                JOIN
                    DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
            )
            SELECT
                emp_id,
                first_name,
                last_name,
                department_name,
                YOUNGER_EMPLOYEES_COUNT
            FROM
                EmployeeAgeRank
            ORDER BY
                emp_id DESC;
            """;

            System.out.println("Step 3: Prepared SQL query for submission.");

            SolutionRequest solutionRequest = new SolutionRequest(sqlQuery);

            HttpHeaders submissionHeaders = new HttpHeaders();
            submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
            submissionHeaders.set("Authorization", webhookResponse.accessToken());

            HttpEntity<SolutionRequest> submissionEntity = new HttpEntity<>(solutionRequest, submissionHeaders);

            String submissionUrl = webhookResponse.webhookUrl();

            System.out.println("Step 4: Submitting final SQL query to " + submissionUrl);

            ResponseEntity<String> submissionResponse = restTemplate.exchange(
                    submissionUrl,
                    HttpMethod.POST,
                    submissionEntity,
                    String.class
            );

            System.out.println("Submission Response Status: " + submissionResponse.getStatusCode());
            System.out.println("Submission Response Body: " + submissionResponse.getBody());

        } else {
            System.err.println("Failed to get webhook URL. Status: " + webhookResponseEntity.getStatusCode());
            System.err.println("Response Body: " + webhookResponseEntity.getBody());
        }

        System.out.println("----- TASK FINISHED -----");
    }
}