#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <string>
#pragma comment(lib,"user32.lib")

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PSTR szCmdParam, int iCmdShow)
{
    int return_code = 0;
    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));

    LPTSTR command = _tcsdup(TEXT(".\\runtime\\bin\\javaw.exe -cp .\\app\\EndPoint.jar ovh.alexisdelhaie.endpoint.Application"));
    if (!CreateProcess(NULL, command, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi))
    {
        int msgboxID = MessageBox(
            NULL,
            TEXT("Unable to start the JVM. Try reinstalling the program to fix this problem."),
            TEXT("EndPoint bootstrap"),
            MB_ICONHAND | MB_OK | MB_DEFBUTTON1
        );
        return_code = -1;
    }
    CloseHandle(&pi.hProcess);
    CloseHandle(&pi.hThread);
    return return_code;
}