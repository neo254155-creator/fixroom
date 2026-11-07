param(
    [Parameter(Mandatory)][ValidateSet('health','open','reproduce','propose','approve','verify','status','report')][string]$Action,
    [string]$Id,
    [string]$Title,
    [string]$By,
    [string]$Summary,
    [string]$Confirmation,
    [string]$BaseUrl = 'http://127.0.0.1:8087'
)
$ErrorActionPreference = 'Stop'
$taskUrl = "$BaseUrl/api/incidents"
$taskMethod = 'Get'
$taskBody = $null
if ($Action -notin @('health','open','status') -and $Id -notmatch '^FIX-\d{3,}$') { throw 'Provide an incident Id such as FIX-001' }
switch ($Action) {
    'health' { $taskUrl = "$BaseUrl/api/health" }
    'open' {
        if (-not $Title -or -not $By) { throw 'Provide -Title and -By' }
        $taskMethod = 'Post'; $taskBody = @{title=$Title; reporter=$By}
    }
    'status' { if ($Id) { $taskUrl += "/$Id" } }
    'reproduce' { $taskUrl += "/$Id/reproduce"; $taskMethod = 'Post' }
    'propose' {
        if (-not $Summary) { throw 'Provide -Summary' }
        $taskUrl += "/$Id/propose"; $taskMethod = 'Post'; $taskBody = @{summary=$Summary}
    }
    'approve' {
        if (-not $By -or $Confirmation -cne "APPROVE $Id") { throw "Provide -By and -Confirmation 'APPROVE $Id' only after the owner approves" }
        $taskUrl += "/$Id/approve"; $taskMethod = 'Post'; $taskBody = @{approvedBy=$By; confirmation=$Confirmation}
    }
    'verify' { $taskUrl += "/$Id/verify"; $taskMethod = 'Post' }
    'report' { $taskUrl += "/$Id/report" }
}
$taskRequest = @{Uri=$taskUrl; Method=$taskMethod; TimeoutSec=240}
if ($null -ne $taskBody) { $taskRequest.Body = $taskBody | ConvertTo-Json; $taskRequest.ContentType = 'application/json' }
$taskResult = Invoke-RestMethod @taskRequest
if ($taskResult -is [string]) { $taskResult } else { $taskResult | ConvertTo-Json -Depth 12 }
