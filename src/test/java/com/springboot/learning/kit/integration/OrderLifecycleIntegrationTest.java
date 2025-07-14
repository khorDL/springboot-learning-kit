package com.springboot.learning.kit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.springboot.learning.kit.config.BaseIntegrationTest;
import com.springboot.learning.kit.domain.CustomerAddress;
import com.springboot.learning.kit.domain.CustomerDetails;
import com.springboot.learning.kit.domain.Order;
import com.springboot.learning.kit.domain.OrderItem;
import com.springboot.learning.kit.dto.request.CustomerAddressRequest;
import com.springboot.learning.kit.dto.request.CustomerDetailsRequest;
import com.springboot.learning.kit.dto.request.OrderItemRequest;
import com.springboot.learning.kit.dto.request.OrderRequest;
import com.springboot.learning.kit.dto.response.OrderStatusResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

// @Sql(scripts = "/sql/cleanup_order_tables.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional // rollback after test
@TestMethodOrder(OrderAnnotation.class)
public class OrderLifecycleIntegrationTest extends BaseIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(OrderLifecycleIntegrationTest.class);
    private static final long TEST_ORDER_UUID = 987654567L;
    private static OrderRequest submittedOrder;

    @Test
    @org.junit.jupiter.api.Order(1)
    void submitOrder_ShouldCreateOrderInDatabase() {
        log.info("==> [1] Submitting Order to API");

        String orderCreationUrl = getBaseUrl() + "/order/submit";
        submittedOrder = createMultipleOrderRequest();
        // Assert successful HTTP response
        ResponseEntity<String> response = restTemplate.postForEntity(orderCreationUrl, submittedOrder, String.class);

        // query database to verify
        assertAll(
                "API response and database validations",
                () -> assertEquals(200, response.getStatusCode().value(), "Response status should be 200 OK"),
                () -> assertNotNull(response.getBody(), "Response body should not be null"),
                () -> assertTrue(
                        response.getBody().toLowerCase().contains("order submitted"),
                        "Response body should contain success message (case insensitive)"),
                () -> assertOrderPersistedCorrectly(submittedOrder.getUUID()),
                () -> assertCustomerDetailsPersisted(submittedOrder.getUUID()),
                () -> assertCustomerAddressPersisted(submittedOrder.getUUID()),
                () -> assertOrderItemsPersisted(submittedOrder.getUUID()));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void getOrderStatus_ShouldReturnCompleteOrderInformation() {
        log.info("==> [2] Fetching order status");

        ResponseEntity<OrderStatusResponse> response = getOrderStatusResponseResponseEntity(TEST_ORDER_UUID);

        // 3. Assert that the request was successful
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected 200 OK"),
                () -> assertNotNull(response.getBody(), "Response body should not be null"));

        // 4. Extract response body
        OrderStatusResponse status = response.getBody();

        // 5. Get actual database entities using the refactored helpers
        assertAll(
                "API response and database validations",
                () -> assertOrderPersistedCorrectly(submittedOrder.getUUID()),
                () -> assertCustomerDetailsPersisted(submittedOrder.getUUID()),
                () -> assertCustomerAddressPersisted(submittedOrder.getUUID()),
                () -> assertOrderItemsPersisted(submittedOrder.getUUID()));

        BigDecimal calculatedTotal = submittedOrder.getOrderItems().stream()
                .map(item -> item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Assert that the order details in the response match the submitted order data
        assertAll(
                "API response validations",
                () -> assertEquals(submittedOrder.getUUID(), status.getOrderId(), "Order ID should match"),
                () -> assertEquals(submittedOrder.getOrderType(), status.getOrderType(), "Order type should match"),
                () -> assertEquals(
                        submittedOrder.getOrderAmount().setScale(2),
                        calculatedTotal.setScale(2),
                        "Total amount should match"),
                () -> assertEquals(submittedOrder.getCurrency(), "USD", "Order currency should match"));

        // 8. Validate each order item in the response against the originally submitted order items
        for (int i = 0; i < submittedOrder.getOrderItems().size(); i++) {
            var expectedItem = submittedOrder.getOrderItems().get(i);
            var actualItem = status.getItems().get(i);

            assertAll(
                    "Item " + i + " validations",
                    () -> assertEquals(
                            expectedItem.getProductId(), actualItem.getProductId(), "Product ID should match"),
                    () -> assertEquals(expectedItem.getQuantity(), actualItem.getQuantity(), "Quantity should match"),
                    () -> assertNotNull(actualItem.getStatus(), "Item status should not be null"));
        }
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void getOrderStatus_WithNonExistentOrder_ShouldReturn404() {
        log.info("==> [3] Testing non-existent order");
        String url = getBaseUrl() + "/order/status/999999"; // Non-existent order ID

        HttpClientErrorException.NotFound thrown = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.exchange(url, HttpMethod.GET, null, String.class),
                "Expected 404 Not Found");

        assertTrue(
                thrown.getResponseBodyAsString().contains("Order not found"),
                "Response should contain 'Order not found'");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void submitMultipleOrders_ShouldMaintainDataIntegrity() {
        log.info("==> [4] Submitting multiple orders concurrently");

        int orderCount = 3;
        String orderCreationUrl = getBaseUrl() + "/order/submit";

        IntStream.range(0, orderCount).parallel().forEach(i -> {
            long uuid = 800000L + i;
            OrderRequest request = createMultipleOrderRequest();
            request.setUUID(uuid);

            ResponseEntity<String> response = restTemplate.postForEntity(orderCreationUrl, request, String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Order " + uuid + " should return 200 OK");
            assertTrue(
                    response.getBody().toLowerCase().contains("submitted"), "Order " + uuid + " should be confirmed");
        });
    }

    private @NotNull ResponseEntity<OrderStatusResponse> getOrderStatusResponseResponseEntity(Long uuid) {
        // 1. Prepare the URL using the UUID from the static variable
        String url = getBaseUrl() + "/order/status/" + uuid;

        // 2. Perform a GET request and map response to OrderStatusResponse
        ResponseEntity<OrderStatusResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        return response;
    }

    private ResponseEntity<String> getOrderStatusRawResponseEntity(Long uuid) {
        String url = getBaseUrl() + "/order/status/" + uuid;
        return restTemplate.exchange(url, HttpMethod.GET, null, String.class);
    }

    private OrderRequest createMultipleOrderRequest() {
        OrderRequest order = new OrderRequest();
        order.setUUID(TEST_ORDER_UUID);
        order.setOrderType("ONLINE");

        CustomerDetailsRequest customerDetails = new CustomerDetailsRequest();
        customerDetails.setName("John Doe");
        customerDetails.setEmail("john.doe@gmail.com");
        customerDetails.setPhone("+1234567890");
        order.setCustomerDetails(customerDetails);

        CustomerAddressRequest customerAddress = new CustomerAddressRequest();
        customerAddress.setStreet("123 Main St");
        customerAddress.setCity("New York");
        customerAddress.setState("NY");
        customerAddress.setZipCode("10001");
        customerAddress.setCountry("USA");
        order.setCustomerAddress(customerAddress);

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setPricePerUnit(BigDecimal.valueOf(29.99));

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);
        item2.setPricePerUnit(BigDecimal.valueOf(49.99));

        OrderItemRequest item3 = new OrderItemRequest();
        item3.setProductId(3L);
        item3.setQuantity(5);
        item3.setPricePerUnit(BigDecimal.valueOf(99.01));

        order.setOrderItems(List.of(item1, item2, item3));

        BigDecimal total = item1.getPricePerUnit()
                .multiply(BigDecimal.valueOf(item1.getQuantity()))
                .add(item2.getPricePerUnit().multiply(BigDecimal.valueOf(item2.getQuantity())))
                .add(item3.getPricePerUnit().multiply(BigDecimal.valueOf(item3.getQuantity())));

        order.setOrderAmount(total);
        order.setCurrency("USD");

        return order;
    }

    private void assertOrderPersistedCorrectly(Long uuid) {
        Order order = entityManager
                .createQuery("SELECT o FROM Order o WHERE o.uuid = :uuid", Order.class)
                .setParameter("uuid", uuid)
                .getSingleResult();

        assertThat(order).isNotNull();
        assertThat(order.getUuid()).isEqualTo(submittedOrder.getUUID());
        assertThat(order.getOrderType().toString()).isEqualTo(submittedOrder.getOrderType());

        log.info("Order entity validated for UUID {}", uuid);
    }

    private void assertCustomerDetailsPersisted(Long uuid) {
        Long customerDetailsId = entityManager
                .createQuery("SELECT o.customerDetailsId FROM Order o WHERE o.uuid = :uuid", Long.class)
                .setParameter("uuid", uuid)
                .getSingleResult();

        CustomerDetails details = entityManager.find(CustomerDetails.class, customerDetailsId);

        assertThat(details).isNotNull();
        assertThat(details.getName()).isEqualTo("John Doe");

        log.info("CustomerDetails entity validated for UUID {}", uuid);
    }

    private void assertCustomerAddressPersisted(Long uuid) {
        Long customerAddressId = ((Number) entityManager
                        .createNativeQuery("SELECT customer_address_id FROM orders WHERE uuid = :uuid")
                        .setParameter("uuid", uuid)
                        .getSingleResult())
                .longValue();

        CustomerAddress address = entityManager.find(CustomerAddress.class, customerAddressId);

        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("123 Main St");

        log.info("CustomerAddress entity validated for UUID {}", uuid);
    }

    private void assertOrderItemsPersisted(Long uuid) {
        List<OrderItem> items = entityManager
                .createQuery("SELECT i FROM OrderItem i WHERE i.orderId = :uuid", OrderItem.class)
                .setParameter("uuid", uuid)
                .getResultList();

        assertThat(items).hasSize(3);

        log.info("OrderItems entity validated: {} items for UUID {}", items.size(), uuid);
    }
}
