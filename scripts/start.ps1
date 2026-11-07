$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\java.exe")) {
    $taskJavaInfo = & cmd.exe /d /c 'java -XshowSettings:properties -version 2>&1' | Out-String
    if ($taskJavaInfo -match 'java.home\s*=\s*([^\r\n]+)') { $env:JAVA_HOME = $Matches[1].Trim() }
}
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\java.exe")) { throw 'A working JDK 21 or later is required' }
Push-Location -LiteralPath $taskRoot
try {
    # The submitted source includes the approved repair, so run the full test suite.
    & .\mvnw.cmd -B package
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    & "$env:JAVA_HOME\bin\java.exe" -jar target/fixroom-0.0.1-SNAPSHOT.jar
} finally { Pop-Location }
