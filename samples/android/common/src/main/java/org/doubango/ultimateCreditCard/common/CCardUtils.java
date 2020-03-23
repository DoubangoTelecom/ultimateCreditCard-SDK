/*
 * Copyright (C) 2016-2020 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateCreditCard-SDK
 * WebSite: https://www.doubango.org/webapps/credit-card-ocr/
 */
package org.doubango.ultimateCreditCard.common;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.PointF;
import android.util.Log;

import org.doubango.ultimateCreditCard.Sdk.UltCreditCardSdkResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class
 */
public class CCardUtils {
    static final String TAG = CCardUtils.class.getCanonicalName();
    /**
     * Translation and scaling information used to map image pixels to screen pixels.
     */
    public static class CCardTransformationInfo {
        final int mXOffset;
        final int mYOffset;
        final float mRatio;
        final int mWidth;
        final int mHeight;
        public CCardTransformationInfo(final int imageWidth, final int imageHeight, final int canvasWidth, final int canvasHeight) {
            final float xRatio = (float)canvasWidth / (float)imageWidth;
            final float yRatio =  (float)canvasHeight / (float)imageHeight;
            mRatio = Math.min( xRatio, yRatio );
            mWidth = (int)(imageWidth * mRatio);
            mHeight = (int)(imageHeight * mRatio);
            mXOffset = (canvasWidth - mWidth) >> 1;
            mYOffset = (canvasHeight - mHeight) >> 1;
        }
        public float transformX(final float x) { return x * mRatio + mXOffset; }
        public float transformY(final float y) { return y * mRatio + mYOffset; }
        public PointF transform(final PointF p) { return new PointF(transformX(p.x), transformY(p.y)); }
        public int getXOffset() { return mXOffset; }
        public int getYOffset() { return mYOffset; }
        public float getRatio() { return mRatio; }
        public int getWidth() { return mWidth; }
        public int getHeight() { return mHeight; }
    }

    /**
     * Credit card field
     */
    static class Field {
        private String mName;
        private String mValue;
        private float mConfidence;
        private boolean mValid;

        public String getName() { return mName; }
        public String getValue() { return mValue; }
        public float getConfidence() { return mConfidence; }
        public boolean isValid() { return mValid; }
    }

    /**
     * Credit card
     */
    static class Card {
        private boolean mNumberIsValid = false;
        private float mDetectionConfidence;
        private float mRecognitionMinConfidence = 100.f;
        private float mWarpedBox[];
        private List<Field> mFields;

        public boolean isNumberValid() { return mNumberIsValid; }
        public float getDetectionConfidence() { return mDetectionConfidence; }
        public float getRecognitionMinConfidence() { return mRecognitionMinConfidence; }
        public float[] getWarpedBox() { return mWarpedBox; }
        public List<Field> getFields() { return mFields; }
    }

    static public final long extractFrameId(final UltCreditCardSdkResult result) {
        final String jsonString = result.json();
        if (jsonString != null) {
            try {
                final JSONObject jObject = new JSONObject(jsonString);
                return jObject.getLong("frame_id");
            }
            catch (JSONException e) { }
        }
        return 0;
    }

    static public final List<Card> extractCards(final UltCreditCardSdkResult result) {
        final List<Card> cards = new LinkedList<>();
        if (!result.isOK() || result.numCards() == 0) {
            return cards;
        }
        final String jsonString = result.json();
        //final String jsonString = "{\"cards\":[{\"confidence\":100,\"description\":\"MasterCard card #1\",\"fields\":[{\"confidence\":89.30191802978516,\"name\":\"number\",\"valid\":true,\"value\":\"5391232061279498\"},{\"confidence\":88.74952697753906,\"name\":\"valid_thru\",\"value\":\"05/19\"},{\"confidence\":81.04048156738281,\"name\":\"holder_name\",\"value\":\"REVOLUT\"}],\"number_valid\":true,\"warpedBox\":[454.18408203125,138.2911376953125,997.2169799804688,138.2911376953125,997.2169799804688,516.2911376953125,454.18408203125,516.2911376953125]}],\"code\":200,\"duration\":380,\"orientation\":1,\"phrase\":\"OK\"}";
        if (jsonString == null) { // No card
            return cards;
        }

        try {
            final JSONObject jObject = new JSONObject(jsonString);
            if (jObject.has("cards") && !jObject.isNull("cards")) {
                final JSONArray jCards = jObject.getJSONArray("cards");
                for (int i = 0; i < jCards.length(); ++i) {
                    final JSONObject jCard = jCards.getJSONObject(i);
                    final JSONArray jWarpedBox = jCard.getJSONArray("warpedBox");
                    final Card card = new Card();
                    card.mFields = new LinkedList<>();
                    card.mWarpedBox = new float[8];
                    for (int j = 0; j < 8; ++j) {
                        card.mWarpedBox[j] = (float) jWarpedBox.getDouble(j);
                    }
                    card.mDetectionConfidence = (float) jCard.getDouble("confidence");
                    card.mRecognitionMinConfidence = 100.f;
                    card.mNumberIsValid = false;

                    if (jCard.has("fields") && !jCard.isNull("fields")) {
                        final JSONArray jFields = jCard.getJSONArray("fields");
                        for (int j = 0; j < jFields.length(); ++j) {
                            final JSONObject jField = jFields.getJSONObject(j);
                            final Field field = new Field();
                            field.mConfidence = (float) jField.getDouble("confidence");
                            field.mName = jField.getString("name");
                            field.mValue = jField.getString("value");
                            if (jField.has("valid")) {
                                field.mValid = jField.getBoolean("valid");
                            }

                            card.mRecognitionMinConfidence = Math.min(card.mRecognitionMinConfidence, field.mConfidence);
                            card.mNumberIsValid |= (field.mName.equals("number") && field.mValid);

                            card.mFields.add(field);
                        }
                    }

                    cards.add(card);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return cards;
    }

    /**
     * Checks if the returned result is success. An assertion will be raised if it's not the case.
     * In production you should catch the exception and perform the appropriate action.
     * @param result The result to check
     * @return The same result
     */
    static public final UltCreditCardSdkResult assertIsOk(final UltCreditCardSdkResult result) {
        if (!result.isOK()) {
            throw new AssertionError("Operation failed: " + result.phrase());
        }
        return result;
    }

    /**
     * Converts the result to String.
     * @param result
     * @return
     */
    static public final String resultToString(final UltCreditCardSdkResult result) {
        return "code: " + result.code() + ", phrase: " + result.phrase() + ", numCards: " + result.numCards() + ", json: " + result.json();
    }

    /**
     *
     * @param fileName
     * @return Must close the returned object
     */
    static public FileChannel readFileFromAssets(final AssetManager assets, final String fileName) {
        FileInputStream inputStream = null;
        try {
            AssetFileDescriptor fileDescriptor = assets.openFd(fileName);
            inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            return inputStream.getChannel();
            // To return DirectByteBuffer: fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return null;
        }
    }
}