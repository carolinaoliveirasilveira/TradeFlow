package br.com.microservices.choreography.productvalidationservice.core.service;

import br.com.microservices.choreography.productvalidationservice.core.dto.Event;
import br.com.microservices.choreography.productvalidationservice.core.dto.History;
import br.com.microservices.choreography.productvalidationservice.core.dto.OrderProducts;
import br.com.microservices.choreography.productvalidationservice.core.enums.ESagaStatus;
import br.com.microservices.choreography.productvalidationservice.core.model.Validation;
import br.com.microservices.choreography.productvalidationservice.core.repository.ProductRepository;
import br.com.microservices.choreography.productvalidationservice.core.repository.ValidationRepository;
import br.com.microservices.choreography.productvalidationservice.core.saga.SagaExecutionController;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import static br.com.microservices.choreography.productvalidationservice.core.enums.ESagaStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductValidationService {

    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final ProductRepository productRepository;
    private final ValidationRepository validationRepository;
    private final SagaExecutionController sagaExecutionController;

    public void validateExistingProducts(Event event) {
        try {
            checkCurrentValidation(event);
            createValidation(event, true);
            handleSuccess(event);
        } catch (ValidationException ex) {
            log.error("Validation failed: {}", ex.getMessage());
            handleFailCurrentNotExecuted(event, ex.getMessage());
        } catch (Exception ex) {
            log.error("Error processing validation: ", ex);
            handleFailCurrentNotExecuted(event, "Unknown error occurred");
        }
        sagaExecutionController.handleSaga(event);
    }

    private void validateProductsInformed(Event event) {
        if (event.getPayload() == null || event.getPayload().getProducts().isEmpty()) {
            throw new ValidationException("Product list is empty!");
        }
        if (!StringUtils.hasText(event.getPayload().getId()) || !StringUtils.hasText(event.getTransactionId())) {
            throw new ValidationException("OrderID and TransactionID must be informed!");
        }
    }

    private void checkCurrentValidation(Event event) {
        validateProductsInformed(event);

        if (validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");
        }

        event.getPayload().getProducts().forEach(product -> {
            validateProductInformed(product);
            validateExistingProduct(product.getProduct().getCode());
        });
    }

    private void validateProductInformed(OrderProducts product) {
        if (product.getProduct() == null || !StringUtils.hasText(product.getProduct().getCode())) {
            throw new ValidationException("Product must be informed!");
        }
    }

    private void validateExistingProduct(String code) {
        if (!productRepository.existsByCode(code)) {
            throw new ValidationException("Product does not exist in database!");
        }
    }

    private void createValidation(Event event, boolean success) {
        var validation = Validation.builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }

    private void handleSuccess(Event event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Products are validated successfully!");
    }

    private void addHistory(Event event, String message) {
        synchronized (event) { // Evita concorrência
            var history = History.builder()
                    .source(event.getSource())
                    .status(event.getStatus())
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .build();
            event.addToHistory(history);
        }
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to validate products: ".concat(message));
    }

    public void rollbackEvent(Event event) {
        changeValidationToFail(event);
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Rollback executed on product validation!");
        sagaExecutionController.handleSaga(event);
    }

    private void changeValidationToFail(Event event) {
        validationRepository
                .findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                .ifPresentOrElse(validation -> {
                            validation.setSuccess(false);
                            validationRepository.save(validation);
                        },
                        () -> createValidation(event, false));
    }
}
