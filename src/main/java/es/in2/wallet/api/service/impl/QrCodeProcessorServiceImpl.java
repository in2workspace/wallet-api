package es.in2.wallet.api.service.impl;

import es.in2.wallet.api.ebsi.comformance.facade.EbsiCredentialIssuanceServiceFacade;
import es.in2.wallet.api.exception.NoSuchQrContentException;
import es.in2.wallet.api.facade.CredentialIssuanceServiceFacade;
import es.in2.wallet.api.model.QrType;
import es.in2.wallet.api.service.QrCodeProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.wallet.api.model.QrType.*;
import static es.in2.wallet.api.util.MessageUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class QrCodeProcessorServiceImpl implements QrCodeProcessorService {

    private final CredentialIssuanceServiceFacade credentialIssuanceServiceFacade;
    private final EbsiCredentialIssuanceServiceFacade ebsiCredentialIssuanceServiceFacade;

    @Override
    public Mono<Object> processQrContent(String processId, String authorizationToken, String qrContent) {
        log.debug("ProcessID: {} - Processing QR content: {}", processId, qrContent);
        return identifyQrContentType(qrContent)
                .flatMap(qrType -> {
                    switch (qrType) {
                        case CREDENTIAL_OFFER_URI, OPENID_CREDENTIAL_OFFER: {
                            log.info("ProcessID: {} - Processing a Verifiable Credential Offer URI", processId);
                            return credentialIssuanceServiceFacade.identifyAuthMethod(processId, authorizationToken, qrContent)
                                    .doOnSuccess(credential -> log.info("ProcessID: {} - Credential Issued: {}", processId, credential))
                                    .doOnError(e -> log.error("ProcessID: {} - Error while issuing credential: {}", processId, e.getMessage()));
                        }
                        case EBSI_CREDENTIAL_OFFER: {
                            log.info("ProcessID: {} - Processing a Verifiable Credential Offer URI in EBSI Format", processId);
                            return ebsiCredentialIssuanceServiceFacade.identifyAuthMethod(processId, authorizationToken, qrContent)
                                    .doOnSuccess(credential -> log.info("ProcessID: {} - Credential Issued: {}", processId, credential))
                                    .doOnError(e -> log.error("ProcessID: {} - Error while issuing credential: {}", processId, e.getMessage()));
                        }
                        case OPENID_AUTHENTICATION_REQUEST: {
                            log.info("ProcessID: {} - Processing an Authentication Request", processId);
                            return Mono.error(new NoSuchQrContentException("OpenID Authentication Request not implemented yet"));
                        }
                        case UNKNOWN: {
                            String errorMessage = "The received QR content cannot be processed";
                            log.warn(errorMessage);
                            return Mono.error(new NoSuchQrContentException(errorMessage));
                        }
                        default: {
                            return Mono.empty();
                        }
                    }
                });
    }

    private Mono<QrType> identifyQrContentType(String qrContent) {
        return Mono.fromSupplier(() -> {
            if (LOGIN_REQUEST_PATTERN.matcher(qrContent).matches()) {
                return VC_LOGIN_REQUEST;
            } else if (CREDENTIAL_OFFER_PATTERN.matcher(qrContent).matches()) {
                return QrType.CREDENTIAL_OFFER_URI;
            } else if (EBSI_CREDENTIAL_OFFER_PATTERN.matcher(qrContent).matches()){
                return EBSI_CREDENTIAL_OFFER;
            } else if (OPENID_CREDENTIAL_OFFER_PATTERN.matcher(qrContent).matches()) {
                return OPENID_CREDENTIAL_OFFER;
            } else if (OPENID_AUTHENTICATION_REQUEST_PATTERN.matcher(qrContent).matches()) {
                return OPENID_AUTHENTICATION_REQUEST;
            } else {
                log.warn("Unknown QR content type: {}", qrContent);
                return UNKNOWN;
            }
        });
    }


}