package com.smart_hire.dispatcher;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dispatcher.services")
public record DispatcherServiceProperties(
        String authUrl,
        String documentUrl,
        String jobUrl,
        String applicationUrl,
        String analysisUrl
) {
}
