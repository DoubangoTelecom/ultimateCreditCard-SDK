  - [Getting started](#getting-started)
  - [Android](#android)
  	- [Sample applications](#sample-applications-android)
		- [Benchmark](#sample-application-benchmark-android)
		- [CCardVideoRecognizer](#sample-application-videorecognizer-android)
	- [Trying the samples](#trying-the-samples-android)
	- [Adding the SDK to your project](#adding-the-sdk-to-your-project-android)
	- [Using the Java API](#using-the-java-api-android)
 - [Raspberry Pi (Raspbian OS), Linux, Windows and others](#others)
 	- [Sample applications](#sample-applications-others)
		- [Benchmark](#sample-application-benchmark-others)
		- [Recognizer](#sample-application-recognizer-others)
	- [Using the C++ API](#using-the-cpp-api-others)
 - [Getting help](#technical-questions)
  
 - Full documentation at https://www.doubango.org/SDKs/credit-card-ocr/docs/
 - Online demo at https://www.doubango.org/webapps/credit-card-ocr/
 - Open source Computer Vision library: https://github.com/DoubangoTelecom/compv
  
<hr />
 
This is a state-of-the-art **DeepLayout Analysis** implementation based on Tensorflow to accurately detect, qualify, extract and recognize/OCR every field from a bank credit card using a single image: **Number, Holder's name, Validity, Company...**

Our implementation works with all cards (**credit, debit, travel, prepaid, corporate...**) from all payment networks (**Visa, MasterCard, American Express, RuPay, Discover...**).

Both **Embossed** and **UnEmbossed** formats are supported.

Unlike other implementations we're not doing brute force OCR (trying multiple images/parameters until match). You only need a single image to get the correct result. There is no template matching which means the data could be malformed or at any position and you'll still have the correct result ([WYSIWYG](https://en.wikipedia.org/wiki/WYSIWYG>)).

Next [video](https://youtu.be/BBI_3AJIc1c) ([https://youtu.be/BBI_3AJIc1c](https://youtu.be/BBI_3AJIc1c)) shows [CCardVideoRecognizer sample](#sample-application-videorecognizer-android) Running on Android: <br />
[![CCardVideoRecognizer Running on Android](https://img.youtube.com/vi/BBI_3AJIc1c/maxresdefault.jpg)](https://www.youtube.com/watch?v=BBI_3AJIc1c)
   
<hr />

You can reach **100% precision** on the credit card number recognition using [validation process](https://www.doubango.org/SDKs/credit-card-ocr/docs/Data_validation.html).

The number of use cases in FinTech industry are countless: **Scan To Pay, Helping visually impaired users, Online shopping speed-up, payment forms auto-filling, better user experience, reducing typing errors, process automation...**

Don't take our word for it, come check our implementation. **No registration, license key or internet connection is required**, just clone the code and start coding/testing. Everything runs on the device, no data is leaving your computer. 
The code released here comes with many ready-to-use samples for [Android](#sample-applications-android), [Raspberry Pi, Windows and Linux](#sample-applications-others) to help you get started easily. 

You can also check our online [cloud-based implementation](https://www.doubango.org/webapps/credit-card-ocr/) (*no registration required*) to check out the accuracy and precision before starting to play with the SDK.

Please check full documentation at https://www.doubango.org/SDKs/credit-card-ocr/docs/



<a name="getting-started"></a>
# Getting started # 
The SDK works on [many platforms](https://www.doubango.org/SDKs/credit-card-ocr/docs/Architecture_overview.html#supportedoperatingsystems) and comes with support for many [programming languages](https://www.doubango.org/SDKs/credit-card-ocr/docs/Architecture_overview.html#supportedprogramminglanguages) but the next sections focus on [Android](#android) and [Raspberry pi](#others). 

<a name="android"></a>
# Android #

The next sections are about Android and Java API.

<a name="sample-applications-android"></a>
## Sample applications (Android) ##
The source code comes with #2 Android sample applications: [Benchmark](#sample-application-benchmark-android) and [CCardVideoRecognizer](#sample-application-videorecognizer-android).

<a name="sample-application-benchmark-android"></a>
### Benchmark (Android) ###
This application is used to check everything is ok and running as fast as expected. The information about the maximum frame rate (**27fps**) on Snapdragon 855 devices could be checked using this application. It's open source and doesn't require registration or license key.

<a name="sample-application-videorecognizer-android"></a>
### CCardVideoRecognizer (Android) ###
This application should be used as reference code by any developer trying to add ultimateCreditCard to their products. It shows how to detect and recognize credit cards in realtime using live video stream from the camera.
<p align="center" style="text-align: center">
  <img src="https://www.doubango.org/SDKs/credit-card-ocr/docs/_images/poster_ocr.jpg">
  <br />
  <em><u><a href="#sample-application-videorecognizer-android">CCardVideorecognizer sample application</a> on Android</u></em>
</p>

<a name="trying-the-samples-android"></a>
## Trying the samples (Android) ##
To try the sample applications on Android:
 1. Open Android Studio and select "Open an existing Android Studio project"
![alt text](https://www.doubango.org/SDKs/credit-card-ocr/docs/_images/android_studio_open_existing_project.jpg "Open an existing Android Studio project")

 2. Navigate to **ultimateCreditCard-SDK/samples**, select **android** folder and click **OK**
![alt text](https://www.doubango.org/SDKs/credit-card-ocr/docs/_images/android_studio_select_samples_android.jpg "Select project")

 3. Select the sample you want to try (e.g. **ccardvideorecognizer**) and press **run**.
![alt text](https://www.doubango.org/SDKs/credit-card-ocr/docs/_images/android_studio_select_samples_videorecognizer.jpg "Select sample")
            
<a name="adding-the-sdk-to-your-project-android"></a>
## Adding the SDK to your project (Android) ##
The SDK is distributed as an Android Studio module and you can add it as reference or you can also build it and add the AAR to your project. But, the easiest way to add the SDK to your project is by directly including the source.

In your *build.gradle* file add:

```python
android {

      # This is the block to add within "android { } " section
      sourceSets {
         main {
             jniLibs.srcDirs += ['path-to-your-ultimateCreditCard-SDK/binaries/android/jniLibs']
             java.srcDirs += ['path-to-your-ultimateCreditCard-SDK/java/android']
             assets.srcDirs += ['path-to-your-ultimateCreditCard-SDK/assets/models']
         }
      }
}
```

<a name="using-the-java-api-android"></a>
## Using the Java API (Android) ##

It's hard to be lost when you try to use the API as there are only 3 useful functions: init, process and deInit.

The C++ API is defined [here](https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html).

```java

	import org.doubango.ultimateAlpr.Sdk.ULTCCARD_SDK_IMAGE_TYPE;
	import org.doubango.ultimateAlpr.Sdk.UltCreditCardSdkEngine;
	import org.doubango.ultimateAlpr.Sdk.UltCreditCardSdkResult;

	final static String CONFIG = "{" +
		"\"debug_level\": \"info\"," + 
		"\"gpgpu_enabled\": true," + 

		"\"detect_minscore\": 0.5," + 
		"\"detect_quantization_enabled\": true," + 

		"\"recogn_score_type\": \"min\"," + 
		"\"recogn_minscore\": 0.2," + 
		"\"recogn_rectify_enabled\": false," + 
		"\"recogn_quantization_enabled\": true" + 
	"}";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the engine
		assert UltCreditCardSdkEngine.init(
				getAssets(),
				CONFIG
		).isOK();
	}

	// Camera listener: https://developer.android.com/reference/android/media/ImageReader.OnImageAvailableListener
	final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

		@Override
		public void onImageAvailable(ImageReader reader) {
				try {
				    final Image image = reader.acquireLatestImage();
				    if (image == null) {
				        return;
				    }

				    // Credit card detection and recognition
				    final Image.Plane[] planes = image.getPlanes();
				    final UltCreditCardSdkResult result = UltCreditCardSdkEngine.process(
				        ULTCCARD_SDK_IMAGE_TYPE.ULTCCARD_SDK_IMAGE_TYPE_YUV420P,
				        planes[0].getBuffer(),
				        planes[1].getBuffer(),
				        planes[2].getBuffer(),
				        image.getWidth(),
				        image.getHeight(),
				        planes[0].getRowStride(),
				        planes[1].getRowStride(),
				        planes[2].getRowStride(),
				        planes[1].getPixelStride()
				    );
				    assert result.isOK();

				    // The result contains JSON string to parse in order to get recognitions
				    // result.json();

				    image.close();

				} catch (final Exception e) {
				   e.printStackTrace();
				}
		}
	};

	@Override
	public void onDestroy() {
		// DeInitialize the engine
		assert UltCreditCardSdkEngine.deInit().isOK();

		super.onDestroy();
	}
```

Again, please check the sample applications for [Android](#sample-applications-android) and [Raspberry Pi](#sample-applications-others) and [full documentation](https://www.doubango.org/SDKs/credit-card-ocr/docs/) for more information.

<a name="others"></a>
# Raspberry Pi (Raspbian OS), Linux, Windows and others #

<a name="sample-applications-others"></a>
## Sample applications (Others) ##
The source code comes with #2 [C++ sample applications](samples/c++): [Benchmark](#sample-application-benchmark-others) and [Recognizer](#sample-application-recognizer-others). These sample applications can be used on all supported platforms: **Android**, **Windows**, **Raspberry pi**, **iOS**, **OSX**, **Linux**...

<a name="sample-application-benchmark-others"></a>
### Benchmark (Others) ###
This application is used to check everything is ok and running as fast as expected. 
The information about the maximum frame rate could be checked using this application. 
It's open source and doesn't require registration or license key.

For more information on how to build and run this sample please check [samples/c++/benchmark](samples/c++/benchmark/README.md).

<a name="sample-application-recognizer-others"></a>
### Recognizer (Others) ###
This is a command line application used to detect and recognize a license plate from any JPEG/PNG/BMP image.

For more information on how to build and run this sample please check [samples/c++/recognizer](samples/c++/recognizer/README.md).

<a name="using-the-cpp-api-others"></a>
## Using the C++ API ##
The C++ API is defined at https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html.

```cpp
	#include <ultimateCreditCard-SDK-API-PUBLIC.h> // Include the API header file

	// JSON configuration string
	// More info at https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html
	static const char* __jsonConfig =
	"{"
	"\"debug_level\": \"info\","
	"\"debug_write_input_image_enabled\": false,"
	"\"debug_internal_data_path\": \".\","
	""
	"\"num_threads\": -1,"
	"\"gpgpu_enabled\": true,"
	""
	"\"detect_roi\": [0, 0, 0, 0],"
	"\"detect_minscore\": 0.5,"
	""
	"\"recogn_minscore\": 0.2,"
	"\"recogn_score_type\": \"min\""
	"}";

	// Local variable
	UltCreditCardSdkResult result;

	// Initialize the engine (should be done once)
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::init(
		__jsonConfig
	)).isOK());

	// Processing (detection + recognition)
	// Call this function for every video frame
	const void* imageData = nullptr;
	ULTCCARD_SDK_ASSERT((*result_ = UltCreditCardSdkEngine::process(
			ULTCCARD_SDK_IMAGE_TYPE_RGB24,
			imageData,
			imageWidth,
			imageHeight
		)).isOK());

	// DeInit
	// Call this function before exiting the app to free the allocate resources
	// You must not call process() after calling this function
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::deInit()).isOK());
```

Again, please check the [sample applications](#sample-applications-others) for more information on how to use the API.

<a name="technical-questions"></a>
 # Technical questions #
 Please check our [discussion group](https://groups.google.com/forum/#!forum/doubango-ai) or [twitter account](https://twitter.com/doubangotelecom?lang=en)
