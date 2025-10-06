package com.aman.finservAssignment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
public record WebhookResponse(@JsonProperty("webhook") String webhookUrl,
                              @JsonProperty("accessToken") String accessToken) {
}