package es.in2.wallet.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.wallet.application.port.BrokerService;
import es.in2.wallet.application.service.RequestSignedLEARCredentialService;
import es.in2.wallet.domain.exception.CredentialNotAvailableException;
import es.in2.wallet.domain.exception.FailedDeserializingException;
import es.in2.wallet.domain.model.TransactionEntity;
import es.in2.wallet.domain.service.CredentialService;
import es.in2.wallet.domain.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestSignedLEARCredentialServiceImpl implements RequestSignedLEARCredentialService {
    private final BrokerService brokerService;
    private final ObjectMapper objectMapper;
    private final CredentialService credentialService;
    private final UserDataService userDataService;
    @Override
    public Mono<Void> requestSignedLEARCredentialServiceByCredentialId(String processId, String userId, String credentialId) {
        return brokerService.getTransactionThatIsLinkedToACredential(processId,credentialId)
                .flatMap(transactionEntity -> {
                            try {
                                TransactionEntity transaction = objectMapper.readValue(transactionEntity, TransactionEntity.class);
                                return credentialService.getCredentialDomeDeferredCase(
                                        transaction.transactionDataAttribute().value()
                                                .transactionId(),
                                        transaction.transactionDataAttribute().value()
                                                .accessToken(),
                                        transaction.transactionDataAttribute().value()
                                                .deferredEndpoint())
                                        .flatMap(credentialResponse -> {

                                            if (credentialResponse.transactionId() == null){
                                                return brokerService.getCredentialByIdThatBelongToUser(processId,userId,credentialId)
                                                        .flatMap(credentialEntity -> userDataService.updateVCEntityWithSignedFormat(credentialEntity,credentialResponse))
                                                        .flatMap(updatedEntity -> brokerService.updateEntity(processId,credentialId,updatedEntity));
                                            }
                                            else {
                                                return userDataService.updateTransactionWithNewTransactionId(transactionEntity,credentialResponse.transactionId())
                                                        .flatMap(updatedEntity -> brokerService.updateEntity(processId,transaction.id(),updatedEntity))
                                                        .then(Mono.error(new CredentialNotAvailableException("The signed credential it's not available yet")));
                                            }
                                        });
                            }
                            catch (Exception e) {
                                log.error("Error while processing Transaction", e);
                                return Mono.error(new FailedDeserializingException("Error processing Transaction: " + transactionEntity));
                            }
                });
    }
}
