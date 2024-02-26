package es.in2.wallet.api.service;

import es.in2.wallet.api.config.CryptoConfig;
import es.in2.wallet.api.service.impl.DidKeyGeneratorServiceImpl;
import es.in2.wallet.vault.service.VaultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CryptoConfig.class)
class DidKeyGeneratorServiceImplTest {

    @Mock
    private VaultService vaultService;

    @InjectMocks
    private DidKeyGeneratorServiceImpl didKeyGeneratorService;

    @Test
    void testGenerateDidKeyJwkJcsPubWithFromKeyPair() {
        when(vaultService.saveSecret(any())).thenReturn(Mono.empty());

        StepVerifier.create(didKeyGeneratorService.generateDidKeyJwkJcsPub())
                .assertNext(did -> {
                    assert did != null;
                    assert did.startsWith("did:key:z");
                })
                .verifyComplete();

        verify(vaultService, times(1)).saveSecret(any());
    }

    @Test
    void testGenerateDidKeyWithFromKeyPair() {
        when(vaultService.saveSecret(any())).thenReturn(Mono.empty());

        StepVerifier.create(didKeyGeneratorService.generateDidKey())
                .assertNext(did -> {
                    assert did != null;
                    assert did.startsWith("did:key:z");
                })
                .verifyComplete();

        verify(vaultService, times(1)).saveSecret(any());
    }
}