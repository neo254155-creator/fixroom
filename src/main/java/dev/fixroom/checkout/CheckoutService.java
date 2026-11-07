package dev.fixroom.checkout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    public record Quote(long subtotalCents, int couponPercent, long discountCents, long totalCents) {}

    public Quote quote(long subtotalCents, int couponPercent) {
        if (subtotalCents < 0 || subtotalCents > 100_000_000) {
            throw new IllegalArgumentException("Subtotal must be between 0 and 100000000 cents");
        }
        if (couponPercent < 0 || couponPercent > 100) {
            throw new IllegalArgumentException("Coupon must be between 0 and 100 percent");
        }
        // Intentionally seeded hackathon bug. The Discord workflow must reproduce it before fixing it.
        long discount = BigDecimal.valueOf(subtotalCents)
                .multiply(BigDecimal.valueOf(couponPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Quote(subtotalCents, couponPercent, discount, subtotalCents - discount);
    }
}
