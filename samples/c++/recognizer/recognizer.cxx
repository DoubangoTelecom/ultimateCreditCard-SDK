/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateCreditCard-SDK
* WebSite: https://www.doubango.org/webapps/credit-card-ocr/
*/

/*
	https://github.com/DoubangoTelecom/ultimateCreditCard/blob/master/SDK_dist/samples/c++/recognizer/README.md
	Usage: 
		recognizer \
			--image <path-to-image-with-to-recognize> \
			[--parallel <whether-to-enable-parallel-mode:true/false>] \
			[--rectify <whether-to-enable-rectification-layer:true/false>] \
			[--assets <path-to-assets-folder>] \
			[--tokenfile <path-to-license-token-file>] \
			[--tokendata <base64-license-token-data>]

	Example:
		recognizer \
			--image C:/Projects/GitHub/ultimate/ultimateCreditCard/SDK_dist/assets/images/revolut.jpg \
			--rectify false \
			--assets C:/Projects/GitHub/ultimate/ultimateCreditCard/SDK_dist/assets \
			--tokenfile C:/Projects/GitHub/ultimate/ultimateCreditCard/SDK_dev/tokens/windows-iMac.lic
		
*/

#include <ultimateCreditCard-SDK-API-PUBLIC.h>
#include "../ccard_utils.h"
#if defined(_WIN32)
#include <algorithm> // std::replace
#endif

using namespace ultimateCreditCardSdk;

// Configuration for ANPR deep learning engine
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
"";

// Asset manager used on Android to files in "assets" folder
#if ULTCCARD_SDK_OS_ANDROID 
#	define ASSET_MGR_PARAM() __sdk_android_assetmgr, 
#else
#	define ASSET_MGR_PARAM() 
#endif /* ULTCCARD_SDK_OS_ANDROID */

static void printUsage(const std::string& message = "");

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	// local variables
	UltCreditCardSdkResult result(0, "OK", "{}");
	std::string assetsFolder, licenseTokenData, licenseTokenFile;
	bool isRectificationEnabled = false;
	std::string pathFileImage;

	// Parsing args
	std::map<std::string, std::string > args;
	if (!alprParseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}
	if (args.find("--image") == args.end()) {
		printUsage("--image required");
		return -1;
	}
	pathFileImage = args["--image"];
		
	if (args.find("--assets") != args.end()) {
		assetsFolder = args["--assets"];
#if defined(_WIN32)
		std::replace(assetsFolder.begin(), assetsFolder.end(), '\\', '/');
#endif
	}
	if (args.find("--rectify") != args.end()) {
		isRectificationEnabled = (args["--rectify"].compare("true") == 0);
	}	
	if (args.find("--tokenfile") != args.end()) {
		licenseTokenFile = args["--tokenfile"];
#if defined(_WIN32)
		std::replace(licenseTokenFile.begin(), licenseTokenFile.end(), '\\', '/');
#endif
	}
	if (args.find("--tokendata") != args.end()) {
		licenseTokenData = args["--tokendata"];
	}

	// Update JSON config
	std::string jsonConfig = __jsonConfig;
	if (!assetsFolder.empty()) {
		jsonConfig += std::string(",\"assets_folder\": \"") + assetsFolder + std::string("\"");
	}
	jsonConfig += std::string(",\"recogn_rectify_enabled\": ") + (isRectificationEnabled ? "true" : "false");
	if (!licenseTokenFile.empty()) {
		jsonConfig += std::string(",\"license_token_file\": \"") + licenseTokenFile + std::string("\"");
	}
	if (!licenseTokenData.empty()) {
		jsonConfig += std::string(",\"license_token_data\": \"") + licenseTokenData + std::string("\"");
	}
	
	jsonConfig += "}"; // end-of-config

	// Decode image
	CreditCardFile fileImage;
	if (!alprDecodeFile(pathFileImage, fileImage)) {
		ULTCCARD_SDK_PRINT_INFO("Failed to read image file: %s", pathFileImage.c_str());
		return -1;
	}

	// Init
	ULTCCARD_SDK_PRINT_INFO("Starting recognizer...");
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str()
	)).isOK());

	// Recognize/Process
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::process(
		fileImage.type, // If you're using data from your camera then, the type would be YUV-family instead of RGB-family. https://www.doubango.org/SDKs/ccard/docs/cpp-api.html#_CPPv4N15ultimateCreditCardSdk22ULTCCARD_SDK_IMAGE_TYPEE
		fileImage.uncompressedData,
		fileImage.width,
		fileImage.height
	)).isOK());
	ULTCCARD_SDK_PRINT_INFO("Processing done.");

	// Print latest result
	const std::string& json_ = result.json();
	if (!json_.empty()) {
		ULTCCARD_SDK_PRINT_INFO("result: %s", json_.c_str());
	}

	ULTCCARD_SDK_PRINT_INFO("Press any key to terminate !!");
	getchar();

	// DeInit
	ULTCCARD_SDK_PRINT_INFO("Ending recognizer...");
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::deInit()).isOK());

	return 0;
}

/*
* Print usage
*/
static void printUsage(const std::string& message /*= ""*/)
{
	if (!message.empty()) {
		ULTCCARD_SDK_PRINT_ERROR("%s", message.c_str());
	}

	ULTCCARD_SDK_PRINT_INFO(
		"\n********************************************************************************\n"
		"recognizer\n"
		"\t--image <path-to-image-with-to-recognize> \n"
		"\t[--assets <path-to-assets-folder>] \n"
		"\t[--rectify <whether-to-enable-rectification-layer:true / false>] \n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--image: Path to the image(JPEG/PNG/BMP) to process. You can use default image at ../../../assets/images/revolut.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n\n"
		"--rectify: Whether to enable the rectification layer. More info about the rectification layer at https ://www.doubango.org/SDKs/ccard/docs/Rectification_layer.html. Default: false.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"********************************************************************************\n"
	);
}
