/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateCreditCard-SDK
* WebSite: https://www.doubango.org/webapps/credit-card-ocr/
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.lang.IllegalArgumentException;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;

import org.doubango.ultimateCreditCard.Sdk.ULTCCARD_SDK_IMAGE_TYPE;
import org.doubango.ultimateCreditCard.Sdk.UltCreditCardSdkEngine;
import org.doubango.ultimateCreditCard.Sdk.UltCreditCardSdkResult;

public class Recognizer {

   /**
   * Defines the debug level to output on the console. You should use "verbose" for diagnostic, "info" in development stage and "warn" on production.
   * JSON name: "debug_level"
   * Default: "info"
   * type: string
   * pattern: "verbose" | "info" | "warn" | "error" | "fatal"
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#debug-level
   */
  static final String CONFIG_DEBUG_LEVEL = "info";

   /**
   * Whether to write the transformed input image to the disk. This could be useful for debugging.
   * JSON name: "debug_write_input_image_enabled"
   * Default: false
   * type: bool
   * pattern: true | false
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#debug-write-input-image-enabled
   */
  static final boolean CONFIG_DEBUG_WRITE_INPUT_IMAGE = false; // must be false unless you're debugging the code

   /**
    * Path to the folder where to write the transformed input image. Used only if "debug_write_input_image_enabled" is true.
   * JSON name: "debug_internal_data_path"
   * Default: ""
   * type: string
   * pattern: folder path
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#debug-internal-data-path
   */
  static final String CONFIG_DEBUG_DEBUG_INTERNAL_DATA_PATH = ".";

   /**
   * Defines the maximum number of threads to use.
   * You should not change this value unless you know what you’re doing. Set to -1 to let the SDK choose the right value.
   * The right value the SDK will choose will likely be equal to the number of virtual cores.
   * For example, on an octa-core device the maximum number of threads will be 8.
   * JSON name: "num_threads"
   * Default: -1
   * type: int
   * pattern: [-inf, +inf]
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#num-threads
   */
  static final int CONFIG_NUM_THREADS = -1;

   /**
   * Whether to enable GPGPU computing. This will enable or disable GPGPU computing on the computer vision and deep learning libraries.
   * On ARM devices this flag will be ignored when fixed-point (integer) math implementation exist for a well-defined function.
   * For example, this function will be disabled for the bilinear scaling as we have a fixed-point SIMD accelerated implementation.
   * Same for many deep learning parts as we’re using QINT8 quantized inference.
   * JSON name: "gpgpu_enabled"
   * Default: true
   * type: bool
   * pattern: true | false
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#gpgpu-enabled
   */
  static final boolean CONFIG_GPGPU_ENABLED = true;

   /**
   * Whether to enable Image Enhancement for Low Contrast Document (IELCD).
   * Technical description at https://www.doubango.org/SDKs/credit-card-ocr/docs/IELCD.html#ielcd.
   * JSON name: "ielcd_enabled"
   * Default: true for x86 CPUs and false for ARM CPUs.
   * type: bool
   * pattern: true | false
   * Available since: 2.3.0
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#ielcd-enabled
   */
  static final boolean CONFIG_IELCD_ENABLED = System.getProperty("os.arch").equals("amd64");

   /**
   * Define a threshold for the detection score. Any detection with a score below that threshold will be ignored. 0.f being poor confidence and 1.f excellent confidence.
   * JSON name: "detect_minscore",
   * Default: 0.5f
   * type: float
   * pattern: ]0.f, 1.f]
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#detect-minscore
   */
  static final double CONFIG_DETECT_MINSCORE = 0.5; // 50%

   /**
   * Defines the Region Of Interest (ROI) for the detector. Any pixels outside region of interest will be ignored by the detector.
   * Defining an WxH region of interest instead of resizing the image at WxH is very important as you'll keep the same quality when you define a ROI while you'll lose in quality when using the later.
   * JSON name: "detect_roi"
   * Default: [0.f, 0.f, 0.f, 0.f]
   * type: float[4]
   * pattern: [left, right, top, bottom]
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#detect-roi
   */
   static final List<Float> CONFIG_DETECT_ROI = Arrays.asList(0.f, 0.f, 0.f, 0.f);

   /**
   * Define a threshold for the overall recognition score. Any recognition with a score below that threshold will be ignored.
   * The overall score is computed based on "recogn_score_type". 0.f being poor confidence and 1.f excellent confidence.
   * JSON name: "recogn_minscore"
   * Default: 0.2f
   * type: float
   * pattern: ]0.f, 1.f]
   * More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#recogn-minscore
   */
  static final double CONFIG_RECOGN_MINSCORE = 0.2; // 20%

   /**
   * Defines the overall score type. The recognizer outputs a recognition score ([0.f, 1.f]) for every character in the license plate.
   * The score type defines how to compute the overall score.
   * - "min": Takes the minimum score.
   * - "mean": Takes the average score.
   * - "median": Takes the median score.
   * - "max": Takes the maximum score.
   * - "minmax": Takes (max + min) * 0.5f.
   * The "min" score is the more robust type as it ensure that every character have at least a certain confidence value.
   * The median score is the default type as it provide a higher recall. In production we recommend using min type.
   * JSON name: "recogn_score_type"
   * Default: "median"
   * Recommended: "min"
   * type: string
   *  More info: https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#recogn-score-type
   */
  static final String CONFIG_RECOGN_SCORE_TYPE = "min";

   /**
   * Whether to add rectification layer between the detector's output and the recognizer's input. A rectification layer is used to suppress the distortion.
   * A plate is distorted when it's skewed and/or slanted. The rectification layer will deslant and deskew the plate to make it straight which make the recognition more accurate.
   * Please note that you only need to enable this feature when the license plates are highly distorted. The implementation can handle moderate distortion without a rectification layer.
   * The rectification layer adds many CPU intensive operations to the pipeline which decrease the frame rate.
   * More info on the rectification layer could be found at https://www.doubango.org/SDKs/credit-card-ocr/docs/Rectification_layer.html#rectificationlayer
   * JSON name: "recogn_rectify_enabled"
   * Default: false
   * Recommended: false
   * type: string
   * More info at https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#recogn-rectify-enabled
   */
  static final boolean CONFIG_RECOGN_RECTIFY_ENABLED = false;

   public static void main(String[] args) throws IllegalArgumentException, FileNotFoundException, IOException {
      // Parse arguments
      final Hashtable<String, String> parameters = ParseArgs(args);

      // Make sur the image is provided using args
      if (!parameters.containsKey("--image"))
      {
         System.err.println("--image required");
         throw new IllegalArgumentException("--image required");
      }
      // Extract assets folder
      // https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#assets-folder
      String assetsFolder = parameters.containsKey("--assets")
          ? parameters.get("--assets") : "";

      // License data - Optional
      // https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html#license-token-data
      String tokenDataBase64 = parameters.containsKey("--tokendata")
          ? parameters.get("--tokendata") : "";

      //!\\ This is a quick and dirty way to load the library. You should not use it:
      // create a static block outside the main function and load the library from there.
      // In the next version we'll make sure the library has the same name regardless the platform/OS.
      System.loadLibrary(System.getProperty("os.name").toLowerCase().contains("win") ? "ultimateCreditCard-SDK" : "ultimate_creditcard-sdk");

      // Initialize the engine: Load deep learning models and init GPU shaders
      // Make sure de disable VS hosting process to see logs from native code: https://social.msdn.microsoft.com/Forums/en-US/5da6cdb2-bc2b-4fff-8adf-752b32143dae/printf-from-dll-in-console-app-in-visual-studio-c-2010-express-does-not-output-to-console-window?forum=Vsexpressvcs
      // This function should be called once.
      // https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html#_CPPv4N21ultimateCreditCardSdk22UltCreditCardSdkEngine4initEPKc
      UltCreditCardSdkResult result = CheckResult("Init", UltCreditCardSdkEngine.init(BuildJSON(assetsFolder, tokenDataBase64)));

      // Decode the JPEG/PNG/BMP file
      final File file = new File(parameters.get("--image"));
      if (!file.exists())
      {
          throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
      }
      final BufferedImage image = ImageIO.read(file);
      final int bytesPerPixel = image.getColorModel().getPixelSize() >> 3;
      if (bytesPerPixel != 1 && bytesPerPixel != 3 && bytesPerPixel != 4)
      {
         throw new IOException("Invalid BPP: " + bytesPerPixel);
      }
      System.out.println("bytesPerPixel: " + bytesPerPixel + System.lineSeparator());

      // Write data to native/direct ByteBuffer
      final DataBuffer dataBuffer = image.getRaster().getDataBuffer();
      if (!(dataBuffer instanceof DataBufferByte)) {
         throw new IOException("Image must contains 1-byte samples");
      }
      final ByteBuffer nativeBuffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * bytesPerPixel);
      final byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
      nativeBuffer.put(pixelData);
      nativeBuffer.rewind();
      
      // Processing
      // For packed formats (RGB-family): https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html#_CPPv4N21ultimateCreditCardSdk22UltCreditCardSdkEngine7processEK23ULTCCARD_SDK_IMAGE_TYPEPKvK6size_tK6size_tK6size_tKi
      // For YUV formats (data from camera): https://www.doubango.org/SDKs/credit-card-ocr/docs/cpp-api.html#_CPPv4N21ultimateCreditCardSdk22UltCreditCardSdkEngine7processEK23ULTCCARD_SDK_IMAGE_TYPEPKvPKvPKvK6size_tK6size_tK6size_tK6size_tK6size_tK6size_tKi
      result = CheckResult("Process", UltCreditCardSdkEngine.process(
            (bytesPerPixel == 1) ? ULTCCARD_SDK_IMAGE_TYPE.ULTCCARD_SDK_IMAGE_TYPE_Y : (bytesPerPixel == 4 ? ULTCCARD_SDK_IMAGE_TYPE.ULTCCARD_SDK_IMAGE_TYPE_BGRA32 : ULTCCARD_SDK_IMAGE_TYPE.ULTCCARD_SDK_IMAGE_TYPE_BGR24),
            nativeBuffer,
            image.getWidth(),
            image.getHeight(),
            image.getWidth(), // stride
            getExifOrientation(file)
         ));
      // Print result to console
      System.out.println("Result: " + result.json() + System.lineSeparator());

       // Wait until user press a key
       System.out.println("Press any key to terminate !!" + System.lineSeparator());
       final java.util.Scanner scanner = new java.util.Scanner(System.in);
       if (scanner != null) {
         scanner.nextLine();
         scanner.close();
       }

       // Now that you're done, deInit the engine before exiting
       CheckResult("DeInit", UltCreditCardSdkEngine.deInit());
   }

   static int getExifOrientation(File file) throws IOException 
   {
      FileInputStream fin= new FileInputStream(file);
      FileChannel channel = fin.getChannel();

      // Check if it's JPEG
      final MappedByteBuffer codeBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, 2); // read 2 first bytes
      if (codeBuffer.asShortBuffer().get() != -40) { // -40 = 0xFFD8 in Short
         return 1;
      }
      
      // Read raw data and extract EXIF info
      final long fileSize = channel.size();
      final ByteBuffer buffer = ByteBuffer.allocateDirect((int) fileSize);
      channel.read(buffer);
      buffer.flip();

      channel.close();
      fin.close();

      return UltCreditCardSdkEngine.exifOrientation(buffer, buffer.remaining());
    }

   static Hashtable<String, String> ParseArgs(String[] args) throws IllegalArgumentException
   {
      System.out.println("Args: " + String.join(" ", args) + System.lineSeparator());

      if ((args.length & 1) != 0)
      {
            String errMessage = String.format("Number of args must be even: %d", args.length);
            System.err.println(errMessage);
            throw new IllegalArgumentException(errMessage);
      }

      // Parsing
      Hashtable<String, String> values = new Hashtable<String, String>();
      for (int index = 0; index < args.length; index += 2)
      {
            String key = args[index];
            if (!key.startsWith("--"))
            {
               String errMessage = String.format("Invalid key: %s", key);
               System.err.println(errMessage);
               throw new IllegalArgumentException(errMessage);
            }
            values.put(key, args[index + 1].replace("$(ProjectDir)", System.getProperty("user.dir").trim()));
      }
      return values;
   }

   static UltCreditCardSdkResult CheckResult(String functionName, UltCreditCardSdkResult result) throws IOException
   {
      if (!result.isOK())
      {
            String errMessage = String.format("%s: Execution failed: %s", functionName, result.json());
            System.err.println(errMessage);
            throw new IOException(errMessage);
      }
      return result;
   }

   // https://www.doubango.org/SDKs/credit-card-ocr/docs/Configuration_options.html
   static String BuildJSON(String assetsFolder, String tokenDataBase64)
   {
      return String.format(
         "{" +
         "\"debug_level\": \"%s\"," +
         "\"debug_write_input_image_enabled\": %s," +
         "\"debug_internal_data_path\": \"%s\"," +
         "" +
         "\"num_threads\": %d," +
         "\"gpgpu_enabled\": %s," +
         "\"ielcd_enabled\": %s," +
         "" +
         "\"detect_minscore\": %f," +
         "\"detect_roi\": [%s]," +
         "" +
         "\"recogn_minscore\": %f," +
         "\"recogn_score_type\": \"%s\"," + 
         "\"recogn_rectify_enabled\": \"%s\"," +
         "" +
         "\"assets_folder\": \"%s\"," +
         "\"license_token_data\": \"%s\"" +
         "" +
         "}"
         , 
         CONFIG_DEBUG_LEVEL,
         CONFIG_DEBUG_WRITE_INPUT_IMAGE ? "true" : "false",
         CONFIG_DEBUG_DEBUG_INTERNAL_DATA_PATH,

         CONFIG_NUM_THREADS,
         CONFIG_GPGPU_ENABLED ? "true" : "false",
         CONFIG_IELCD_ENABLED ? "true" : "false",

         CONFIG_DETECT_MINSCORE,
         CONFIG_DETECT_ROI.stream().map(String::valueOf).collect(Collectors.joining(",")),

         CONFIG_RECOGN_MINSCORE,
         CONFIG_RECOGN_SCORE_TYPE,
         CONFIG_RECOGN_RECTIFY_ENABLED ? "true" : "false",

         assetsFolder,
         tokenDataBase64
      );
   }
}