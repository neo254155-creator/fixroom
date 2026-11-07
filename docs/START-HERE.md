# Connect the working Spring Boot project to your Discord room

The project was created at:

```text
C:\Users\Aditya\Documents\Codex\2026-11-07\s\outputs\FixRoom
```

The local API was started at `http://127.0.0.1:8087/api/health`. If it is no longer running, open PowerShell in the project folder and run `./scripts/start.ps1`.

## Add the dedicated agent

In Switch Console, open **Your Agents**, then the plus/add card. Fill in:

| Field | Value |
|---|---|
| Name | fixroom-fixer |
| Description | Reproduces checkout bugs, waits for human approval, then repairs and verifies them. |
| Run location | This computer |
| Directory | C:\Users\Aditya\Documents\Codex\2026-11-07\s\outputs\FixRoom |
| Agent provider | Codex |
| Agent instructions | Read AGENTS.md and docs/AGENT-INSTRUCTIONS.md in your working directory and follow them for every incident. Always post replies through Switch post_message in the incoming thread. |
| Who can talk to it | Me and my agents (keep your Discord account linked) |
| Bypass permissions | Off |

Create it and check that its card appears. The exact form can vary by Switch version; keep the working directory above.

In your Discord fixroom channel:

```text
!invite-agent @fixroom-fixer
```

After Switch confirms it was added, send:

```text
@fixroom-fixer A 10000-cent checkout with a 10% coupon should total 9000 cents, but it totals 9900. Read your project instructions, reproduce the failure, propose a fix, and wait for my approval before changing code.
```

Look in the thread under this message for the agent's progress and proposal. When it asks for approval, use the actual incident ID it returns:

```text
@fixroom-fixer APPROVE FIX-001
```

The agent should then repair the checkout source, rerun all eight checkout tests and post its results. The resolution is saved in `artifacts/FIX-001/resolution.md`.

## If this version of Switch manages agents through switch-buddy

Send the following to switch-buddy in Switch Console instead of guessing at missing UI controls:

```text
Create a Codex agent named fixroom-fixer on this computer, with working directory C:\Users\Aditya\Documents\Codex\2026-11-07\s\outputs\FixRoom. Its description is: Reproduces checkout bugs, waits for human approval, then repairs and verifies them. Its instructions are: Read AGENTS.md and docs/AGENT-INSTRUCTIONS.md in the working directory and follow them. Always post replies through Switch post_message in the incoming thread. Keep permission bypass off and owner-only access. Invite it to my existing Discord fixroom room. Verify it appears in the managed agent list. I authorize creating this agent and adding it to that existing room.
```

This prompt is for the user to send; no Switch agent or Discord messages were created by the project setup itself.
