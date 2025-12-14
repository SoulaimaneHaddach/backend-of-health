<#
Login as admin and GET /api/admin/doctors to verify admin auth and list presence.
Usage:
  .\check_admin_list_doctors.ps1 -ApiBase "http://localhost:8080/api" -Email "admin@hospital.com" -Password "admin123"
#>
param(
    [string]$ApiBase = "http://localhost:8080/api",
    [string]$Email = "admin@hospital.com",
    [string]$Password = "admin123"
)

$loginUrl = "${ApiBase.TrimEnd('/')}/admin/auth/login"
$body = @{ email = $Email; password = $Password } | ConvertTo-Json
Write-Host "POST $loginUrl (login)"
try {
    $login = Invoke-RestMethod -Uri $loginUrl -Method POST -Body $body -ContentType 'application/json' -ErrorAction Stop
} catch {
    Write-Error "Login failed: $($_.Exception.Message)"
    if ($_.Exception.Response -ne $null) {
        try { $sr = [System.IO.StreamReader]$_.Exception.Response.GetResponseStream(); Write-Error $sr.ReadToEnd() } catch {}
    }
    exit 2
}

if (-not $login.token) { Write-Error "Login succeeded but token missing in response"; exit 3 }
$token = $login.token
Write-Host "Got token. Now requesting admin doctors list..."

$headers = @{ Authorization = "Bearer $token" }
$listUrl = "${ApiBase.TrimEnd('/')}/admin/doctors"
try {
    $resp = Invoke-RestMethod -Uri $listUrl -Headers $headers -Method GET -ErrorAction Stop
    Write-Host "Doctors response:`n"
    $resp | ConvertTo-Json -Depth 6
    $count = ($resp | Measure-Object).Count
    Write-Host "Doctor items count: $count"
    exit 0
} catch {
    Write-Error "Request failed: $($_.Exception.Message)"
    if ($_.Exception.Response -ne $null) {
        try { $sr = [System.IO.StreamReader]$_.Exception.Response.GetResponseStream(); Write-Error $sr.ReadToEnd() } catch {}
    }
    exit 4
}
