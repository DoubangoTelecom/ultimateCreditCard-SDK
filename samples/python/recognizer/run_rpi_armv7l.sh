PYTHONPATH=../../../binaries/raspbian/armv7l:../../../python \
LD_LIBRARY_PATH=../../../binaries/raspbian/armv7l:$LD_LIBRARY_PATH \
python3 recognizer.py --image ../../../assets/images/revolut.jpg --assets ../../../assets --ielcd False 