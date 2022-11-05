/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateCreditCard-SDK
* WebSite: https://www.doubango.org/webapps/credit-card-ocr/
*/
/**
\file ultimateCreditCard-SDK-API-PUBLIC.h
ultimateCreditCard SDK public header
*/
#ifndef _ULTIMATE_CCARD_SDK_API_PUBLIC_H_
#define _ULTIMATE_CCARD_SDK_API_PUBLIC_H_

#include <string>

#define ULTCCARD_SDK_VERSION_MAJOR		2
#define ULTCCARD_SDK_VERSION_MINOR		6
#define ULTCCARD_SDK_VERSION_MICRO		2

// Windows's symbols export
#if defined(SWIG)
# 	define ULTIMATE_CCARD_SDK_PUBLIC_API
#else
#	if (defined(WIN32) || defined(_WIN32) || defined(_WIN32_WCE) || defined(_WIN16) || defined(_WIN64) || defined(__WIN32__) || defined(__TOS_WIN__) || defined(__WINDOWS__)) && !defined(ULTCCARD_SDK_STATIC)
#		if defined(ULTIMATE_CCARD_SDK_PUBLIC_EXPORTS)
# 			define ULTIMATE_CCARD_SDK_PUBLIC_API		__declspec(dllexport)
#		else
# 			define ULTIMATE_CCARD_SDK_PUBLIC_API		__declspec(dllimport)
#		endif
#	else
# 		define ULTIMATE_CCARD_SDK_PUBLIC_API			__attribute__((visibility("default")))
#	endif /* WIN32 */
#endif /* SWIG */

// Android OS detection
#if (defined(__ANDROID__) || defined(ANDROID)) && !defined(SWIG)
#	define ULTCCARD_SDK_OS_ANDROID	1
#endif /* ULTCCARD_SDK_OS_ANDROID */

// Macros to print logs to the console
#if ULTCCARD_SDK_OS_ANDROID
#	if !defined(SWIG)
#		include <android/log.h>
#		include <android/asset_manager.h>
#		include <jni.h>
#	endif
#	define ULTCCARD_SDK_PRINT_VERBOSE(FMT, ...) __android_log_print(ANDROID_LOG_VERBOSE, "org.doubango.ultimateCreditCard.Sdk", "*[ULTCCARD_SDK VERBOSE]: " FMT "\n", ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_INFO(FMT, ...) __android_log_print(ANDROID_LOG_INFO, "org.doubango.ultimateCreditCard.Sdk", "*[ULTCCARD_SDK INFO]: " FMT "\n", ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_WARN(FMT, ...) __android_log_print(ANDROID_LOG_WARN, "org.doubango.ultimateCreditCard.Sdk", "**[ULTCCARD_SDK WARN]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_ERROR(FMT, ...) __android_log_print(ANDROID_LOG_ERROR, "org.doubango.ultimateCreditCard.Sdk", "***[ULTCCARD_SDK ERROR]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_FATAL(FMT, ...) __android_log_print(ANDROID_LOG_FATAL, "org.doubango.ultimateCreditCard.Sdk", "****[ULTCCARD_SDK FATAL]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#else
#	define ULTCCARD_SDK_PRINT_VERBOSE(FMT, ...) fprintf(stderr, "*[ULTCCARD_SDK VERBOSE]: " FMT "\n", ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_INFO(FMT, ...) fprintf(stderr, "*[ULTCCARD_SDK INFO]: " FMT "\n", ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_WARN(FMT, ...) fprintf(stderr, "**[ULTCCARD_SDK WARN]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_ERROR(FMT, ...) fprintf(stderr, "***[ULTCCARD_SDK ERROR]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#	define ULTCCARD_SDK_PRINT_FATAL(FMT, ...) fprintf(stderr, "****[ULTCCARD_SDK FATAL]: function: \"%s()\" \nfile: \"%s\" \nline: \"%u\" \nmessage: " FMT "\n", __FUNCTION__,  __FILE__, __LINE__, ##__VA_ARGS__)
#endif /* !ULTCCARD_SDK_OS_ANDROID */

// Assertion function
#if defined(NDEBUG)
#	define ULTCCARD_SDK_ASSERT(x) do { bool __ULTCCARD_SDK_b_ret = (x); if (!__ULTCCARD_SDK_b_ret) { ULTCCARD_SDK_PRINT_FATAL("Assertion failed!"); abort(); } } while(0)
#else
#	define ULTCCARD_SDK_ASSERT(x) do { bool __ULTCCARD_SDK_b_ret = (x); assert(__ULTCCARD_SDK_b_ret); } while(0)
#endif /* !NDEBUG */

namespace ultimateCreditCardSdk  
{
	/*! Defines the image types.
	*/
	enum ULTCCARD_SDK_IMAGE_TYPE {
		/*! Each pixel is stored on 3 bytes. Each channel (R, G, B) is stored with 8 bits of precision (256 possible values).
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
		* \endcode
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_RGB24,

		/*! Each pixel is stored on 4 bytes. Each channel (R, G, B, A) is stored with 8 bits (1 byte) of precision (256 possible values).
		* The R channel is stored at the lowest memory address followed by G, B then A channels. If you're using Android then,
		* this is the same as <a href="https://developer.android.com/reference/android/graphics/Bitmap.Config#ARGB_8888">ARGB_8888</a>.
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
		* \endcode
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_RGBA32,

		/*! Each pixel is stored on 4 bytes. Each channel (B, G, R, A) is stored with 8 bits (1 byte) of precision (256 possible values).
		* The B channel is stored at the lowest memory address followed by G, R then A channels. If you're using iOS then,
		* this is the same as <a href="https://developer.apple.com/documentation/corevideo/1563591-pixel_format_identifiers/kcvpixelformattype_32bgra?language=objc">kCVPixelFormatType_32BGRA</a>.
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
		* \endcode
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_BGRA32,

		/*! YUV 4:2:0 image with a plane of 8 bit Y samples followed by an interleaved U/V plane containing 8 bit 2x2 subsampled colour difference samples.
		*	More information at https://www.fourcc.org/pixel-format/yuv-nv12/
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_NV12,

		/*! YUV 4:2:0 image with a plane of 8 bit Y samples followed by an interleaved V/U plane containing 8 bit 2x2 subsampled chroma samples. 
		* The same as \ref ULTCCARD_SDK_IMAGE_TYPE_NV12 except the interleave order of U and V is reversed.
		*	More information at https://www.fourcc.org/pixel-format/yuv-nv21/
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_NV21,

		/*! These formats are identical to YV12 except that the U and V plane order is reversed. 
		* They comprise an NxM Y plane followed by (N/2)x(M/2) U and V planes. 
		* This is the format of choice for many software MPEG codecs.
		* More information at https://www.fourcc.org/pixel-format/yuv-i420/
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_YUV420P,

		/*! Same as \ref ULTCCARD_SDK_IMAGE_TYPE_YUV420P except the order of U and V is reversed.
		* More information at https://www.fourcc.org/pixel-format/yuv-yv12/
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_YVU420P,

		/*! YUV 4:2:2 image with an NxM Y plane followed by (N/2)x(M) V and U planes.
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_YUV422P,

		/*! YUV 4:4:4 image with an NxM Y plane followed by NxM V and U planes.
		*
		*/
		ULTCCARD_SDK_IMAGE_TYPE_YUV444P,

		/*! Grayscale image with single channel (luminance only). Each pixel is stored in single byte (8 bit Y samples).
		*
		* Available since: 2.1.0
		*/
		ULTCCARD_SDK_IMAGE_TYPE_Y,
		/*! Each pixel is stored on 3 bytes. Each channel (B, G, R) is stored with 8 bits (1 byte) of precision (256 possible values).
		* The B channel is stored at the lowest memory address followed by G then R channels. If you're using C# then,
		* this is the same as <b>PixelFormat.Format24bppRgb</b>.
		* Here is how the pixels are packed:
		* \code{.cpp}
		* const int pixel = (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
		* \endcode
		*
		* Available since: 2.2.0
		*/
		ULTCCARD_SDK_IMAGE_TYPE_BGR24,
	};

	/*! Result returned by the \ref UltCreditCardSdkEngine "engine" at initialization, deInitialization and processing stages.
	*/
	class ULTIMATE_CCARD_SDK_PUBLIC_API UltCreditCardSdkResult {
	public:
		UltCreditCardSdkResult();
		UltCreditCardSdkResult(const int code, const char* phrase, const char* json, const size_t numZones = 0);
		UltCreditCardSdkResult(const UltCreditCardSdkResult& other);
		virtual ~UltCreditCardSdkResult();
#if !defined(SWIG)
		UltCreditCardSdkResult& operator=(const UltCreditCardSdkResult& other) { return operatorAssign(other); }
#endif

		/*! The result code. 0 if success, nonzero otherwise.
		*/
		inline int code()const { return code_; }
		/*! Short description for the \ref code.
		*/
		inline const char* phrase()const { return phrase_; }
		/*! The credit cards as JSON content string. May be null if no card found.
		*/
		inline const char* json()const { return json_; }
		/*! Number of credit cards in \ref json string. This is a helper function to quickly check whether the result contains credit cards
			without parsing the \ref json string.
		*/
		inline const size_t numCards()const { return numCards_; }
		/*! Whether the result is success. true if success, false otherwise.
		*/
		inline bool isOK()const { return (code_ == 0); }
#if !defined(SWIG)
		static UltCreditCardSdkResult bodyless(const int code, const char* phrase) { return UltCreditCardSdkResult(code, phrase, ""); }
		static UltCreditCardSdkResult bodylessOK() { return UltCreditCardSdkResult(0, "OK", ""); }
#endif /* SWIG */

	private:
		void ctor(const int code, const char* phrase, const char* json, const size_t numZones);
#if !defined(SWIG)
		UltCreditCardSdkResult& operatorAssign(const UltCreditCardSdkResult& other);
#endif /* SWIG */

	private:
		int code_;
		char* phrase_ = nullptr;
		char* json_ = nullptr;
		size_t numCards_;
	};

	/*! Callback function to be used to get asynchronous notifications.
	*/
	struct UltCreditCardSdkParallelDeliveryCallback 
	{
	protected:
		UltCreditCardSdkParallelDeliveryCallback() { }
	public:
		virtual ~UltCreditCardSdkParallelDeliveryCallback() {  }
		/*! Notification function to override in order to receive the results. */
		virtual void onNewResult(const UltCreditCardSdkResult* newResult) const = 0;
	};

	/*! The Credit Card Deep Learning engine.
	*/
	class ULTIMATE_CCARD_SDK_PUBLIC_API UltCreditCardSdkEngine
	{
#if !defined(SWIG)
	protected:
		UltCreditCardSdkEngine() = delete;
#endif /* SWIG */
	public:

#if ULTCCARD_SDK_OS_ANDROID
		/*! Initializes the engine. This function must be the first one to call.
			This function is only available on Android.
			\param assetManager AssetManager to use to read the content of the "assets" folder containing the models and configuration files.
			\param jsonConfig JSON string containing configuration entries. May be null. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
			\returns a result
		*/
		static UltCreditCardSdkResult init(jobject assetManager, const char* jsonConfig = nullptr);
#else
		/*! Initializes the engine. This function must be the first one to call.
			\param jsonConfig JSON string containing configuration entries. May be null. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html
			\returns a \ref UltCreditCardSdkResult "result"
		*/
		static UltCreditCardSdkResult init(const char* jsonConfig = nullptr);
#endif /* ULTCCARD_SDK_OS_ANDROID */

		/*! DeInitialize the engine. This function must be the last one to be call.
			Deallocate all the resources allocated using \ref init function.
			\returns a \ref UltCreditCardSdkResult "result"
		*/
		static UltCreditCardSdkResult deInit();

		/*! Performs credit card detection and recognition operations.
			\param imageType The image type.
			\param imageData Pointer to the image data.
			\param imageWidthInSamples Image width in samples.
			\param imageHeightInSamples Image height in samples.
			\param imageStrideInSamples Image stride in samples. Should be zero unless your the data is strided.
			\param imageExifOrientation Image EXIF/JPEG orientation. Must be within [1, 8]. More information at https://www.impulseadventure.com/photo/exif-orientation.html.
			\returns a \ref UltCreditCardSdkResult "result"
		*/
		static UltCreditCardSdkResult process(
			const ULTCCARD_SDK_IMAGE_TYPE imageType, 
			const void* imageData, 
			const size_t imageWidthInSamples,
			const size_t imageHeightInSamples,
			const size_t imageStrideInSamples = 0,
			const int imageExifOrientation = 1
		);

		/*! Performs credit card detection and recognition operations.
			\param imageType The image type.
			\param yPtr Pointer to the start of the Y (luma) samples.
			\param uPtr Pointer to the start of the U (chroma) samples.
			\param vPtr Pointer to the start of the V (chroma) samples.
			\param widthInSamples Image width in samples.
			\param heightInSamples Image height in samples.
			\param yStrideInBytes Stride in bytes for the Y (luma) samples.
			\param uStrideInBytes Stride in bytes for the U (chroma) samples.
			\param vStrideInBytes Stride in bytes for the V (chroma) samples.
			\param uvPixelStrideInBytes Pixel stride in bytes for the UV (chroma) samples. Should be 1 for planar and 2 for semi-planar formats. Set to 0 for auto-detect.
			\param exifOrientation Image EXIF/JPEG orientation. Must be within [1, 8]. More information at https://www.impulseadventure.com/photo/exif-orientation.html.
			\returns a \ref UltCreditCardSdkResult "result"
		*/
		static UltCreditCardSdkResult process(
			const ULTCCARD_SDK_IMAGE_TYPE imageType,
			const void* yPtr,
			const void* uPtr,
			const void* vPtr,
			const size_t widthInSamples,
			const size_t heightInSamples,
			const size_t yStrideInBytes,
			const size_t uStrideInBytes,
			const size_t vStrideInBytes,
			const size_t uvPixelStrideInBytes = 0,
			const int exifOrientation = 1
		);

		/*! Retrieve EXIF orientation value from JPEG meta-data.
			\param jpegMetaDataPtr Pointer to the meta-data.
			\param jpegMetaDataSize Size of the meta-data.
			\returns Image's EXIF/JPEG orientation. Must be within [1, 8]. More information at https://www.impulseadventure.com/photo/exif-orientation.html.

			Available since: 2.5.0
		*/
		static int exifOrientation(const void* jpegMetaDataPtr, const size_t jpegMetaDataSize);
		
		/*! Build a unique runtime license key associated to this device.
			You must \ref init "initialize" the engine before calling this function.
			This function doesn't require internet connection.
			The runtime key must be activated to obtain a token. The activation procedure is explained at https://www.doubango.org/SDKs/LicenseManager/docs/Activation_use_cases.html.
			\param rawInsteadOfJSON Whether to output the runtime key as raw string intead of JSON entry. Requesting raw
				string instead of JSON could be helpful for applications without JSON parser to extract the key.
			\returns a \ref UltCreditCardSdkResult "result"
		*/
		static UltCreditCardSdkResult requestRuntimeLicenseKey(const bool& rawInsteadOfJSON = false);

		static UltCreditCardSdkResult warmUp(const ULTCCARD_SDK_IMAGE_TYPE imageType);

#if ULTCCARD_SDK_OS_ANDROID && !defined(SWIG)
		static void setAssetManager(AAssetManager* assetManager);
	private:
		static bool s_bOweAAssetManager;
#endif /* ULTCCARD_SDK_OS_ANDROID */
	};

} // namespace ultimateCreditCardSdk 

#endif /* _ULTIMATE_CCARD_SDK_API_PUBLIC_H_ */
