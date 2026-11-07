# FIX-001 — 10 percent coupon gives wrong checkout total

Status: VERIFIED
Reporter: neo254155
Approval recorded for: neo254155

## Proposed fix

CheckoutService.quote divides subtotalCents * couponPercent by 1000, applying one tenth of the intended percentage discount. Change only BigDecimal.valueOf(1000) to BigDecimal.valueOf(100) in CheckoutService.java, preserving HALF_UP cent rounding and validation. For 10000 cents and 10 percent, discount becomes 1000 and total becomes 9000 cents.

## Test evidence

Before: 8 tests; 3 failures; 0 errors; 0 skipped; exit 1
After: 8 tests; 0 failures; 0 errors; 0 skipped; exit 0

Changed source: src/main/java/dev/fixroom/checkout/CheckoutService.java

## Timeline

- 2026-11-07T12:57:09.492741600Z OPEN: neo254155: 10 percent coupon gives wrong checkout total
- 2026-11-07T12:57:29.159244600Z REPRODUCED: 8 tests; 3 failures; 0 errors; 0 skipped; exit 1
- 2026-11-07T12:57:49.775821200Z PROPOSED: CheckoutService.quote divides subtotalCents * couponPercent by 1000, applying one tenth of the intended percentage discount. Change only BigDecimal.valueOf(1000) to BigDecimal.valueOf(100) in CheckoutService.java, preserving HALF_UP cent rounding and validation. For 10000 cents and 10 percent, discount becomes 1000 and total becomes 9000 cents.
- 2026-11-07T12:59:32.505954Z APPROVED: Approval recorded for neo254155; source was unchanged at approval time
- 2026-11-07T13:00:16.430996Z VERIFIED: 8 tests; 0 failures; 0 errors; 0 skipped; exit 0

Approval names are supplied by the caller; this local demo does not authenticate Discord identities.
