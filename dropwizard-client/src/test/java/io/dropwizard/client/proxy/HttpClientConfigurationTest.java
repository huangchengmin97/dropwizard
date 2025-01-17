package io.dropwizard.client.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.configuration.ConfigurationParsingException;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.util.Resources;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class HttpClientConfigurationTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private HttpClientConfiguration configuration = new HttpClientConfiguration();

    private void load(String configLocation) throws Exception {
        configuration = new DefaultConfigurationFactoryFactory<HttpClientConfiguration>()
            .create(
                HttpClientConfiguration.class,
                Validators.newValidator(),
                objectMapper, "dw"
            ).build(new File(Resources.getResource(configLocation).toURI()));
    }

    @Test
    void testNoProxy() throws Exception {
        load("./yaml/no_proxy.yml");
        assertThat(configuration.getProxyConfiguration()).isNull();
    }

    @Test
    void testFullConfigBasicProxy() throws Exception {
        load("yaml/proxy.yml");

        ProxyConfiguration proxy = requireNonNull(configuration.getProxyConfiguration());

        assertThat(proxy.getHost()).isEqualTo("192.168.52.11");
        assertThat(proxy.getPort()).isEqualTo(8080);
        assertThat(proxy.getScheme()).isEqualTo("https");

        AuthConfiguration auth = requireNonNull(proxy.getAuth());
        assertThat(auth.getUsername()).isEqualTo("secret");
        assertThat(auth.getPassword()).isEqualTo("stuff");

        List<String> nonProxyHosts = proxy.getNonProxyHosts();
        assertThat(nonProxyHosts).contains("localhost", "192.168.52.*", "*.example.com");
    }

    @Test
    void testFullConfigNtlmProxy() throws Exception {
        load("yaml/proxy_ntlm.yml");

        ProxyConfiguration proxy = requireNonNull(configuration.getProxyConfiguration());

        assertThat(proxy.getHost()).isEqualTo("192.168.52.11");
        assertThat(proxy.getPort()).isEqualTo(8080);
        assertThat(proxy.getScheme()).isEqualTo("https");

        AuthConfiguration auth = requireNonNull(proxy.getAuth());
        assertThat(auth.getUsername()).isEqualTo("secret");
        assertThat(auth.getPassword()).isEqualTo("stuff");
        assertThat(auth.getAuthScheme()).isEqualTo("NTLM");
        assertThat(auth.getRealm()).isEqualTo("realm");
        assertThat(auth.getHostname()).isEqualTo("workstation");
        assertThat(auth.getDomain()).isEqualTo("HYPERCOMPUGLOBALMEGANET");
        assertThat(auth.getCredentialType()).isEqualTo("NT");

        List<String> nonProxyHosts = proxy.getNonProxyHosts();
        assertThat(nonProxyHosts).contains("localhost", "192.168.52.*", "*.example.com");
    }

    @Test
    void testNoScheme() throws Exception {
        load("./yaml/no_scheme.yml");

        ProxyConfiguration proxy = requireNonNull(configuration.getProxyConfiguration());
        assertThat(proxy.getHost()).isEqualTo("192.168.52.11");
        assertThat(proxy.getPort()).isEqualTo(8080);
        assertThat(proxy.getScheme()).isEqualTo("http");
    }

    @Test
    void testNoAuth() throws Exception {
        load("./yaml/no_auth.yml");

        ProxyConfiguration proxy = requireNonNull(configuration.getProxyConfiguration());
        assertThat(proxy.getHost()).isNotNull();
        assertThat(proxy.getAuth()).isNull();
    }

    @Test
    void testNoPort() throws Exception {
        load("./yaml/no_port.yml");

        ProxyConfiguration proxy = requireNonNull(configuration.getProxyConfiguration());
        assertThat(proxy.getHost()).isNotNull();
        assertThat(proxy.getPort()).isEqualTo(-1);
    }

    @Test
    void testNoNonProxy() throws Exception {
        load("./yaml/no_port.yml");

        ProxyConfiguration proxy = requireNonNull(configuration.getProxyConfiguration());
        assertThat(proxy.getNonProxyHosts()).isNull();
    }

    @Test
    void testNoHost() {
        assertConfigurationValidationException("yaml/bad_host.yml");
    }

    @Test
    void testBadPort() {
        assertConfigurationValidationException("./yaml/bad_port.yml");
    }

    @Test
    void testBadScheme() {
        assertThatExceptionOfType(ConfigurationParsingException.class).isThrownBy(() ->
            load("./yaml/bad_scheme.yml"));
    }

    @Test
    void testBadAuthUsername() {
        assertConfigurationValidationException("./yaml/bad_auth_username.yml");
    }

    @Test
    void testBadPassword() {
        assertConfigurationValidationException("./yaml/bad_auth_password.yml");
    }

    @Test
    void testBadAuthScheme() {
        assertConfigurationValidationException("./yaml/bad_auth_scheme.yml");
    }

    @Test
    void testBadCredentialType() {
        assertConfigurationValidationException("./yaml/bad_auth_credential_type.yml");
    }

    private void assertConfigurationValidationException(String configLocation){
        assertThatExceptionOfType(ConfigurationValidationException.class).isThrownBy(()->load(configLocation));
    }
}
