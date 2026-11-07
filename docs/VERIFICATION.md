# Verified implementation, November 7, 2026

The project was checked on Windows with Java 23.0.1, Spring Boot 4.1.2 and the bundled Maven Wrapper. The first section records the initial isolated rehearsal; the live run is recorded below.

- Workflow unit tests and Spring context startup: **9 tests passed**, no failures or skips.
- An isolated source copy was launched on loopback port 8088 for a real HTTP/Maven rehearsal.
- Seeded checkout HTTP response: **9900** cents for a 10000-cent subtotal and 10-percent coupon.
- Invalid negative subtotal: **HTTP 400**.
- Reproduction API launched Maven and parsed a new Surefire report: **8 tests, 3 failures, 0 errors, 0 skipped**.
- Verification before approval: **HTTP 409**.
- Wrong approval confirmation: **HTTP 400**.
- A synthetic test-operator approval was recorded only in the isolated rehearsal.
- The denominator was corrected only in that source copy.
- Verification API launched Maven again: **8 tests passed**, no failures, errors or skips.
- Full repaired suite and executable JAR build: **17 tests passed**.
- After stopping, rebuilding and restarting the rehearsal server: checkout response **9000** cents; incident persisted as **VERIFIED**.
- The original delivered project was left with the seeded bug and no demo incidents, then started on **127.0.0.1:8087**; health returned UP and quote returned the expected seeded 9900.
- Windows PowerShell start and incident health helper scripts were exercised.

Raw rehearsal logs and resolution are in `rehearsal-evidence/`. They are explicitly **automated local rehearsal evidence**, separate from the subsequently completed live incident.

Windows packaging check found that a running JAR must be stopped before rebuilding it. The README documents the stop/rebuild/start sequence.

## Completed live Discord/Switch incident

The actual agent run created FIX-001 and persisted the following timeline on November 7, 2026 (times below are IST):

- 18:27:09 — OPEN, reported by neo254155.
- 18:27:29 — REPRODUCED: 8 checkout tests, 3 failures, 0 errors, 0 skipped.
- 18:27:49 — PROPOSED: correct the percentage denominator from 1000 to 100.
- 18:29:32 — APPROVED, approval recorded for neo254155; checkout source remained unchanged at approval time.
- 18:30:16 — VERIFIED: 8 checkout tests, 0 failures, 0 errors, 0 skipped, exit code 0.

The actual checkout source now uses BigDecimal.valueOf(100). The completed incident, before/after command logs and resolution were preserved under `live-evidence/`. The full repaired project was then packaged with **17 tests passing**. The initial broken source archive was retained separately as `FixRoom-demo-start.zip` so a future demonstration does not overwrite the live record.

As described in the README, approval identities are supplied by the caller; the API itself does not independently authenticate Discord identity. The saved state demonstrates the application's recorded sequence, and the chat recording/screenshots provide the associated human interaction.
