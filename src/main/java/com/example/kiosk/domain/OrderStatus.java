package com.example.kiosk.domain;

/**
 * Sifarişin həyat dövrü.
 * İcazə verilən ardıcıllıq: PENDING -> PREPARING -> READY -> COMPLETED.
 * CANCELLED statusuna yalnız PENDING mərhələsindən keçmək mümkündür.
 * COMPLETED və CANCELLED son statuslardır: bunlardan başqa statuslara keçid yoxdur.
 */
public enum OrderStatus {
    PENDING,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED;

    /** Bu statusdakı sifariş hədəf statusa keçə bilərsə, "doğru" (true) qaytarır. */
    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PREPARING || target == CANCELLED;
            case PREPARING -> target == READY;
            case READY -> target == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}