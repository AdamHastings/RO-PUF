::Kill PuTTY and XMD to remove cluttered windows
taskkill /IM putty.exe /f
taskkill /IM xmd.exe /f

::Find the newest instance of the XMD log
pushd E:\Adam\Characterizing\Thesis\logs\XMD
for /f "tokens=*" %%a in ('dir /b /od') do set xmdlog=%%~dpnxa
popd

::Find the newest instance of the PuTTY log
pushd E:\Adam\Characterizing\Thesis\logs\PuTTY
for /f "tokens=*" %%a in ('dir /b /od') do set puttylog=%%~dpnxa
popd

::Email the results with attached logs!
blat -to ihavenoface@gmail.com -subject "Lab results" -body "This is some text" -attach %xmdlog% -attach %puttylog%
