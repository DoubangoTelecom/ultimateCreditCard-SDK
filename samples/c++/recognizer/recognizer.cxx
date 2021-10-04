/* Copyright (C) 2011-2021 Doubango Telecom <https://www.doubango.org>
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
			[--ielcd <whether-to-enable-IELCD:true/false>] \
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
#include <sys/stat.h>
#include <map>
#if defined(_WIN32)
#include <algorithm> // std::replace
#endif

// Not part of the SDK, used to decode images -> https://github.com/nothings/stb
#define STB_IMAGE_IMPLEMENTATION
#define STB_IMAGE_STATIC
#include "../stb_image.h"

using namespace ultimateCreditCardSdk;

// Asset manager used on Android to files in "assets" folder
#if ULTCCARD_SDK_OS_ANDROID 
#	define ASSET_MGR_PARAM() __sdk_android_assetmgr, 
#else
#	define ASSET_MGR_PARAM() 
#endif /* ULTCCARD_SDK_OS_ANDROID */

struct CCardFile {
	int width = 0, height = 0, channels = 0;
	stbi_uc* uncompressedDataPtr = nullptr;
	void* compressedDataPtr = nullptr;
	size_t compressedDataSize = 0;
	FILE* filePtr = nullptr;
	virtual ~CCardFile() {
		if (uncompressedDataPtr) free(uncompressedDataPtr), uncompressedDataPtr = nullptr;
		if (compressedDataPtr) free(compressedDataPtr), compressedDataPtr = nullptr;
		if (filePtr) fclose(filePtr), filePtr = nullptr;
}
	bool isValid() const {
		return width > 0 && height > 0 && (channels == 1 || channels == 3 || channels == 4) && uncompressedDataPtr && compressedDataPtr && compressedDataSize > 0;
	}
};

static void printUsage(const std::string& message = "");
static bool parseArgs(int argc, char *argv[], std::map<std::string, std::string >& values);
static bool readFile(const std::string& path, CCardFile& file);

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

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	// local variables
	UltCreditCardSdkResult result(0, "OK", "{}");
	std::string assetsFolder, licenseTokenData, licenseTokenFile;
#if defined(__arm__) || defined(__thumb__) || defined(__TARGET_ARCH_ARM) || defined(__TARGET_ARCH_THUMB) || defined(_ARM) || defined(_M_ARM) || defined(_M_ARMT) || defined(__arm) || defined(__aarch64__)
	bool isRectificationEnabled = false;
	bool ielcdEnabled = false;
#else
	bool isRectificationEnabled = true;
	bool ielcdEnabled = true;
#endif
	std::string pathFileImage;

	// Parsing args
	std::map<std::string, std::string > args;
	if (!parseArgs(argc, argv, args)) {
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
	if (args.find("--ielcd") != args.end()) {
		ielcdEnabled = (args["--ielcd"] == "true");
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
	jsonConfig += std::string(",\"ielcd_enabled\": ") + (ielcdEnabled ? "true" : "false");
	if (!licenseTokenFile.empty()) {
		jsonConfig += std::string(",\"license_token_file\": \"") + licenseTokenFile + std::string("\"");
	}
	if (!licenseTokenData.empty()) {
		jsonConfig += std::string(",\"license_token_data\": \"") + licenseTokenData + std::string("\"");
	}
	
	jsonConfig += "}"; // end-of-config

	// Decode the file
	CCardFile file;
	if (!readFile(pathFileImage, file)) {
		ULTCCARD_SDK_PRINT_ERROR("Can't process %s", pathFileImage.c_str());
		return -1;
	}
	ULTCCARD_SDK_ASSERT(file.isValid());

	// Init
	ULTCCARD_SDK_PRINT_INFO("Starting recognizer...");
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::init(
		ASSET_MGR_PARAM()
		jsonConfig.c_str()
	)).isOK());

	// Recognize/Process
	ULTCCARD_SDK_ASSERT((result = UltCreditCardSdkEngine::process(
		file.channels == 4 ? ULTCCARD_SDK_IMAGE_TYPE_RGBA32 : (file.channels == 1 ? ULTCCARD_SDK_IMAGE_TYPE_Y : ULTCCARD_SDK_IMAGE_TYPE_RGB24),
		file.uncompressedDataPtr,
		static_cast<size_t>(file.width),
		static_cast<size_t>(file.height),
		0, // stride
		UltCreditCardSdkEngine::exifOrientation(file.compressedDataPtr, file.compressedDataSize)
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
		"\t[--rectify <whether-to-enable-rectification-layer:true/false>] \n"
		"\t[--ielcd <whether-to-enable-IELCD:true/false>] \n"
		"\t[--tokenfile <path-to-license-token-file>] \n"
		"\t[--tokendata <base64-license-token-data>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--image: Path to the image(JPEG/PNG/BMP) to process. You can use default image at ../../../assets/images/revolut.jpg.\n\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n\n"
		"--rectify: Whether to enable the rectification layer. More info about the rectification layer at https ://www.doubango.org/SDKs/ccard/docs/Rectification_layer.html. Default: false.\n\n"
		"--ielcd: Whether to enable Image Enhancement for Low Contrast Document (IELCD). More information at https://www.doubango.org/SDKs/credit-card-ocr/docs/IELCD.html. Default: true for x86 CPUs and false for ARM CPUs.\n\n"
		"--tokenfile: Path to the file containing the base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"--tokendata: Base64 license token if you have one. If not provided then, the application will act like a trial version. Default: null.\n\n"
		"********************************************************************************\n"
	);
}

static bool parseArgs(int argc, char *argv[], std::map<std::string, std::string >& values)
{
	ULTCCARD_SDK_ASSERT(argc > 0 && argv != nullptr);

	values.clear();

	// Make sure the number of arguments is even
	if ((argc - 1) & 1) {
		ULTCCARD_SDK_PRINT_ERROR("Number of args must be even");
		return false;
	}

	// Parsing
	for (int index = 1; index < argc; index += 2) {
		std::string key = argv[index];
		if (key.size() < 2 || key[0] != '-' || key[1] != '-') {
			ULTCCARD_SDK_PRINT_ERROR("Invalid key: %s", key.c_str());
			return false;
		}
		values[key] = argv[index + 1];
	}

	return true;
}

static bool readFile(const std::string& path, CCardFile& file)
{
	// Open the file
	if ((file.filePtr = fopen(path.c_str(), "rb")) == nullptr) {
		ULTCCARD_SDK_PRINT_ERROR("Can't open %s", path.c_str());
		return false;
	}

	// Retrieve file size
	struct stat st_;
	if (stat(path.c_str(), &st_) != 0) {
		ULTCCARD_SDK_PRINT_ERROR("File is empty %s", path.c_str());
	}
	file.compressedDataSize = static_cast<size_t>(st_.st_size);

	// Alloc memory and read data
	file.compressedDataPtr = ::malloc(file.compressedDataSize);
	if (!file.compressedDataPtr) {
		ULTCCARD_SDK_PRINT_ERROR("Failed to alloc mem with size = %zu", file.compressedDataSize);
		return false;
	}
	size_t read_;
	if (file.compressedDataSize != (read_ = fread(file.compressedDataPtr, 1, file.compressedDataSize, file.filePtr))) {
		ULTCCARD_SDK_PRINT_ERROR("fread(%s) returned %zu instead of %zu", path.c_str(), read_, file.compressedDataSize);
		return false;
	}

	// Decode image
	file.uncompressedDataPtr = stbi_load_from_memory(
		reinterpret_cast<stbi_uc const *>(file.compressedDataPtr), static_cast<int>(file.compressedDataSize),
		&file.width, &file.height, &file.channels, 0
	);

	return file.isValid();
}
