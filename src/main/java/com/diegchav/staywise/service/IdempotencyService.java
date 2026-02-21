package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.constant.ErrorMessages;
import com.diegchav.staywise.exception.IdempotencyProcessingException;
import com.diegchav.staywise.repository.IdempotencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {
    private final IdempotencyRepository repository;

    public IdempotencyService(IdempotencyRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected BookingResponse fetchStoredResponse(String idempotencyKey) {
        var storedResponse = repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IdempotencyProcessingException(ErrorMessages.IDEMPOTENCY_RECORD_NOT_FOUND));

        return storedResponse.getResponseBody();
    }
}
