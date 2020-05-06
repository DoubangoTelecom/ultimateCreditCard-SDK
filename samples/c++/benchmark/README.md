- [GPU acceleration](#gpu-acceleration)
- [Peformance numbers](#peformance-numbers)
- [Pre-built binaries](#prebuilt)
- [Building](#building)
  - [Windows](#building-windows)
  - [Generic GCC](#building-generic-gcc)
  - [Raspberry Pi (Raspbian OS)](#building-rpi)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)


This application is used to check everything is ok and running as fast as expected. 
The information about the maximum frame rate could be checked using this application. 
It's open source and doesn't require registration or license key.

More information about the benchmark rules at [https://www.doubango.org/SDKs/credit-card-ocr/docs/Benchmark.html](https://www.doubango.org/SDKs/credit-card-ocr/docs/Benchmark.html).

<a name="gpu-acceleration"></a>
# GPU acceleration #
By default GPU acceleration is disabled. Check [here](../README.md#gpu-acceleration) for more information on how to enable.

<a name="peformance-numbers"></a>
# Peformance numbers #

Some performance numbers using **720p (1280x720)** images:

|  | 0.0 rate | 0.2 rate | 0.5 rate | 0.7 rate | 1.0 rate |
|-------- | --- | --- | --- | --- | --- |
| **Xeon® E31230v5, GTX 1070 (Ubuntu 18)** | 976 millis <br/> **102.43 fps** | 1602 millis <br/> 62.39 fps | 2567 millis <br/> 38.94 fps | 3214 millis <br/> 31.11 fps | 4142 millis <br/> 24.13 fps |
| **i7-4790K (Windows 7)** | 4459 millis <br/> **22.42 fps** | 8811 millis <br/> 11.34 fps | 15827 millis <br/> 6.31 fps | 20571 millis <br/> 4.86 fps | 27361 millis <br/> 3.65 fps |
| **i7-4770HQ (Windows 10)** | 8653 millis <br/> **11.55 fps** | 16583 millis <br/> 6.03 fps | 29668 millis <br/> 3.37 fps | 38636 millis <br/> 2.58 fps | 52945 millis <br/> 1.88 fps |
| **Galaxy S10+ (Android)** | 3673 millis <br/> **27.22 fps** | 13053 millis <br/> 7.66 fps | 28304 millis <br/> 3.53 fps | 38175 millis <br/> 2.61 fps | 52685 millis <br/> 1.89 fps |
| **Raspberry Pi 4 (Raspbian Buster)** | 9731 millis <br/> **10.21 fps** | 35449 millis <br/> 2.82 fps | 74698 millis <br/> 1.33 fps | 101665 millis <br/> 0.98 fps | 128269 millis <br/> 0.77 fps |

Some notes:
- **Please note that even if Raspberry Pi 4 has a 64-bit CPU [Raspbian OS](https://en.wikipedia.org/wiki/Raspbian>) uses a 32-bit kernel which means we're loosing many SIMD optimizations.**

<a name="prebuilt"></a>
# Pre-built binaries #

If you don't want to build this sample by yourself then, use the pre-built versions:
 - Windows: [benchmark.exe](../../../binaries/windows/x86_64/benchmark.exe) under [binaries/windows/x86_64](../../../binaries/windows/x86_64)
 - Linux: [benchmark](../../../binaries/linux/x86_64/benchmark) under [binaries/linux/x86_64](../../../binaries/linux/x86_64). Built on Ubuntu 18. **You'll need to download libtensorflow.so as explained [here](../README.md#gpu-acceleration-tensorflow-linux)**.
 - Raspberry Pi: [benchmark](../../../binaries/raspbian/armv7l/benchmark) under [binaries/raspbian/armv7l](../../../binaries/raspbian/armv7l)
 - Android: check [android](../../android) folder
 
On **Windows**, the easiest way to try this sample is to navigate to [binaries/windows/x86_64](../../../binaries/windows/x86_64/) and run [binaries/windows/x86_64/benchmark.bat](../../../binaries/windows/x86_64/benchmark.bat). You can edit these files to use your own images and configuration options.

<a name="building"></a>
# Building #

This sample contains [a single C++ source file](benchmark.cxx) and is easy to build. The documentation about the C++ API is at [https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html](https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html).

<a name="building-windows"></a>
## Windows ##
You'll need Visual Studio to build the code. The VS project is at [benchmark.vcxproj](benchmark.vcxproj). Open it.
 1. You will need to change the **"Command Arguments"** like the [below image](../../../VC++_config.jpg). Default value: `--loops 100 --rate 0.2 --positive $(ProjectDir)..\..\..\assets\images\revolut.jpg --negative $(ProjectDir)..\..\..\assets\images\paymentsense.jpg --assets $(ProjectDir)..\..\..\assets`
 2. You will need to change the **"Environment"** variable like the [below image](../../../VC++_config.jpg). Default value: `PATH=$(VCRedistPaths)%PATH%;$(ProjectDir)..\..\..\binaries\windows\x86_64`
 
![VC++ config](../../../VCpp_config.jpg)
 
You're now ready to build and run the sample.

<a name="building-generic-gcc"></a>
## Generic GCC ##
Next command is a generic GCC command:
```
cd ultimateCreditCard-SDK/samples/c++/benchmark

g++ benchmark.cxx -O3 -I../../../c++ -L../../../binaries/<yourOS>/<yourArch> -lultimate_creditcard-sdk -o benchmark
```
- You've to change `yourOS` and  `yourArch` with the correct values. For example, on **Linux x86_64** they would be equal to `linux` and `x86_64` respectively.
- If you're cross compiling then, you'll have to change `g++` with the correct triplet. For example, on **Android ARM64** the triplet would be equal to `aarch64-linux-android-g++`.

<a name="building-rpi"></a>
## Raspberry Pi (Raspbian OS) ##

To build the sample for Raspberry Pi you can either do it on the device itself or cross compile it on [Windows](../README.md#cross-compilation-rpi-install-windows), [Linux](../README.md#cross-compilation-rpi-install-ubuntu) or OSX machines. 
For more information on how to install the toolchain for cross compilation please check [here](../README.md#cross-compilation-rpi).

```
cd ultimateCreditCard-SDK/samples/c++/benchmark

arm-linux-gnueabihf-g++ benchmark.cxx -O3 -I../../../c++ -L../../../binaries/raspbian/armv7l -lultimate_creditcard-sdk -o benchmark
```
- On Windows: replace `arm-linux-gnueabihf-g++` with `arm-linux-gnueabihf-g++.exe`
- If you're building on the device itself: replace `arm-linux-gnueabihf-g++` with `g++` to use the default GCC

<a name="testing"></a>
# Testing #
After [building](#building) the application you can test it on your local machine.

<a name="testing-usage"></a>
## Usage ##

Benchmark is a command line application with the following usage:
```
benchmark \
      --positive <path-to-image-with-ccard> \
      --negative <path-to-image-without-ccard> \
      [--assets <path-to-assets-folder>] \
      [--loops <number-of-times-to-run-the-loop:[1, inf]>] \
      [--rate <positive-rate:[0.0, 1.0]>] \
      [--rectify <whether-to-enable-rectification-layer:true/false>] \
      [--tokenfile <path-to-license-token-file>] \
      [--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--positive` Path to an image (JPEG/PNG/BMP) with a valid credit card. This image will be used to evaluate the recognizer. You can use default image at [../../../assets/images/revolut.jpg](../../../assets/images/revolut.jpg).
- `--negative` Path to an image (JPEG/PNG/BMP) without a valid credit card. This image will be used to evaluate the decoder. You can use default image at [../../../assets/images/paymentsense.jpg](../../../assets/images/paymentsense.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. Default value is the current folder.
- `--loops` Number of times to run the processing pipeline.
- `--rate` Percentage value within [0.0, 1.0] defining the positive rate. The positive rate defines the percentage of images with valid credit cards.
- `--rectify` Whether to enable the rectification layer. More info about the rectification layer at https://www.doubango.org/SDKs/credit-card-ocr/docs/Rectification_layer.html. This layer is always present on x86_64 devices. Default: false.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

The information about the maximum frame rate is obtained using `--rate 0.0` which means evaluating the negative (no valid credit card) image only. The minimum frame rate could be obtained using `--rate 1.0` which means evaluating the positive image only (all images on the video stream have a credit card). In real life, very few frames from a video stream will contain a credit card (--rate < **0.01**).

<a name="testing-examples"></a>
## Examples ##

For example, on **Raspberry Pi** you may call the benchmark application using the following command:
```
LD_LIBRARY_PATH=../../../binaries/raspbian/armv7l:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/revolut.jpg \
    --negative ../../../assets/images/paymentsense.jpg \
    --assets ../../../assets \
    --loops 100 \
    --rate 0.2 \
    --rectify false
```
On **Linux x86_64**, you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/linux/x86_64:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/revolut.jpg \
    --negative ../../../assets/images/paymentsense.jpg \
    --assets ../../../assets \
    --loops 100 \
    --rate 0.2
```
On **Windows x86_64**, you may use the next command:
```
benchmark.exe ^
    --positive ../../../assets/images/revolut.jpg ^
    --negative ../../../assets/images/paymentsense.jpg ^
    --assets ../../../assets ^
    --loops 100 ^
    --rate 0.2
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


