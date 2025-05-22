package io.kestra.plugin.sifflet;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@KestraTest
class RunRuleTest {
    @Inject
    private RunContextFactory runContextFactory;

    /**
     * This test is only enabled if the SIFFLET_API_URL, SIFFLET_ACCESS_TOKEN, and
     * SIFFLET_RULE_ID
     * environment variables are set. This allows for testing against a real Sifflet
     * instance.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "SIFFLET_API_URL", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "SIFFLET_ACCESS_TOKEN", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "SIFFLET_RULE_ID", matches = ".+")
    void runWithRealCredentials() throws Exception {
        // Create a RunContext with environment variables
        Map<String, Object> vars = new HashMap<>();
        vars.put("apiUrl", System.getenv("SIFFLET_API_URL"));
        vars.put("accessToken", System.getenv("SIFFLET_ACCESS_TOKEN"));
        vars.put("ruleId", System.getenv("SIFFLET_RULE_ID"));

        RunContext runContext = runContextFactory.of(vars);

        // Create and run the task
        RunRule task = RunRule.builder()
                .id("test-task")
                .type(RunRule.class.getName())
                .baseUrl("{{ baseUrl }}")
                .apiKey("{{ apiKey }}")
                .ruleId("{{ ruleId }}")
                .connectionTimeout(Duration.ofSeconds(30))
                .requestTimeout(Duration.ofMinutes(1))
                .build();

        // Execute the task
        RunRule.Output output = task.run(runContext);

        // Verify the output
        assertThat(output, notNullValue());
        assertThat(output.getStatusCode(), allOf(greaterThanOrEqualTo(200), lessThan(300)));
        assertThat(output.getSuccess(), is(true));
        assertThat(output.getRuleId(), equalTo(System.getenv("SIFFLET_RULE_ID")));
    }

    @Test
    void testInvalidUrl() {
        // Create a RunContext with invalid URL
        Map<String, Object> vars = new HashMap<>();
        vars.put("apiUrl", "https://invalid-url");
        vars.put("accessToken", "dummy-token");
        vars.put("ruleId", "dummy-rule-id");

        RunContext runContext = runContextFactory.of(vars);

        // Create the task
        RunRule task = RunRule.builder()
                .id("test-task")
                .type(RunRule.class.getName())
                .baseUrl("{{ baseUrl }}")
                .apiKey("{{ apiKey }}")
                .ruleId("{{ ruleId }}")
                .connectionTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        // Execute the task and expect an exception
        assertThrows(Exception.class, () -> task.run(runContext));
    }
}
