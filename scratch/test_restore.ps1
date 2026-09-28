$conn = New-Object System.Data.SqlClient.SqlConnection("Server=.;Database=master;Integrated Security=True")
try {
    $conn.Open()
    
    # Try to create database if not exists
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLyDLpro') CREATE DATABASE QuanLyDLpro"
    $cmd.ExecuteNonQuery()
    Write-Output "Database QuanLyDLpro created/verified."
    
    # Try to restore differential backup with REPLACE
    $cmd.CommandText = "RESTORE DATABASE QuanLyDLpro FROM DISK='d:\quanly\QuanLyDaiLy.bak' WITH REPLACE"
    $cmd.ExecuteNonQuery()
    Write-Output "Restore succeeded!"
} catch {
    Write-Error $_.Exception.Message
} finally {
    $conn.Close()
}
