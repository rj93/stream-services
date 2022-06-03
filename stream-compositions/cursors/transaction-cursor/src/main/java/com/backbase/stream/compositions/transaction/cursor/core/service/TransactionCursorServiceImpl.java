package com.backbase.stream.compositions.transaction.cursor.core.service;

import com.backbase.stream.compositions.transaction.cursor.core.domain.TransactionCursorEntity;
import com.backbase.stream.compositions.transaction.cursor.core.mapper.TransactionCursorMapper;
import com.backbase.stream.compositions.transaction.cursor.core.repository.TransactionCursorRepository;
import com.backbase.stream.compositions.transaction.cursor.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionCursorServiceImpl implements TransactionCursorService {

  private final TransactionCursorRepository transactionCursorRepository;
  private final TransactionCursorMapper mapper;

  /**
   * The Service to filter the cursor based on arrangementId
   *
   * @param arrangementId ArrangementId of the Cursor
   * @return TransactionCursorResponse
   */
  @Override
  public Mono<ResponseEntity<TransactionCursorResponse>> findByArrangementId(String arrangementId) {
    log.debug("TransactionCursorService :: findByArrangementId {} ", arrangementId);
    return transactionCursorRepository.findByArrangementId(arrangementId)
        .map(this::mapModelToResponse)
        .orElse(
            Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
  }

  /**
   * The Service to delete the cursor based on either id or arrangementId
   *
   * @param transactionCursorDeleteRequest TransactionDeleteRequest Payload
   * @return Response Entity
   */
  @Override
  public Mono<ResponseEntity<Void>> deleteCursor(
      Mono<TransactionCursorDeleteRequest> transactionCursorDeleteRequest) {
    transactionCursorDeleteRequest.map(transactionCursorRepository::deleteCursor).subscribe();
    return Mono.empty();
  }

  /**
   * The Service to filter the cursor based on id
   *
   * @param id Id of the Cursor
   * @return TransactionCursorResponse
   */
  @Override
  public Mono<ResponseEntity<TransactionCursorResponse>> findById(String id) {
    log.debug("TransactionCursorService :: findById {} ", id);
    return transactionCursorRepository.findById(id)
        .map(this::mapModelToResponse)
        .orElse(
            Mono.just(new ResponseEntity<>(new TransactionCursorResponse(), HttpStatus.NOT_FOUND)));
  }

  /**
   * The Service to upsert a cursor
   *
   * @param transactionCursorUpsertRequest TransactionCursorUpsertRequest payload
   * @return TransactionCursorUpsertResponse
   */
  @Override
  public Mono<ResponseEntity<TransactionCursorUpsertResponse>> upsertCursor(
      Mono<TransactionCursorUpsertRequest> transactionCursorUpsertRequest) {
    return transactionCursorUpsertRequest.map(mapper::mapToDomain)
        .flatMap(transactionCursorRepository::upsertCursor)
        .doOnNext(id -> log.info("Id is {}", id))
        .map(this::mapUpsertToResponse);
  }

  /**
   * The Service to patch the cursor to update lastTxnIds, lastTxnDate & status based on
   * arrangementId
   *
   * @param arrangementId                 ArrangementId of the Cursor
   * @param transactionCursorPatchRequest TransactionCursorPatchRequest Payload
   * @return Response Entity
   */
  @Override
  public Mono<ResponseEntity<Void>> patchByArrangementId(String arrangementId,
      Mono<TransactionCursorPatchRequest> transactionCursorPatchRequest) {
    transactionCursorPatchRequest.map(transactionCursorPatchReq
        -> {
      try {
        return transactionCursorRepository
            .patchByArrangementId(arrangementId, transactionCursorPatchReq);
      } catch (ParseException parseException) {
        throw new RuntimeException(parseException);
      }
    }).doOnError(throwable -> log
        .error("TransactionCursorServiceImpl patchByArrangementId Exception: {}",
            throwable.getMessage()))
        .subscribe();
    return Mono.empty();
  }

  /**
   * Transform domain to model response
   *
   * @param response Model to CursorResponse
   * @return TransactionCursorResponse
   */
  private Mono<ResponseEntity<TransactionCursorResponse>> mapModelToResponse(
      TransactionCursorEntity response) {
    return Mono.just(new ResponseEntity<>(mapper.mapToModel(response), HttpStatus.OK));
  }

  /**
   * Create the response for Upsert Cursor
   *
   * @param id Cursor Unique Id reference
   * @return TransactionCursorUpsertResponse
   */
  private ResponseEntity<TransactionCursorUpsertResponse> mapUpsertToResponse(String id) {
    return new ResponseEntity<>(
        new TransactionCursorUpsertResponse()
            .withId(id),
        HttpStatus.CREATED);
  }

}