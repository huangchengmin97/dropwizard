package io.dropwizard.metrics;

import com.codahale.metrics.MetricAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<MetricsFactory> factory = new YamlConfigurationFactory<>(
        MetricsFactory.class, BaseValidator.newValidator(), objectMapper, "dw");
    private MetricsFactory config;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleReporterFactory.class, CsvReporterFactory.class,
            Slf4jReporterFactory.class);

        this.config = factory.build(new File(Resources.getResource("yaml/metrics.yml").toURI()));
    }

    @Test
    void hasADefaultFrequency() throws Exception {
        assertThat(config.getFrequency()).isEqualTo(Duration.seconds(10));
    }

    @Test
    void hasReporters() throws Exception {
        CsvReporterFactory csvReporter = new CsvReporterFactory();
        csvReporter.setFile(new File("metrics"));
        assertThat(config.getReporters()).hasSize(3);
    }

    @Test
    void canReadExcludedAndIncludedAttributes() {
        assertThat(config.getReporters()).hasSize(3);
        final ReporterFactory reporterFactory = config.getReporters().get(0);
        assertThat(reporterFactory).isInstanceOf(ConsoleReporterFactory.class);
        final ConsoleReporterFactory consoleReporterFactory = (ConsoleReporterFactory) reporterFactory;
        assertThat(consoleReporterFactory.getIncludesAttributes()).isEqualTo(EnumSet.of(
            MetricAttribute.P50, MetricAttribute.P95, MetricAttribute.P98, MetricAttribute.P99));
        assertThat(consoleReporterFactory.getExcludesAttributes()).isEqualTo(EnumSet.of(MetricAttribute.P98));
    }

    @Test
    void canReadDefaultExcludedAndIncludedAttributes() {
        assertThat(config.getReporters()).hasSize(3);
        final ReporterFactory reporterFactory = config.getReporters().get(1);
        assertThat(reporterFactory).isInstanceOf(CsvReporterFactory.class);
        final CsvReporterFactory csvReporterFactory = (CsvReporterFactory) reporterFactory;
        assertThat(csvReporterFactory.getIncludesAttributes()).isEqualTo(EnumSet.allOf(MetricAttribute.class));
        assertThat(csvReporterFactory.getExcludesAttributes()).isEmpty();
    }

    @Test
    void reportOnStopFalseByDefault() {
        assertThat(config.isReportOnStop()).isFalse();
    }

    @Test
    void reportOnStopCanBeTrue() throws Exception {
        config = factory.build(new File(Resources.getResource("yaml/metrics-report-on-stop.yml").toURI()));
        assertThat(config.isReportOnStop()).isTrue();
    }

}
