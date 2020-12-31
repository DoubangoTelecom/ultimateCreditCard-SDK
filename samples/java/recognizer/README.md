- [Pre-built binaries](#prebuilt)
- [Building](#building)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)


This application is a reference implementation for developers to show how to use the Java API and could
be used to easily check the accuracy. The Java API is a wrapper around the C++ API defined at [https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html](https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html).

The application accepts path to a JPEG/PNG/BMP file as input. This **is not the recommended** way to use the API. We recommend reading the data directly from the camera and feeding the SDK with the uncompressed **YUV data** without saving it to a file or converting it to RGB.

If you don't want to build this sample and is looking for a quick way to check the accuracy then, try
our cloud-based solution at [https://www.doubango.org/webapps/credit-card-ocr/](https://www.doubango.org/webapps/credit-card-ocr/).

This sample is open source and doesn't require registration or license key.

<a name="prebuilt"></a>
# Pre-built binaries #

If you don't want to build this sample by yourself then, use the pre-built C++ versions:
 - Windows: [recognizer.exe](../../../binaries/windows/x86_64/recognizer.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64)
 - Linux: [recognizer](../../../binaries/linux/x86_64/recognizer) under [binaries/linux/x86_64](../../../binaries/linux/x86_64). Built on Ubuntu 18. **You'll need to download libtensorflow.so as explained [here](../../c++/README.md#gpu-acceleration-tensorflow-linux)**
 - Raspberry Pi: [recognizer](../../../binaries/raspbian/armv7l/recognizer) under [binaries/raspbian/armv7l](../../../binaries/raspbian/armv7l)
 - Android: check [android](../../android) folder
 - iOS: check [ios](../../ios) folder
 
On **Windows**, the easiest way to try this sample is to navigate to [binaries/windows/x86_64](../../../binaries/windows/x86_64/) and run [binaries/windows/x86_64/recognizer.bat](../../../binaries/windows/x86_64/recognizer.bat). You can edit these files to use your own images and configuration options.

<a name="building"></a>
# Building #

This sample contains [a single Java source file](Recognizer.java).

You have to navigate to the current folder (`ultimateCreditCard-SDK/samples/java/recognizer` ) before trying the next commands:
```
cd ultimateCreditCard-SDK/samples/java/recognizer
```

Here is how to build the file using `javac`:
```
javac @sources.txt -d .
```

<a name="testing-usage"></a>
## Usage ##

`Recognizer` is a command line application with the following usage:
```
Recognizer \
      --image <path-to-image-with-credit-card-to-process> \
      [--assets <path-to-assets-folder>] \
      [--tokenfile <path-to-license-token-file>] \
      [--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--image` Path to the image(JPEG/PNG/BMP) to process. You can use default image at [../../../assets/images/revolut.jpg](../../../assets/images/revolut.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. Default value is the current folder.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

<a name="testing-examples"></a>
## Examples ##
You'll need to build the sample as explained [above](#building).

You have to navigate to the current folder (`ultimateCreditCard-SDK/samples/java/recognizer` ) before trying the next commands:
```
cd ultimateCreditCard-SDK/samples/java/recognizer
```

- For example, on **Raspberry Pi** you may call the recognizer application using the following command:
```
LD_LIBRARY_PATH=../../../binaries/raspbian/armv7l:$LD_LIBRARY_PATH \
java Recognizer --image ../../../assets/images/revolut.jpg --assets ../../../assets
```
- On **Linux x86_64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH \
java Recognizer --image ../../../assets/images/revolut.jpg --assets ../../../assets
```
Before trying to run the program **you'll need to download libtensorflow.so as explained [here](../../c++/README.md#gpu-acceleration-tensorflow-linux)**

- On **Windows x86_64**, you may use the next command:
```
setlocal
set PATH=%PATH%;../../../binaries/windows/x86_64
java Recognizer --image ../../../assets/images/revolut.jpg --assets ../../../assets
endlocal
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


