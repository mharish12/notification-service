package com.notificationservice.controller;

import com.notificationservice.dto.RFQDto;
import com.notificationservice.entity.RFQ;
import com.notificationservice.entity.RFQEvent;
import com.notificationservice.service.RFQService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfqs")
@RequiredArgsConstructor
public class RFQController {
    private final RFQService rfqService;

    @GetMapping
    public List<RFQDto> getAll() {
        return rfqService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RFQDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rfqService.getById(id));
    }

    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<RFQDto> getByReferenceNumber(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(rfqService.getByReferenceNumber(referenceNumber));
    }

    @PostMapping
    public ResponseEntity<RFQDto> create(@Valid @RequestBody RFQDto dto) {
        return ResponseEntity.ok(rfqService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RFQDto> update(@PathVariable Long id, @Valid @RequestBody RFQDto dto) {
        return ResponseEntity.ok(rfqService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rfqService.delete(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/{id}/state")
//    public ResponseEntity<RFQDto> changeState(@PathVariable Long id, @RequestParam RFQ.State state) {
//        return ResponseEntity.ok(rfqService.changeState(id, state));
//    }

    @PostMapping("/{id}/event")
    public ResponseEntity<RFQDto> sendEvent(@PathVariable Long id, @RequestParam RFQEvent event) {
        return ResponseEntity.ok(rfqService.sendEvent(id, event));
    }
}