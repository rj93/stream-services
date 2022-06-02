package com.backbase.stream.compositions.transaction.core.service.impl;

import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.dbs.transaction.api.service.v2.model.TransactionsPostResponseBody;
import com.backbase.stream.compositions.events.egress.event.spec.v1.TransactionsCompletedEvent;
import com.backbase.stream.compositions.events.egress.event.spec.v1.TransactionsFailedEvent;
import com.backbase.stream.compositions.transaction.core.config.TransactionConfigurationProperties;
import com.backbase.stream.compositions.transaction.core.service.TransactionPostIngestionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionPostIngestionServiceImpl implements TransactionPostIngestionService {
    private final EventBus eventBus;

    private final TransactionConfigurationProperties transactionConfigurationProperties;

    @Override
    public void handleSuccess(List<TransactionsPostResponseBody> res) {
        /**
         * TODO: Call to PATCH /cursor/arrangement/{id}
         * This is only for successful completion of Ingestion
         * Status => SUCCESS
         * last_txn_date => current_timestamp
         * last_txn_ids => "id1,id2"
         */
        log.info("Transaction ingestion completed successfully.");
        if (Boolean.TRUE.equals(transactionConfigurationProperties.getEvents().getEnableCompleted())) {
            TransactionsCompletedEvent event = new TransactionsCompletedEvent()
                    .withTransactionIds(res.stream().map(TransactionsPostResponseBody::getId).collect(Collectors.toList()));
            EnvelopedEvent<TransactionsCompletedEvent> envelopedEvent = new EnvelopedEvent<>();
            envelopedEvent.setEvent(event);
            eventBus.emitEvent(envelopedEvent);
        }

        if (log.isDebugEnabled()) {
            log.debug("Ingested Transactions: {}", res);
        }
    }

    @Override
    public Mono<List<TransactionsPostResponseBody>> handleFailure(Throwable error) {
        /**
         * TODO: Call to PATCH /cursor/arrangement/{id}
         * This is only for successful completion of Ingestion
         * Status => FAILED
         * last_txn_date <=/=> NO CHANGE
         * last_txn_ids <=/=> NO CHANGE
         */
        log.error("Transaction ingestion failed. {}", error.getMessage());
        if (Boolean.TRUE.equals(transactionConfigurationProperties.getEvents().getEnableFailed())) {
            TransactionsFailedEvent event = new TransactionsFailedEvent().withEventId(UUID.randomUUID().toString())
                    .withMessage(error.getMessage());
            EnvelopedEvent<TransactionsFailedEvent> envelopedEvent = new EnvelopedEvent<>();
            envelopedEvent.setEvent(event);
            eventBus.emitEvent(envelopedEvent);
        }
        return Mono.empty();
    }
}
