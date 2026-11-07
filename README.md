# FixRoom

A human and a coding agent fix a Spring Boot checkout bug together in Discord, connected through Switch.

**Report → real failing JUnit tests → proposed correction → human approval → source repair → passing tests and a saved resolution.**

**Live run completed:** FIX-001 is VERIFIED. The Discord/Switch agent reproduced three checkout test failures, recorded the owner's approval, corrected the denominator, and passed all eight checkout tests. The final source also passes the full 17-test suite. See the [live evidence](live-evidence/) and [verification notes](docs/VERIFICATION.md).

The current checkout source includes the repair. To reproduce the original live demonstration, download and extract the included [demo starter](demo/FixRoom-demo-start.zip) into a new folder, point a separate demo agent at that folder, and follow the [demo script](docs/DEMO-SCRIPT.md). Do not reset the completed live project or overwrite its evidence.

## What is implemented

- Spring Boot 4.1.2, Java 21+, Maven Wrapper; no database, paid API integration or npm dependency required by this project.
- Checkout REST endpoint repaired through the live workflow; an explicitly seeded starting version is supplied separately for reproduction.
- Incident API with persisted OPEN → REPRODUCED → AWAITING_APPROVAL → APPROVED → VERIFIED states.
- Reproduction and verification launch a fixed Maven/JUnit command and parse fresh Surefire XML, retaining the full command output.
- SHA-256 checks reject source changes before approval and changes to tests, workflow files or build files during the incident.
- Resolution report and timestamped event history survive application restarts.
- Switch agent and room instructions, PowerShell helper commands and demo script.

## Run on Windows

Requirements: JDK 21 or later, internet access for Maven's first dependency download, Switch Console with a signed-in coding provider, and a connected Discord room. Java 23 works on the development machine.

```powershell
cd '<your FixRoom directory>'
.\scripts\start.ps1
```

This packages the repaired app, runs all tests, then starts it in the foreground. Keep that terminal open. The API binds to `127.0.0.1:8087` only. The separately archived broken demo starter uses a startup build that skips its deliberately failing checkout tests; reproduction then captures them through the API.

From a second PowerShell terminal:

```powershell
Invoke-RestMethod -Uri http://127.0.0.1:8087/api/health
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8087/api/checkout/quote -ContentType application/json -Body '{"subtotalCents":10000,"couponPercent":10}'
```

Current repaired response: totalCents 9000. The original seeded baseline returned 9900.

If your terminal blocks `.ps1` files, use the Maven wrapper directly instead of `scripts/start.ps1`: set `JAVA_HOME` to your JDK directory, run `./mvnw.cmd package`, then `java -jar target/fixroom-0.0.1-SNAPSHOT.jar`. Direct API commands are in [API-COMMANDS.md](docs/API-COMMANDS.md).

On macOS/Linux, set JAVA_HOME to a JDK, run `sh ./mvnw package`, then `java -jar target/fixroom-0.0.1-SNAPSHOT.jar`. The Java runner supports both Windows and Unix. PowerShell helper scripts need PowerShell; the REST endpoints can also be called using curl.

## Connect the coding agent through Switch

1. Switch Console → Your Agents → add an agent.
2. Name: `fixroom-fixer`. Description: `Reproduces checkout bugs, waits for human approval, then repairs and verifies them.`
3. Run location: this computer. **Directory: this repository's absolute path**, not switch-buddy's folder.
4. Provider: Codex (or another working coding provider). Leave permission bypass off. Restrict who may instruct it to the owner and link that owner's Discord account.
5. Agent instructions: paste the text in `docs/AGENT-INSTRUCTIONS.md`. Put `docs/ROOM-INSTRUCTIONS.md` in the room's Instructions field.
6. In the Discord room send `!invite-agent @fixroom-fixer` and wait for confirmation.
7. Mention the agent with the bug report in `docs/DEMO-SCRIPT.md`.

Some Switch versions can create the dedicated agent through switch-buddy. Give it this directory and the instructions above, and verify the new agent appears under Your Agents before inviting it.

Switch is the project's conversation and agent-runtime integration: Discord messages go through its room to the coding agent, and the agent posts progress and approval requests back through Switch. This repository does not implement a separate Discord bot or send model API calls directly. The Spring API can be tested independently; the demonstrated human-agent chat workflow depends on Switch.

Official setup: https://docs.switchagents.ai/switch-rooms/getting-started/onboard-your-agents

## API and commands

If the Switch session says `.ps1` scripts are disabled (`UnauthorizedAccess`), use the direct built-in `Invoke-RestMethod` commands in `docs/API-COMMANDS.md`. The agent instructions use those commands. Do not change or bypass PowerShell execution policy to run the helper.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | /api/health | Readiness |
| POST | /api/checkout/quote | Checkout demo, subtotalCents and couponPercent |
| POST | /api/incidents | Open incident: title, reporter |
| GET | /api/incidents | List incidents |
| GET | /api/incidents/{id} | State and evidence |
| POST | /api/incidents/{id}/reproduce | Run original checkout tests, require assertion failures |
| POST | /api/incidents/{id}/propose | Record summary, enter approval wait |
| POST | /api/incidents/{id}/approve | Record approvedBy and exact confirmation |
| POST | /api/incidents/{id}/verify | Run original tests and verify the source repair |
| GET | /api/incidents/{id}/report | Human-readable resolution/timeline |

Example commands (the agent normally runs these in response to Discord messages):

```powershell
.\scripts\incident.ps1 open -Title '10 percent coupon gives wrong total' -By 'NEOPAT'
.\scripts\incident.ps1 reproduce -Id FIX-001
.\scripts\incident.ps1 propose -Id FIX-001 -Summary '<diagnosed cause and minimal change>'
# Stop here and wait for a separate human approval message.
.\scripts\incident.ps1 approve -Id FIX-001 -By 'NEOPAT' -Confirmation 'APPROVE FIX-001'
# The coding agent now makes the approved source change.
.\scripts\incident.ps1 verify -Id FIX-001
.\scripts\incident.ps1 report -Id FIX-001
```

Use the returned id, not always FIX-001. Artifacts are written under `artifacts/<id>/`; `incidents.json` stores the state. No bot tokens or provider credentials belong in this repository.

## Tests and archived baseline

```powershell
$env:JAVA_HOME = '<your JDK directory>'
# Workflow behavior and Spring startup should pass:
.\mvnw.cmd '-Dtest=WorkflowServiceTest,FixRoomApplicationTests' test
# Eight checkout tests now pass in the repaired submission:
.\mvnw.cmd '-Dtest=CheckoutServiceTest' test
```

Run `./mvnw.cmd test` for the full 17-test suite. In the separately archived demo starter, three of the eight checkout tests deliberately fail before the approved repair. Reproduction/verification endpoints run only the checkout tests; workflow behavior is tested separately. The test runner times out after 180 seconds. Build or dependency failures do not count as a reproduced bug.

## Limits and honest scope

- The approval API is a local record, **not an identity/authentication boundary**. The caller supplies approvedBy; Switch owner restrictions and the agent instructions govern whose chat approval is accepted. No production authorization claim is made.
- Hashes detect ordinary changes during the workflow; they do not stop an agent with filesystem access from editing files or evidence. Run on a demo repository only.
- The API is local and unauthenticated. It runs a fixed test command in this project; keep it bound to loopback. Test dependencies/code must be trusted.
- Verification proves the repaired source passes tests. After repair, stop the foreground server with Ctrl+C, then run `./scripts/start.ps1` again to rebuild and load the corrected HTTP endpoint. On Windows a running JAR is locked, so stop the server before packaging again. Incident state survives restart.
- One local process is supported. There is no multi-user locking, production deploy, automated merge, rollback or GitHub integration.
- No custom web UI is included. Discord is the demo interface.
- Rehearse from the archived demo starter in a fresh directory with its own artifacts. Keep the completed live project's evidence intact; clearly distinguish `live-evidence/` from `rehearsal-evidence/`.

## Submission

Include this repository, a short recording of the actual Discord/Switch workflow, and the generated before/after logs and resolution report (check these for secrets first). Show how Switch connects the person and agent, the approval pause, and the verified result. See `docs/DEMO-SCRIPT.md`.
