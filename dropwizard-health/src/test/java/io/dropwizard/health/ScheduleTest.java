package io.dropwizard.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<Schedule> configFactory =
        new YamlConfigurationFactory<>(Schedule.class, validator, objectMapper, "dw");

    @Test
    void shouldBuildAScheduleFromYaml() throws Exception {
        final File yml = new File(Resources.getResource("yml/schedule.yml").toURI());
        final Schedule schedule = configFactory.build(yml);

        assertThat(schedule.getCheckInterval().toMilliseconds()).isEqualTo(2500L);
        assertThat(schedule.getDowntimeInterval().toSeconds()).isEqualTo(25L);
        assertThat(schedule.getFailureAttempts()).isEqualTo(2);
        assertThat(schedule.getSuccessAttempts()).isEqualTo(1);
    }
}
