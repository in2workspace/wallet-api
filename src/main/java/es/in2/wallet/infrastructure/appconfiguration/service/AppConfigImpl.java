package es.in2.wallet.infrastructure.appconfiguration.service;


import es.in2.wallet.application.port.AppConfig;
import es.in2.wallet.infrastructure.appconfiguration.util.ConfigAdapterFactory;
import es.in2.wallet.infrastructure.core.config.properties.AuthServerProperties;
import es.in2.wallet.infrastructure.core.config.properties.VerifiablePresentationProperties;
import es.in2.wallet.infrastructure.core.config.properties.WalletDrivingApplicationProperties;
import es.in2.wallet.infrastructure.ebsi.config.properties.EbsiProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static es.in2.wallet.domain.util.ApplicationUtils.formatUrl;

@Configuration
@Slf4j
public class AppConfigImpl implements AppConfig {

    private final GenericConfigAdapter genericConfigAdapter;
    private final AuthServerProperties authServerProperties;
    private final WalletDrivingApplicationProperties walletDrivingApplicationProperties;
    private final EbsiProperties ebsiProperties;

    private final VerifiablePresentationProperties verifiablePresentationProperties;

    private String authServerInternalUrl;
    private String authServerExternalUrl;
    private String authServerTokenEndpoint;

    @PostConstruct
    public void init() {
        authServerInternalUrl = initAuthServerInternalUrl();
        authServerExternalUrl = initAuthServerExternalUrl();
        authServerTokenEndpoint = initAuthServerTokenEndpoint();
        log.debug(authServerExternalUrl);
        log.debug(authServerInternalUrl);
    }

    public AppConfigImpl(ConfigAdapterFactory configAdapterFactory,
                         AuthServerProperties authServerProperties,
                         WalletDrivingApplicationProperties walletDrivingApplicationProperties,
                         EbsiProperties ebsiProperties,
                         VerifiablePresentationProperties verifiablePresentationProperties) {
        this.genericConfigAdapter = configAdapterFactory.getAdapter();
        this.authServerProperties = authServerProperties;
        this.walletDrivingApplicationProperties = walletDrivingApplicationProperties;
        this.ebsiProperties = ebsiProperties;
        log.debug(ebsiProperties.url());
        this.verifiablePresentationProperties = verifiablePresentationProperties;
    }


    @Override
    public List<String> getWalletDrivingUrls() {
        log.debug(String.valueOf(walletDrivingApplicationProperties.urls().get(0).port()));
        return walletDrivingApplicationProperties.urls().stream()
                .map(urlProperties -> {
                    String domain = "localhost".equalsIgnoreCase(urlProperties.domain()) ?
                            urlProperties.domain() :
                            genericConfigAdapter.getConfiguration(urlProperties.domain());
                    return formatUrl(urlProperties.scheme(), domain, urlProperties.port(), null);
                })
                .toList();
    }

    @Override
    public String getAuthServerInternalUrl() {
        return authServerInternalUrl;
    }


    private String initAuthServerInternalUrl() {
        return formatUrl(authServerProperties.internalUrl().scheme(),
                genericConfigAdapter.getConfiguration(authServerProperties.internalUrl().domain()),
                authServerProperties.internalUrl().port(),
                authServerProperties.internalUrl().path());
    }

    @Override
    public String getAuthServerExternalUrl() {
        return authServerExternalUrl;
    }

    private String initAuthServerExternalUrl() {
        return formatUrl(authServerProperties.externalUrl().scheme(),
                genericConfigAdapter.getConfiguration(authServerProperties.externalUrl().domain()),
                authServerProperties.externalUrl().port(),
                authServerProperties.externalUrl().path());
    }

    @Override
    public String getAuthServerTokenEndpoint() {
        return authServerTokenEndpoint;
    }

    private String initAuthServerTokenEndpoint() {
        return formatUrl(authServerProperties.tokenUrl().scheme(),
                genericConfigAdapter.getConfiguration(authServerProperties.tokenUrl().domain()),
                authServerProperties.tokenUrl().port(),
                authServerProperties.tokenUrl().path());
    }

    @Override
    public String getIdentityProviderUrl() {
        return ebsiProperties.url();
    }

    @Override
    public String getIdentityProviderUsername() {
        return ebsiProperties.username();
    }

    @Override
    public String getIdentityProviderPassword() {
        return ebsiProperties.password();
    }

    @Override
    public String getIdentityProviderClientId() {
        return ebsiProperties.clientId();
    }

    @Override
    public String getIdentityProviderClientSecret() {
        return ebsiProperties.clientSecret();
    }

    @Override
    public Long getCredentialPresentationExpirationTime() {
        return verifiablePresentationProperties.expirationTime();
    }

    @Override
    public String getCredentialPresentationExpirationUnit() {
        return verifiablePresentationProperties.expirationUnit();
    }

    private String getAuthServerJwtDecoderPath() {
        return authServerProperties.jwtDecoderPath();
    }
    @Override
    public String getJwtDecoder() {
        return getAuthServerInternalUrl() + getAuthServerJwtDecoderPath();
    }

}
