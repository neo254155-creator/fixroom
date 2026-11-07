# Direct API commands for the Switch agent

Enter these built-in PowerShell commands directly in your shell tool. They do not execute a `.ps1` file and require no execution-policy change. Use these instead of the optional `scripts/incident.ps1` helper on machines where script files are blocked.

Always use the actual incident ID returned by the API. FIX-001 is only an example. Every command is independent; it does not rely on variables from a previous shell call.

## Health and incident list

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8087/api/health' -TimeoutSec 10 | ConvertTo-Json
Invoke-RestMethod -Uri 'http://127.0.0.1:8087/api/incidents' -TimeoutSec 10 | ConvertTo-Json -Depth 12
```

## Open a new incident

Replace NEOPAT with the actual person reporting the issue if different. Reuse an existing matching incident before opening another one.

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8087/api/incidents' -ContentType 'application/json' -Body '{"title":"10 percent coupon gives wrong checkout total","reporter":"NEOPAT"}' -TimeoutSec 10 | ConvertTo-Json -Depth 12
```

## Reproduce

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8087/api/incidents/FIX-001/reproduce' -TimeoutSec 240 | ConvertTo-Json -Depth 12
```

The server runs Maven, records the failing tests and returns structured evidence. Then inspect the checkout source without modifying it.

## Propose and wait

Replace the summary with the cause and exact correction you actually diagnosed.

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8087/api/incidents/FIX-001/propose' -ContentType 'application/json' -Body '{"summary":"<diagnosed cause and exact proposed correction>"}' -TimeoutSec 10 | ConvertTo-Json -Depth 12
```

Post the evidence and proposal in the incoming Switch thread, ask the owner for `APPROVE FIX-001`, and end your turn. Never call approve before a separate explicit owner approval message.

## Record explicit owner approval

Only after receiving the exact incident-specific approval in the same thread, substitute the actual approving person's name:

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8087/api/incidents/FIX-001/approve' -ContentType 'application/json' -Body '{"approvedBy":"NEOPAT","confirmation":"APPROVE FIX-001"}' -TimeoutSec 10 | ConvertTo-Json -Depth 12
```

Now make only the approved change to CheckoutService.java.

## Verify and read the report

```powershell
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8087/api/incidents/FIX-001/verify' -TimeoutSec 240 | ConvertTo-Json -Depth 12
Invoke-RestMethod -Uri 'http://127.0.0.1:8087/api/incidents/FIX-001/report' -TimeoutSec 10
```

Post actual results to the original Switch thread. Do not change tests or fabricate evidence. If API requests themselves are denied by the session's network rules, ask for the specific local access required; do not disable the sandbox.
