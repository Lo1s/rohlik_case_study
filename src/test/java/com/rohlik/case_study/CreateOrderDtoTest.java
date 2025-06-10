package com.rohlik.case_study;

import com.rohlik.case_study.dto.CreateOrderDto;
import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateOrderDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldFailValidation_whenNoItems() {
        CreateOrderDto dto = new CreateOrderDto();
        dto.setItems(null);

        Set<ConstraintViolation<CreateOrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}