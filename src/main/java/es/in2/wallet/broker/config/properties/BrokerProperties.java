package es.in2.wallet.broker.config.properties;

import es.in2.wallet.api.config.properties.UrlProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * Configuration intended to connect the NGSI-LD ContextBroker
 *
 * @param provider       - context broker provider
 * @param externalUrl - domain that the broker is externally available. Used for the hashlink.
 * @param internalUrl - internal address of the broker, used to connect from within the connector
 * @param paths          - ngis-ld paths to be used when connecting the broker
 */
@ConfigurationProperties(prefix = "broker")
public record BrokerProperties(String provider, UrlProperties externalUrl, UrlProperties internalUrl,
                               @NestedConfigurationProperty BrokerPathProperties paths) {

    @ConstructorBinding
    public BrokerProperties(String provider, UrlProperties externalUrl, UrlProperties internalUrl, BrokerPathProperties paths) {
        this.provider = provider;
        this.externalUrl = externalUrl;
        this.internalUrl = internalUrl;
        this.paths = Optional.ofNullable(paths).orElse(new BrokerPathProperties(null));
    }

}

