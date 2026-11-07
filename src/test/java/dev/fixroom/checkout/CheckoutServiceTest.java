package dev.fixroom.checkout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheckoutServiceTest {
    private final CheckoutService checkout = new CheckoutService();

    @Test void noCouponKeepsOriginalPrice() { assertEquals(10000, checkout.quote(10000, 0).totalCents()); }
    @Test void tenPercentCouponDeductsTenPercent() { assertEquals(9000, checkout.quote(10000, 10).totalCents()); }
    @Test void fullDiscountMakesOrderFree() { assertEquals(0, checkout.quote(10000, 100).totalCents()); }
    @Test void roundsDiscountToNearestCent() { assertEquals(849, checkout.quote(999, 15).totalCents()); }
    @Test void emptyCartStaysZero() { assertEquals(0, checkout.quote(0, 10).totalCents()); }
    @Test void negativeSubtotalIsRejected() { assertThrows(IllegalArgumentException.class, () -> checkout.quote(-1, 10)); }
    @Test void excessiveCouponIsRejected() { assertThrows(IllegalArgumentException.class, () -> checkout.quote(10000, 101)); }
    @Test void negativeCouponIsRejected() { assertThrows(IllegalArgumentException.class, () -> checkout.quote(10000, -1)); }
}
