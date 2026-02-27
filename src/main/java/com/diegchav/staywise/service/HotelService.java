package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.api.dto.HotelResponse;
import com.diegchav.staywise.api.dto.UpdateHotelRequest;
import com.diegchav.staywise.api.dto.ValidationError;
import com.diegchav.staywise.constant.ErrorMessages;
import com.diegchav.staywise.exception.HotelNotFoundException;
import com.diegchav.staywise.exception.HotelUpdateException;
import com.diegchav.staywise.mapper.HotelMapper;
import com.diegchav.staywise.producer.HotelEventProducer;
import com.diegchav.staywise.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HotelService {
    private final HotelRepository repository;
    private final HotelEventProducer producer;

    public HotelService(HotelRepository repository, HotelEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public List<HotelResponse> getAll() {
        var all = repository.findAll();

        return all.stream().map(HotelMapper::fromEntity).toList();
    }

    public HotelResponse get(UUID id) {
        var found = repository.findById(id).orElseThrow(
                () -> new HotelNotFoundException(ErrorMessages.HOTEL_NOT_FOUND + id)
        );

        return HotelMapper.fromEntity(found);
    }

    @Transactional
    public HotelResponse create(CreateHotelRequest request) {
        var toSave = HotelMapper.toEntity(request, BigDecimal.valueOf(5.0));
        var saved = repository.save(toSave);

        producer.produceHotelEvents(HotelMapper.toEvent(saved));

        return HotelMapper.fromEntity(saved);
    }

    @Transactional
    public HotelResponse update(UUID id, UpdateHotelRequest request) {
        var found = repository.findById(id).orElseThrow(
                () -> new HotelNotFoundException(ErrorMessages.HOTEL_NOT_FOUND + id)
        );

        var errors = new ArrayList<ValidationError>();

        if (request.name() != null) {
            if (!request.name().isEmpty()) {
                found.setName(request.name());
            } else {
                var error = new ValidationError("name", "Name cannot be empty");
                errors.add(error);
            }
        }

        if (request.address() != null) {
            if (!request.address().isEmpty()) {
                found.setAddress(request.address());
            } else {
                var error = new ValidationError("address", "Address cannot be empty");
                errors.add(error);
            }
        }

        if (request.city() != null) {
            if (!request.city().isEmpty()) {
                found.setCity(request.city());
            } else {
                var error = new ValidationError("city", "City cannot be empty");
                errors.add(error);
            }
        }

        if (request.country() != null) {
            if (!request.country().isEmpty()) {
                found.setCountry(request.country());
            } else {
                var error = new ValidationError("country", "Country cannot be empty");
                errors.add(error);
            }
        }

        if (!errors.isEmpty()) {
            throw new HotelUpdateException(errors);
        }

        repository.save(found);

        return HotelMapper.fromEntity(found);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
