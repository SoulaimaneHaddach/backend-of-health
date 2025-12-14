<#
Simple smoke test: GET /api/admin/doctors/{id}
Usage:
  .\check_admin_get_doctor.ps1 -ApiBase "http://localhost:8080/api" -AdminToken "<token>" -DoctorId "<uuid>"

The script issues a GET with Authorization header and prints JSON response.
#>
param(
    [string]$ApiBase = "http://localhost:8080/api",
    [Parameter(Mandatory=$true)][string]$AdminToken,
    [Parameter(Mandatory=$true)][string]$DoctorId
)

$headers = @{ Authorization = "Bearer $AdminToken" }
$url = "${ApiBase.TrimEnd('/')}/admin/doctors/$DoctorId"
Write-Host "GET $url"
try {
    $resp = Invoke-RestMethod -Uri $url -Headers $headers -Method GET -ErrorAction Stop
    Write-Host "Response JSON:`n"
    $resp | ConvertTo-Json -Depth 6
    exit 0
} catch {
    Write-Error "Request failed: $($_.Exception.Message)"
    if ($_.Exception.Response -ne $null) {
        try {
            $sr = [System.IO.StreamReader]$_.Exception.Response.GetResponseStream()
            Write-Error $sr.ReadToEnd()
        } catch { }
    }
    exit 2
}
