setlocal
set PYTHONPATH=../../../binaries/windows/x86_64;../../../python
python recognizer.py --image ../../../assets/images/revolut.jpg --assets ../../../assets --ielcd True
endlocal