package io.kestra.plugin.sifflet;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This test will test the RunRule task directly.
 * It is only enabled if the required environment variables are set.
 */
@KestraTest
class RunRuleRunnerTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "SIFFLET_API_URL", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "SIFFLET_ACCESS_TOKEN", matches = ".+")
    @EnabledIfEnvironmentVariable(named = "SIFFLET_RULE_ID", matches = ".+")
    void runTask() throws Exception {
        // Create a RunContext with environment variables
        Map<String, Object> vars = Map.of(
                "apiUrl", System.getenv("SIFFLET_API_URL"),
                "accessToken", System.getenv("SIFFLET_ACCESS_TOKEN"),
                "ruleId", System.getenv("SIFFLET_RULE_ID"));

        RunContext runContext = runContextFactory.of(vars);

        // Create and run the task
        RunRule task = RunRule.builder()
                .id("test-task")
                .type(RunRule.class.getName())
                .apiUrl("{{ apiUrl }}")
                .accessToken("{{ accessToken }}")
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
}
