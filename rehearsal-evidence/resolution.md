# FIX-001 — Isolated rehearsal: coupon error

Status: VERIFIED
Reporter: RehearsalOperator
Approval recorded for: RehearsalOperator

## Proposed fix

Use percent denominator 100 instead of 1000

## Test evidence

Before: 8 tests; 3 failures; 0 errors; 0 skipped; exit 1
After: 8 tests; 0 failures; 0 errors; 0 skipped; exit 0

Changed source: src/main/java/dev/fixroom/checkout/CheckoutService.java

## Timeline

- 2026-11-07T09:56:51.030740100Z OPEN: RehearsalOperator: Isolated rehearsal: coupon error
- 2026-11-07T09:57:00.028300900Z REPRODUCED: 8 tests; 3 failures; 0 errors; 0 skipped; exit 1
- 2026-11-07T09:57:00.053496100Z PROPOSED: Use percent denominator 100 instead of 1000
- 2026-11-07T09:57:00.088602700Z APPROVED: Approval recorded for RehearsalOperator; source was unchanged at approval time
- 2026-11-07T09:57:09.080557500Z VERIFIED: 8 tests; 0 failures; 0 errors; 0 skipped; exit 0

Approval names are supplied by the caller; this local demo does not authenticate Discord identities.
