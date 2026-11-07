# FixRoom: two-minute demo

The first live workflow has completed and the submitted source is fixed. You can record a walkthrough of that existing Discord conversation and `live-evidence/` without running it again.

For a new live recording, extract `demo/FixRoom-demo-start.zip` into a fresh directory; it contains the original bug and the updated direct API instructions. Stop the existing server before starting that separate demo on the same port. Point a separate demo agent at the extracted directory and use a new demo room so it does not reuse the completed incident. Start its server, keep Switch Console open, and follow the sequence below. Do not overwrite the completed project's source or artifacts.

1. Show Discord's fixroom channel and the FixRoom agent registered in Switch with the project as its directory.
2. Send: `@fixroom-fixer A 10000-cent checkout with a 10% coupon should total 9000 cents, but it totals 9900. Please reproduce it, propose a fix, and wait for my approval.`
3. Show the agent's reproduction output and proposal. The baseline is eight checkout tests, with three failures.
4. Show the awaiting incident with `./scripts/incident.ps1 status -Id FIX-001`. Note that the source is unchanged.
5. In the same Discord thread, send: `@fixroom-fixer APPROVE FIX-001`.
6. Show the real Java source correction and the agent's final verification: eight tests passed, none skipped.
7. Open `artifacts/FIX-001/resolution.md`: timestamped reproduction, proposal, approval and verification.

Suggested narration: "FixRoom brings a coding teammate into Discord through Switch. It reproduces a reported bug, asks the owner before editing, and returns executable test evidence. Switch carries the conversation and owner context; Spring Boot records the workflow and runs the checks."

The sample bug is intentionally seeded. Label it as a demonstration fixture. Do not claim the app fixed a real production incident or that the approval API independently authenticates Discord users.
