package io.kestra.plugin.sifflet;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(title = "Run a Sifflet rule", description = "Trigger a run of a Sifflet rule by its ID")
@Plugin(examples = {
        @Example(title = "Run a Sifflet rule", code = {
                "apiUrl: \"https://tenant.siffletdata.com/api/v1\"",
                "accessToken: \"{{ secret('SIFFLET_ACCESS_TOKEN') }}\"",
                "ruleId: \"12345678-1234-1234-1234-123456789012\""
        })
})
public class RunRule extends Task implements RunnableTask<RunRule.Output> {

    @Schema(title = "Sifflet Base URL", description = "The base URL of the Sifflet instance (e.g., https://app.siffletdata.com)")
    @PluginProperty(dynamic = true)
    @NotNull
    private String baseUrl;

    @Schema(title = "Sifflet API Key", description = "The API key for authenticating with the Sifflet API")
    @PluginProperty(dynamic = true)
    @NotNull
    private String apiKey;

    @Schema(title = "Rule ID", description = "The ID of the Sifflet rule to run")
    @PluginProperty(dynamic = true)
    @NotNull
    private String ruleId;

    @Schema(title = "Connection timeout", description = "The timeout for establishing a connection to the Sifflet API")
    @PluginProperty
    private Duration connectionTimeout;

    @Schema(title = "Request timeout", description = "The timeout for the entire request to the Sifflet API")
    @PluginProperty
    private Duration requestTimeout;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        // Render dynamic properties
        String renderedBaseUrl = runContext.render(baseUrl);
        String renderedApiKey = runContext.render(apiKey);
        String renderedRuleId = runContext.render(ruleId);

        // Construct the API URL for running a rule
        String apiEndpoint = renderedBaseUrl + "/api/v1/rules/" + renderedRuleId + "/_run";

        logger.info("Triggering Sifflet rule with ID: {}", renderedRuleId);

        // Create HTTP client with timeouts if specified
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        if (connectionTimeout != null) {
            clientBuilder.connectTimeout(connectionTimeout);
        }

        HttpClient client = clientBuilder.build();

        // Create HTTP request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + renderedApiKey)
                .POST(HttpRequest.BodyPublishers.noBody());

        if (requestTimeout != null) {
            requestBuilder.timeout(requestTimeout);
        }

        HttpRequest request = requestBuilder.build();

        // Execute the request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            logger.info("Successfully triggered Sifflet rule. Status code: {}", statusCode);
            return Output.builder()
                    .statusCode(statusCode)
                    .response(responseBody)
                    .ruleId(renderedRuleId)
                    .success(true)
                    .build();
        } else {
            logger.error("Failed to trigger Sifflet rule. Status code: {}, Response: {}", statusCode, responseBody);
            throw new Exception(
                    "Failed to trigger Sifflet rule. Status code: " + statusCode + ", Response: " + responseBody);
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "HTTP status code from the Sifflet API response")
        private final Integer statusCode;

        @Schema(title = "Response body from the Sifflet API")
        private final String response;

        @Schema(title = "The ID of the rule that was triggered")
        private final String ruleId;

        @Schema(title = "Whether the rule was successfully triggered")
        private final Boolean success;
    }
}
