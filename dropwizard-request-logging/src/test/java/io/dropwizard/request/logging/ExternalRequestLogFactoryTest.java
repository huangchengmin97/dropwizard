package io.dropwizard.request.logging;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalRequestLogFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    void canBeDeserialized() throws Exception {
        RequestLogFactory<?> externalRequestLogFactory = new YamlConfigurationFactory<>(RequestLogFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new File(Resources.getResource("yaml/externalRequestLog.yml").toURI()));
        assertThat(externalRequestLogFactory)
            .isNotNull()
            .isInstanceOf(ExternalRequestLogFactory.class);
        assertThat(externalRequestLogFactory.isEnabled()).isTrue();
    }

    @Test
    void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(ExternalRequestLogFactory.class);
    }
}
