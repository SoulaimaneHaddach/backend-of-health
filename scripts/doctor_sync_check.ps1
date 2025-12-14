<#
Doctor Sync Verification Script
Usage:
  .\doctor_sync_check.ps1 -ApiBase 'https://api.example.com' -AdminToken '<ADMIN_JWT>'

This script performs the following checks against the backend API:
 1. Create doctor A and B via admin API
 2. Update doctor A via admin API
 3. Approve doctor A and reject doctor B
 4. Verify admin list contains entries and statuses
 5. Verify public/mobile endpoints reflect approval/rejection
 6. Delete doctor A and verify removal
 7. Smoke-check all admin endpoints used by the admin UI

Note: DB checks are not performed by default. You may add DB verification commands specific to your database engine.
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$ApiBase,

    [Parameter(Mandatory=$true)]
    [string]$AdminToken,

    [switch]$Verbose,
    [switch]$UseDb,
    [string]$DbHost = 'localhost',
    [int]$DbPort = 3306,
    [string]$DbUser = 'root',
    [string]$DbPassword = '',
    [string]$DbName = 'healing'
)

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )
    $uri = "$ApiBase$Path"
    $defaultHeaders = @{ "Authorization" = "Bearer $AdminToken"; "Content-Type" = "application/json" }
    $allHeaders = $defaultHeaders + $Headers
    try {
        if ($Body -ne $null) {
            $json = $Body | ConvertTo-Json -Depth 10
            $resp = Invoke-RestMethod -Method $Method -Uri $uri -Headers $allHeaders -Body $json -ErrorAction Stop
            return @{ ok = $true; status = 200; body = $resp }
        } else {
            $resp = Invoke-RestMethod -Method $Method -Uri $uri -Headers $allHeaders -ErrorAction Stop
            return @{ ok = $true; status = 200; body = $resp }
        }
    } catch {
        # Try to capture status code if available
        $err = $_.Exception.Response
        if ($err -ne $null) {
            try { $stat = $err.StatusCode.value__ } catch { $stat = $null }
            $text = ''
            try { $text = (New-Object System.IO.StreamReader($err.GetResponseStream())).ReadToEnd() } catch {}
            return @{ ok = $false; status = $stat; error = $text }
        }
        return @{ ok = $false; status = $null; error = $_.Exception.Message }
    }
}

function Assert-ContainsId($list, $id) {
    foreach ($item in $list) {
        if ($null -ne $item.id -and $item.id -eq $id) { return $true }
        if ($null -ne $item.id -and ($item.id -as [string]) -eq $id) { return $true }
        if ($null -ne $item.id -and ($item.id -as [string]) -eq $id.ToString()) { return $true }
    }
    return $false
}

function Invoke-MySqlQuery {
    param(
        [string]$Query
    )
    $mysqlExe = 'mysql'
    if (-not (Get-Command $mysqlExe -ErrorAction SilentlyContinue)) {
        return @{ ok = $false; error = "mysql CLI not found in PATH" }
    }

    $args = @('-h', $DbHost, '-P', $DbPort.ToString(), '-u', $DbUser, "-p$DbPassword", '-D', $DbName, '-N', '-e', $Query)
    try {
        $out = & $mysqlExe @args 2>&1
        if ($LASTEXITCODE -ne 0) {
            return @{ ok = $false; error = $out }
        }
        return @{ ok = $true; out = $out }
    } catch {
        return @{ ok = $false; error = $_.Exception.Message }
    }
}

Write-Host "[INFO] Starting Doctor Sync Verification against $ApiBase"

# 1) Create Doctor A
$docA = @{ name = "Dr Sync A"; email = "dr.sync.a.$([guid]::NewGuid().ToString().Substring(0,8))@example.com"; specialty = "Cardiology" }
Write-Host "[STEP] Create Doctor A: $($docA.email)"
$createA = Invoke-Api -Method Post -Path '/api/admin/doctors' -Body $docA
if (-not $createA.ok) { Write-Error "Create A failed: status=$($createA.status) error=$($createA.error)"; exit 1 }
$createdA = $createA.body
if ($null -eq $createdA.id) { Write-Warning "Created A did not return id in body; trying to inspect body"; $createdA | Format-List }
$idA = $createdA.id
Write-Host "[OK] Created A id=$idA"

if ($UseDb) {
    Write-Host "[DB] Verifying Doctor A exists in DB by email"
    $q = "SELECT id FROM doctors WHERE email = '$($docA.email)' LIMIT 1;"
    $r = Invoke-MySqlQuery $q
    if (-not $r.ok) { Write-Warning "DB check failed: $($r.error)" } else {
        $row = $r.out.Trim()
        if ($row -ne '') {
            $dbId = ($row -split "`t")[0]
            if ($dbId -and ($dbId -eq $idA -or $idA -like "*$dbId*" -or $dbId -like "*$idA*")) {
                Write-Host "[DB OK] Doctor A found with id=$dbId"
            } else {
                Write-Warning "[DB MISMATCH] Doctor A row found but id mismatch DB=$dbId API=$idA"
            }
        } else {
            Write-Warning "[DB MISSING] Doctor A not found in DB by email"
        }
    }
}
# 2) Create Doctor B
$docB = @{ name = "Dr Sync B"; email = "dr.sync.b.$([guid]::NewGuid().ToString().Substring(0,8))@example.com"; specialty = "Dermatology" }
Write-Host "[STEP] Create Doctor B: $($docB.email)"
$createB = Invoke-Api -Method Post -Path '/api/admin/doctors' -Body $docB
if (-not $createB.ok) { Write-Error "Create B failed: status=$($createB.status) error=$($createB.error)"; exit 1 }
$createdB = $createB.body
$idB = $createdB.id
Write-Host "[OK] Created B id=$idB"

if ($UseDb) {
    Write-Host "[DB] Verifying Doctor B exists in DB by email"
    $q = "SELECT id FROM doctors WHERE email = '$($docB.email)' LIMIT 1;"
    $r = Invoke-MySqlQuery $q
    if (-not $r.ok) { Write-Warning "DB check failed: $($r.error)" } else {
        $row = $r.out.Trim()
        if ($row -ne '') { Write-Host "[DB OK] Doctor B found" } else { Write-Warning "[DB MISSING] Doctor B not found in DB by email" }
    }
}
# 3) Update Doctor A
Write-Host "[STEP] Update Doctor A (PATCH)"
$updateA = @{ name = "Dr Sync A Updated"; phone = "+123456700" }
$patchA = Invoke-Api -Method Patch -Path "/api/admin/doctors/$idA" -Body $updateA
if (-not $patchA.ok) { Write-Error "Patch A failed: status=$($patchA.status) error=$($patchA.error)"; exit 1 }
Write-Host "[OK] Patched A"

if ($UseDb) {
    Write-Host "[DB] Verify Doctor A phone updated"
    $q = "SELECT phone_number, phoneNumber FROM doctors WHERE email = '$($docA.email)' LIMIT 1;"
    $r = Invoke-MySqlQuery $q
    if (-not $r.ok) { Write-Warning "DB check failed: $($r.error)" } else {
        $row = $r.out.Trim()
        if ($row -ne '') {
            $cols = $row -split "`t"
            $phone = $cols[0]
            if (-not $phone) { $phone = $cols[1] }
            if ($phone -and $phone -eq $updateA.phone) { Write-Host "[DB OK] Phone matches updated value" } else { Write-Warning "[DB MISMATCH] Phone in DB='$phone' expected='$($updateA.phone)'" }
        } else { Write-Warning "[DB MISSING] Doctor A not found for phone verification" }
    }
}
# 4) Approve Doctor A
Write-Host "[STEP] Approve Doctor A"
$approveA = Invoke-Api -Method Post -Path "/api/admin/doctors/$idA/approve"
if (-not $approveA.ok) { Write-Error "Approve A failed: status=$($approveA.status) error=$($approveA.error)"; exit 1 }
Write-Host "[OK] Approved A"

if ($UseDb) {
    Write-Host "[DB] Verify Doctor A status = active"
    $q = "SELECT status FROM doctors WHERE email = '$($docA.email)' LIMIT 1;"
    $r = Invoke-MySqlQuery $q
    if (-not $r.ok) { Write-Warning "DB check failed: $($r.error)" } else {
        $row = $r.out.Trim()
        if ($row -ne '') {
            $status = $row -split "`t" | Select-Object -First 1
            if ($status -eq 'active') { Write-Host "[DB OK] status=active" } else { Write-Warning "[DB MISMATCH] status='$status' (expected 'active')" }
        } else { Write-Warning "[DB MISSING] Doctor A not found for status verification" }
    }
}
# 5) Reject Doctor B
Write-Host "[STEP] Reject Doctor B"
$rejectB = Invoke-Api -Method Post -Path "/api/admin/doctors/$idB/reject"
if (-not $rejectB.ok) { Write-Error "Reject B failed: status=$($rejectB.status) error=$($rejectB.error)"; exit 1 }
Write-Host "[OK] Rejected B"

if ($UseDb) {
    Write-Host "[DB] Verify Doctor B status = rejected"
    $q = "SELECT status FROM doctors WHERE email = '$($docB.email)' LIMIT 1;"
    $r = Invoke-MySqlQuery $q
    if (-not $r.ok) { Write-Warning "DB check failed: $($r.error)" } else {
        $row = $r.out.Trim()
        if ($row -ne '') {
            $status = $row -split "`t" | Select-Object -First 1
            if ($status -eq 'rejected') { Write-Host "[DB OK] status=rejected" } else { Write-Warning "[DB MISMATCH] status='$status' (expected 'rejected')" }
        } else { Write-Warning "[DB MISSING] Doctor B not found for status verification" }
    }
}
# 6) Admin list consistency
Write-Host "[STEP] Get admin doctors list"
$listAdmin = Invoke-Api -Method Get -Path '/api/admin/doctors'
if (-not $listAdmin.ok) { Write-Error "Admin list failed: status=$($listAdmin.status) error=$($listAdmin.error)"; exit 1 }
$adminList = $listAdmin.body
$hasA = Assert-ContainsId $adminList $idA
$hasB = Assert-ContainsId $adminList $idB
if ($hasA -and $hasB) { Write-Host "[OK] Admin list contains both doctors" } else { Write-Warning "Admin list missing doctors: A=$hasA B=$hasB" }

# 7) Mobile/public list and detail checks
Write-Host "[STEP] Get public doctors list"
$listPublic = Invoke-Api -Method Get -Path '/api/doctors'
if (-not $listPublic.ok) { Write-Error "Public list failed: status=$($listPublic.status) error=$($listPublic.error)"; exit 1 }
$publicList = $listPublic.body
# Check A should be visible (approved)
$foundAInPublic = $false
foreach ($item in $publicList) { if ($null -ne $item.id -and ($item.id -eq $idA -or ($item.id -as [string]) -eq $idA)) { $foundAInPublic = $true } }
if ($foundAInPublic) { Write-Host "[OK] Doctor A visible in public list" } else { Write-Warning "Doctor A not visible in public list (expected, if public hides non-active)" }
# Check B should NOT be visible (rejected)
$foundBInPublic = $false
foreach ($item in $publicList) { if ($null -ne $item.id -and ($item.id -eq $idB -or ($item.id -as [string]) -eq $idB)) { $foundBInPublic = $true } }
if (-not $foundBInPublic) { Write-Host "[OK] Doctor B not visible in public list as expected" } else { Write-Warning "Doctor B is visible in public list (unexpected)" }

# 8) Public detail for A
Write-Host "[STEP] Get public doctor A detail"
$detailA = Invoke-Api -Method Get -Path "/api/doctors/$idA"
if ($detailA.ok) { Write-Host "[OK] Public doctor A returned" } else { Write-Warning "Public doctor A returned status=$($detailA.status) error=$($detailA.error)" }

# 9) Delete Doctor A
Write-Host "[STEP] Delete Doctor A"
$delA = Invoke-Api -Method Delete -Path "/api/admin/doctors/$idA"
if (-not $delA.ok) { Write-Error "Delete A failed: status=$($delA.status) error=$($delA.error)"; exit 1 }
Write-Host "[OK] Deleted A"

if ($UseDb) {
    Write-Host "[DB] Verify Doctor A removed from DB"
    $q = "SELECT COUNT(*) FROM doctors WHERE email = '$($docA.email)';"
    $r = Invoke-MySqlQuery $q
    if (-not $r.ok) { Write-Warning "DB check failed: $($r.error)" } else {
        $count = $r.out.Trim()
        if ($count -eq '0') { Write-Host "[DB OK] Doctor A not present in DB" } else { Write-Warning "[DB MISMATCH] Doctor A still present in DB (count=$count)" }
    }
}
# Verify deletion from public
$detailA2 = Invoke-Api -Method Get -Path "/api/doctors/$idA"
if (-not $detailA2.ok -and ($detailA2.status -eq 404 -or $detailA2.status -eq 400 -or $detailA2.status -eq $null)) {
    Write-Host "[OK] Public detail for A returns not found after delete (status=$($detailA2.status))"
} else {
    Write-Warning "Public detail for A still exists (status=$($detailA2.status)). Response: $($detailA2.body)"
}

# 10) Endpoint smoke checks used by frontend
Write-Host "[STEP] Smoke-check admin endpoints used by frontend"
$endpoints = @(
    @{ method='Post'; path='/api/admin/auth/login'; expect='201/200/200' },
    @{ method='Get'; path='/api/admin/me'; expect='200' },
    @{ method='Get'; path='/api/admin/doctors'; expect='200' },
    @{ method='Post'; path='/api/admin/doctors'; expect='201' },
    @{ method='Patch'; path='/api/admin/doctors/$idB'; expect='200' },
    @{ method='Post'; path='/api/admin/doctors/$idB/approve'; expect='200' },
    @{ method='Post'; path='/api/admin/doctors/$idB/reject'; expect='200' },
    @{ method='Get'; path='/api/admin/bookings'; expect='200' },
    @{ method='Get'; path='/api/admin/users'; expect='200' },
    @{ method='Get'; path='/api/admin/specialties'; expect='200' },
    @{ method='Get'; path='/api/admin/reports/bookings'; expect='200' },
    @{ method='Get'; path='/api/admin/reports/doctors'; expect='200' }
)

foreach ($ep in $endpoints) {
    try {
        $m = $ep.method
        $p = $ep.path
        Write-Host "Checking $m $p ..."
        $res = Invoke-Api -Method $m -Path $p
        if ($res.ok) { Write-Host "  [OK] returned status 200" } else { Write-Warning "  [WARN] returned status $($res.status) error=$($res.error)" }
    } catch { Write-Warning "  [ERROR] Exception when checking $($ep.path): $_" }
}

Write-Host "[INFO] Doctor Sync Verification script completed. Review warnings above for issues."

# Optional: Print manual SQL checks to run against your DB
Write-Host "\nManual DB verification examples (run in your DB client):"
Write-Host "  SELECT id, name, email, specialization, status FROM doctors WHERE email LIKE 'dr.sync.a.%';"
Write-Host "  SELECT id, name, email, specialization, status FROM doctors WHERE email LIKE 'dr.sync.b.%';"

Write-Host "Done."
