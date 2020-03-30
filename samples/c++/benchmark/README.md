- [Peformance numbers](#peformance-numbers)
- [Building](#building)
  - [Windows](#building-windows)
  - [Generic GCC](#building-generic-gcc)
  - [Raspberry Pi (Raspbian OS)](#building-rpi)
- [Testing](#testing)
  - [Usage](#testing-usage)
  - [Examples](#testing-examples)


This application is used to check everything is ok and running as fast as expected. 
The information about the maximum frame rate (**27fps** on Snapdragon 855 devices and **10fps** on Raspberry Pi 4) could be checked using this application. 
It's open source and doesn't require registration or license key.

More information about the benchmark rules at [https://www.doubango.org/SDKs/credit-card-ocr/docs/Benchmark.html](https://www.doubango.org/SDKs/credit-card-ocr/docs/Benchmark.html).

<a name="peformance-numbers"></a>
# Peformance numbers #

Some performance numbers on high-end (**Galaxy S10+**) and low-end (**Raspberry Pi 4**) devices using **720p (1280x720)** images:

|  | 0.0 rate | 0.2 rate | 0.5 rate | 0.7 rate | 1.0 rate |
|-------- | --- | --- | --- | --- | --- |
| **Galaxy S10+ (Android)** | 3673 millis <br/> **27.22 fps** | 13053 millis <br/> 7.66 fps | 28304 millis <br/> 3.53 fps | 38175 millis <br/> 2.61 fps | 52685 millis <br/> 1.89 fps |
| **Raspberry Pi 4 (Raspbian Buster)** | 9731 millis <br />**10.21 fps** | 35449 millis <br/> 2.82 fps | 74698 millis <br/> 1.33 fps | 101665 millis <br/> 0.98 fps | 128269 millis <br/> 0.77 fps |

Some notes:
- **Please note that even if Raspberry Pi 4 has a 64-bit CPU [Raspbian OS](https://en.wikipedia.org/wiki/Raspbian>) uses a 32-bit kernel which means we're loosing many SIMD optimizations.**

<a name="building"></a>
# Building #

This sample contains [a single C++ source file](benchmark.cxx) and is easy to build. The documentation about the C++ API is at [https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html](https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html).

<a name="building-windows"></a>
## Windows ##
You'll need Visual Studio and the project is at [benchmark.vcxproj](benchmark.vcxproj).

<a name="building-generic-gcc"></a>
## Generic GCC ##
Next command is a generic GCC command:
```
cd ultimateCreditCard-SDK/samples/c++/benchmark

g++ benchmark.cxx -O3 -I../../../c++ -L../../../binaries/<yourOS>/<yourArch> -lultimate_creditcard-sdk -o benchmark
```
- You've to change `yourOS` and  `yourArch` with the correct values. For example, on Android ARM64 they would be equal to `android` and `jniLibs/arm64-v8a` respectively.
- If you're cross compiling then, you'll have to change `g++` with the correct triplet. For example, on Android ARM64 the triplet would be equal to `aarch64-linux-android-g++`.

<a name="building-rpi"></a>
## Raspberry Pi (Raspbian OS) ##

To build the sample for Raspberry Pi you can either do it on the device itself or cross compile it on [Windows](#cross-compilation-rpi-install-windows), [Linux](#cross-compilation-rpi-install-ubunt) or OSX machines. 
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
      --positive <path-to-image-with-a-card> \
      --negative <path-to-image-without-a-card> \
      [--assets <path-to-assets-folder>] \
      [--loops <number-of-times-to-run-the-loop:[1, inf]>] \
      [--rate <positive-rate:[0.0, 1.0]>] \
      [--rectify <whether-to-enable-rectification-layer:true/false>] \
      [--tokenfile <path-to-license-token-file>] \
      [--tokendata <base64-license-token-data>]
```
Options surrounded with **[]** are optional.
- `--positive` Path to an image (JPEG/PNG/BMP) with a creit card. This image will be used to evaluate the recognizer. You can use default image at [../../../assets/images/revolut.jpg](../../../assets/images/revolut.jpg).
- `--negative` Path to an image (JPEG/PNG/BMP) without a credit card. This image will be used to evaluate the decoder. You can use default image at [../../../assets/images/paymentsense.jpg](../../../assets/images/paymentsense.jpg).
- `--assets` Path to the [assets](../../../assets) folder containing the configuration files and models. Default value is the current folder.
- `--loops` Number of times to run the processing pipeline.
- `--rate` Percentage value within [0.0, 1.0] defining the positive rate. The positive rate defines the percentage of images with a plate.
- `--rectify` Whether to enable the rectification layer. More info about the rectification layer at [https://www.doubango.org/SDKs/credit-card-ocr/docs/Rectification_layer.html](https://www.doubango.org/SDKs/credit-card-ocr/docs/Rectification_layer.html). Default: *false*.
- `--tokenfile` Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.
- `--tokendata` Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: *null*.

The information about the maximum frame rate (**27fps** on Snapdragon 855 devices and **10fps** on Raspberry Pi 4) is obtained using `--rate 0.0` which means evaluating the negative (no credit card) image only. The minimum frame rate could be obtained using `--rate 1.0` which means evaluating the positive image only (all images on the video stream have a credit card). In real life, very few frames from a video stream will contain a credit card (`--rate` **< 0.01**).

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
On Android ARM64 you may use the next command:
```
LD_LIBRARY_PATH=../../../binaries/android/jniLibs/arm64-v8a:$LD_LIBRARY_PATH ./benchmark \
    --positive ../../../assets/images/revolut.jpg \
    --negative ../../../assets/images/paymentsense.jpg \
    --assets ../../../assets \
    --loops 100 \
    --rate 0.2 \
    --rectify false
```

Please note that if you're cross compiling the application then you've to make sure to copy the application and both the [assets](../../../assets) and [binaries](../../../binaries) folders to the target device.


