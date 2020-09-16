#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <string>
#pragma comment(lib,"user32.lib")

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PSTR szCmdParam, int iCmdShow)
{
    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));

    LPTSTR command = _tcsdup(TEXT(".\\runtime\\bin\\javaw.exe --module-path \".\\lib\" --add-modules javafx.controls,javafx.fxml -jar .\\app\\EndPoint.jar"));
    if (!CreateProcess(NULL, command, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi))
    {
        CloseHandle(&pi.hProcess);
        CloseHandle(&pi.hThread);
        int msgboxID = MessageBox(
            NULL,
            TEXT("Unable to start the JVM. Try reinstalling the program to fix this problem."),
            TEXT("EndPoint bootstrap"),
            MB_ICONHAND | MB_OK | MB_DEFBUTTON1
        );
        return -1;
    }
    CloseHandle(&pi.hProcess);
    CloseHandle(&pi.hThread);
    return 0;
}