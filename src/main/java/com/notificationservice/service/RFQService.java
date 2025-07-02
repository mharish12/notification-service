package com.notificationservice.service;

import com.notificationservice.dto.RFQDto;
import com.notificationservice.entity.RFQ;
import com.notificationservice.entity.RFQEvent;
import com.notificationservice.repository.RFQRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RFQService {
    private final RFQRepository rfqRepository;
    @Qualifier("rfqStateMachine")
    private final StateMachine<RFQ.State, RFQEvent> stateMachine;

    public List<RFQDto> getAll() {
        return rfqRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<RFQ> getRFQById(Long id) {
        return rfqRepository.findById(id);
    }

    public RFQ getOrThrowRFQById(Long id) {
        return getRFQById(id).orElseThrow(() -> new EntityNotFoundException("RFQ not found: " + id));
    }

    public RFQDto getById(Long id) {
        return toDto(getOrThrowRFQById(id));
    }

    public Optional<RFQ> getRFQByReferenceNumber(String referenceNumber) {
        return rfqRepository.findByReferenceNumber(referenceNumber);
    }

    public RFQ getOrThrowRFQByReferenceNumber(String referenceNumber) {
        return getRFQByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new EntityNotFoundException("RFQ not found: " + referenceNumber));
    }

    public RFQDto getByReferenceNumber(String referenceNumber) {
        return toDto(getOrThrowRFQByReferenceNumber(referenceNumber));
    }

    @Transactional
    public RFQDto create(@Valid RFQDto dto) {
        RFQ rfq = toEntity(dto);
        rfq.setId(null); // Ensure new entity
        rfq.setState(RFQ.State.DRAFT);
        return toDto(rfqRepository.save(rfq));
    }

    @Transactional
    public RFQDto update(Long id, @Valid RFQDto dto) {
        RFQ existing = getOrThrowRFQById(id);
        // Use state machine to check if update is allowed
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
            access.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    existing.getState(), null, null, null)).block();
        });
        stateMachine.startReactively().block();
        RFQ.State currentState = stateMachine.getState().getId();
        if (currentState == RFQ.State.ENRICHMENT_COMPLETED || currentState == RFQ.State.CLOSED) {
            throw new IllegalStateException("Cannot update RFQ in state: " + currentState);
        }
        existing.setReferenceNumber(dto.getReferenceNumber());
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setState(dto.getState());
        return toDto(rfqRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        getOrThrowRFQById(id); // Throws if not found
        rfqRepository.deleteById(id);
    }

    // State transition logic using state machine
    @Transactional
    public RFQDto sendEvent(Long id, RFQEvent event) {
        RFQ rfq = getOrThrowRFQById(id);
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
            access.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    rfq.getState(), null, null, null)).block();
        });
        stateMachine.startReactively().block();
        boolean accepted = Objects.requireNonNull(
                stateMachine.sendEvent(
                        Mono.just(
                                MessageBuilder.withPayload(event).build()))
                        .blockFirst())
                .getResultType().equals(StateMachineEventResult.ResultType.ACCEPTED);
        if (accepted && stateMachine.getState() != null) {
            rfq.setState(stateMachine.getState().getId());
            rfqRepository.save(rfq);
        } else {
            throw new IllegalStateException(
                    "Invalid state transition for event: " + event + " from state: " + rfq.getState());
        }
        return toDto(rfq);
    }

    // Mapping methods
    private RFQDto toDto(RFQ rfq) {
        return RFQDto.builder()
                .id(rfq.getId())
                .referenceNumber(rfq.getReferenceNumber())
                .title(rfq.getTitle())
                .description(rfq.getDescription())
                .state(rfq.getState())
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }

    private RFQ toEntity(RFQDto dto) {
        return RFQ.builder()
                .id(dto.getId())
                .referenceNumber(dto.getReferenceNumber())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .state(dto.getState())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}