@echo off
echo ====================================================
echo  Generando stubs gRPC (Python) desde src/main/proto
echo ====================================================

if exist "venv\Scripts\python.exe" (
    set "PY_BIN=venv\Scripts\python.exe"
) else (
    set "PY_BIN=python"
)

%PY_BIN% -m grpc_tools.protoc -I..\src\main\proto --python_out=.\grpc_clients --grpc_python_out=.\grpc_clients ..\src\main\proto\*.proto

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Stubs generados correctamente en grpc_clients\
) else (
    echo.
    echo [ERROR] Hubo un problema al compilar los .proto.
)
echo ====================================================
pause