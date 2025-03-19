package br.com.microservices.choreography.productvalidationservice.core.dto;

import br.com.microservices.choreography.productvalidationservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private String id;
    private String orderId;
    private String transactionId;
    private Order payload;
    private String source;
    private ESagaStatus status;
    @Builder.Default
    private List<History> eventHistory = new ArrayList<>();
    private LocalDateTime createdAt;

    public void addToHistory(History history) {

        eventHistory.add(history);
    }
}
