package com.backbase.stream.compositions.product.http;

import com.backbase.stream.compositions.product.api.ProductCompositionApi;
import com.backbase.stream.compositions.product.core.mapper.ProductGroupMapper;
import com.backbase.stream.compositions.product.core.model.ProductIngestPullRequest;
import com.backbase.stream.compositions.product.core.model.ProductIngestPushRequest;
import com.backbase.stream.compositions.product.core.model.ProductIngestResponse;
import com.backbase.stream.compositions.product.core.service.ProductIngestionService;
import com.backbase.stream.compositions.product.api.model.ProductIngestionResponse;
import com.backbase.stream.compositions.product.api.model.ProductPullIngestionRequest;
import com.backbase.stream.compositions.product.api.model.ProductPushIngestionRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@Slf4j
public class ProductController implements ProductCompositionApi {
    private final ProductIngestionService productIngestionService;
    private final ProductGroupMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<Void>> pullIngestProductAsync(
            @Valid Mono<ProductPullIngestionRequest> pullIngestionRequest, ServerWebExchange exchange) {

        Mono.fromCallable(() -> pullIngestionRequest.map(this::buildPullRequest)
                .flatMap(productIngestionService::ingestPull)
                .map(this::mapIngestionToResponse)).subscribeOn(Schedulers.boundedElastic()).subscribe();

        return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<ProductIngestionResponse>> pullIngestProduct(
            @Valid Mono<ProductPullIngestionRequest> pullIngestionRequest, ServerWebExchange exchange) {

        return pullIngestionRequest.map(this::buildPullRequest)
                .flatMap(productIngestionService::ingestPull)
                .map(this::mapIngestionToResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<Void>> pushIngestProductAsync(
            @Valid Mono<ProductPushIngestionRequest> pushIngestionRequest, ServerWebExchange exchange) {
        Mono.fromCallable(() -> pushIngestionRequest.map(this::buildPushRequest)
                .flatMap(productIngestionService::ingestPush)
                .map(this::mapIngestionToResponse))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

         return Mono.empty();
    }

    @Override
    public Mono<ResponseEntity<ProductIngestionResponse>> pushIngestProduct(
            @Valid Mono<ProductPushIngestionRequest> pushIngestionRequest, ServerWebExchange exchange) {
        return pushIngestionRequest.map(this::buildPushRequest)
                .flatMap(productIngestionService::ingestPush)
                .map(this::mapIngestionToResponse);
    }

    /**
     * Builds ingestion request for downstream service.
     *
     * @param request PullIngestionRequest
     * @return ProductIngestPullRequest
     */
    private ProductIngestPullRequest buildPullRequest(ProductPullIngestionRequest request) {
        return ProductIngestPullRequest
                .builder()
                .legalEntityExternalId(request.getLegalEntityExternalId())
                .serviceAgreementExternalId(request.getServiceAgreementExternalId())
                .serviceAgreementInternalId(request.getServiceAgreementInternalId())
                .userExternalId(request.getUserExternalId())
                .additionalParameters(request.getAdditionalParameters())
                .build();
    }

    /**
     * Builds ingestion request for downstream service.
     *
     * @param request PushIngestionRequest
     * @return ProductIngestPushRequest
     */
    private ProductIngestPushRequest buildPushRequest(ProductPushIngestionRequest request) {
        return ProductIngestPushRequest.builder()
                .productGroup(mapper.mapCompositionToStream(request.getProductGgroup()))
                .build();
    }

    /**
     * Builds ingestion response for API endpoint.
     *
     * @param response ProductCatalogIngestResponse
     * @return IngestionResponse
     */
    private ResponseEntity<ProductIngestionResponse> mapIngestionToResponse(ProductIngestResponse response) {
        return new ResponseEntity<>(
                new ProductIngestionResponse()
                        .withProductGgroup(mapper.mapStreamToComposition(response.getProductGroup())),
                HttpStatus.CREATED);
    }
}