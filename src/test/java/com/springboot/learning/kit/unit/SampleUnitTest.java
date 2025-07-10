package com.springboot.learning.kit.unit;

import com.springboot.learning.kit.domain.Order;
import com.springboot.learning.kit.dto.request.OrderRequest;
import com.springboot.learning.kit.service.OrderService;
import com.springboot.learning.kit.transformer.OrderTransformer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SampleUnitTest {

    @Test
    void sampleTest() {
        assertTrue(true, "This is a sample unit test");
    }
}
