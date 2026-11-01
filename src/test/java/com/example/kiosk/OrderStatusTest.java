package com.example.kiosk;

import com.example.kiosk.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.example.kiosk.domain.OrderStatus.CANCELLED;
import static com.example.kiosk.domain.OrderStatus.COMPLETED;
import static com.example.kiosk.domain.OrderStatus.PENDING;
import static com.example.kiosk.domain.OrderStatus.PREPARING;
import static com.example.kiosk.domain.OrderStatus.READY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure unit test (no Spring): checks every possible status transition. */
class OrderStatusTest {


    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            PENDING, EnumSet.of(PREPARING, CANCELLED),
            PREPARING, EnumSet.of(READY),
            READY, EnumSet.of(COMPLETED),
            COMPLETED, EnumSet.noneOf(OrderStatus.class),
            CANCELLED, EnumSet.noneOf(OrderStatus.class)
    );

    @Test
    void everyTransitionMatchesTheAllowedTable() {
        for (OrderStatus from : OrderStatus.values()) {
            for (OrderStatus to : OrderStatus.values()) {
                assertEquals(ALLOWED.get(from).contains(to), from.canTransitionTo(to),
                        from + " -> " + to);
            }
        }
    }

    @Test
    void cancellationIsPossibleOnlyFromPending() {
        assertTrue(PENDING.canTransitionTo(CANCELLED));
        assertFalse(PREPARING.canTransitionTo(CANCELLED));
        assertFalse(READY.canTransitionTo(CANCELLED));
        assertFalse(COMPLETED.canTransitionTo(CANCELLED));
    }
}