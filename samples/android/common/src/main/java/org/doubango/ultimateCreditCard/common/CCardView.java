/*
 * Copyright (C) 2016-2020Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateCreditCard-SDK
 * WebSite: https://www.doubango.org/webapps/credit-card-ocr/
 */
package org.doubango.ultimateCreditCard.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import org.doubango.ultimateCreditCard.Sdk.UltCreditCardSdkResult;

import java.util.List;

public class CCardView extends View {

    static final String TAG = CCardView.class.getCanonicalName();

    static final float TEXT_NUMBER_SIZE_DIP = 12;
    static final float TEXT_CONFIDENCE_SIZE_DIP = 10;
    static final float TEXT_PROCESSING_TIME_SIZE_DIP = 10;
    static final int STROKE_WIDTH_DIP = 3;

    static final float MIN_CONFIDENCE_TO_NOMINATE_AS_OK = 70.f; // 70%

    private final Paint mPaintText;
    private final Paint mPaintTextBackground;
    private final Paint mPaintTextConfidence;
    private final Paint mPaintTextConfidenceBackground;
    private final Paint mPaintBorder;
    private final Paint mPaintTextProcessingTime;
    private final Paint mPaintTextProcessingTimeBackground;
    private final Paint mPaintDetectROI;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    long mProcessingTimeMillis;

    private Size mImageSize;
    private List<CCardUtils.Card> mCards = null;
    private RectF mDetectROI;

    /**
     *
     * @param context
     * @param attrs
     */
    public CCardView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final Typeface fontOCRB = Typeface.createFromAsset(context.getAssets(), "OCR-A Regular.ttf");
        final float strokeWidthInPixel = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH_DIP, getResources().getDisplayMetrics());

        mPaintText = new Paint();
        mPaintText.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_NUMBER_SIZE_DIP, getResources().getDisplayMetrics()));
        mPaintText.setColor(Color.BLACK);
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintText.setTypeface(Typeface.create(fontOCRB, Typeface.BOLD));

        mPaintTextBackground = new Paint();
        mPaintTextBackground.setColor(Color.YELLOW);
        mPaintTextBackground.setStrokeWidth(strokeWidthInPixel);
        mPaintTextBackground.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaintTextConfidence = new Paint();
        mPaintTextConfidence.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_CONFIDENCE_SIZE_DIP, getResources().getDisplayMetrics()));
        mPaintTextConfidence.setColor(Color.BLUE);
        mPaintTextConfidence.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextConfidence.setTypeface(Typeface.create(fontOCRB, Typeface.BOLD));

        mPaintTextConfidenceBackground = new Paint();
        mPaintTextConfidenceBackground.setColor(Color.YELLOW);
        mPaintTextConfidenceBackground.setStrokeWidth(strokeWidthInPixel);
        mPaintTextConfidenceBackground.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaintBorder = new Paint();
        mPaintBorder.setStrokeWidth(strokeWidthInPixel);
        mPaintBorder.setPathEffect(null);
        mPaintBorder.setColor(Color.YELLOW);
        mPaintBorder.setStyle(Paint.Style.STROKE);

        mPaintTextProcessingTime = new Paint();
        mPaintTextProcessingTime.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_PROCESSING_TIME_SIZE_DIP, getResources().getDisplayMetrics()));
        mPaintTextProcessingTime.setColor(Color.BLACK);
        mPaintTextProcessingTime.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextProcessingTime.setTypeface(Typeface.create(fontOCRB, Typeface.BOLD));

        mPaintTextProcessingTimeBackground = new Paint();
        mPaintTextProcessingTimeBackground.setColor(Color.WHITE);
        mPaintTextProcessingTimeBackground.setStrokeWidth(strokeWidthInPixel);
        mPaintTextProcessingTimeBackground.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaintDetectROI = new Paint();
        mPaintDetectROI.setColor(Color.RED);
        mPaintDetectROI.setStrokeWidth(strokeWidthInPixel);
        mPaintDetectROI.setStyle(Paint.Style.STROKE);
        mPaintDetectROI.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
    }

    public void setDetectROI(final RectF roi) { mDetectROI = roi; }

    /**
     *
     * @param width
     * @param height
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    /**
     *
     * @param result
     * @param imageSize
     * @param processingTimeMillis
     */
    public synchronized void setResult(@NonNull final UltCreditCardSdkResult result, @NonNull final Size imageSize, final long processingTimeMillis) {
        mCards = CCardUtils.extractCards(result);
        mImageSize = imageSize;
        mProcessingTimeMillis = processingTimeMillis;
        postInvalidate();
    }

    @Override
    public synchronized void draw(final Canvas canvas) {
        super.draw(canvas);

        if (mImageSize == null) {
            Log.i(TAG, "Not initialized yet");
            return;
        }

        // Total processing time (Inference, format conversion, thresholding, binarization...)
        final String mInferenceTimeMillisString = "Total processing time: " + mProcessingTimeMillis;
        Rect boundsTextProcessingTimeMillis = new Rect();
        mPaintTextProcessingTime.getTextBounds(mInferenceTimeMillisString, 0, mInferenceTimeMillisString.length(), boundsTextProcessingTimeMillis);
        canvas.drawRect(0, 0, boundsTextProcessingTimeMillis.width(), boundsTextProcessingTimeMillis.height(), mPaintTextProcessingTimeBackground);
        canvas.drawText(mInferenceTimeMillisString, 0, boundsTextProcessingTimeMillis.height(), mPaintTextProcessingTime);

        // Transformation info
        final CCardUtils.CCardTransformationInfo tInfo = new CCardUtils.CCardTransformationInfo(mImageSize.getWidth(), mImageSize.getHeight(), getWidth(), getHeight());

        // ROI
        if (mDetectROI != null && !mDetectROI.isEmpty()) {
            canvas.drawRect(
                    new RectF(
                            tInfo.transformX(mDetectROI.left),
                            tInfo.transformY(mDetectROI.top),
                            tInfo.transformX(mDetectROI.right),
                            tInfo.transformY(mDetectROI.bottom)
                    ),
                    mPaintDetectROI
            );
        }

        // Zones
        if (mCards != null && !mCards.isEmpty()) {

            // We want the stoke to be outside of the text and this is why we use half-stroke width offset
            final float borderWidth = (mPaintBorder.getStrokeWidth() * 0.5f) * 3.f; // times 3.f to have nice visual effect

            for (final CCardUtils.Card card : mCards) {

                // Green if number is ok, orange if looks ok, otherwise red
                final int color = card.isNumberValid() ? Color.GREEN : (card.getRecognitionMinConfidence() >= MIN_CONFIDENCE_TO_NOMINATE_AS_OK ? Color.rgb(255,165,0) :  Color.RED);
                mPaintBorder.setColor(color);
                mPaintTextBackground.setColor(color);
                mPaintTextConfidenceBackground.setColor(color);

                // Transform corners
                final float[] warpedBox = card.getWarpedBox();
                final PointF cornerA = new PointF(tInfo.transformX(warpedBox[0]) - borderWidth, tInfo.transformY(warpedBox[1]) - borderWidth);
                final PointF cornerB = new PointF(tInfo.transformX(warpedBox[2]) + borderWidth, tInfo.transformY(warpedBox[3]) - borderWidth);
                final PointF cornerC = new PointF(tInfo.transformX(warpedBox[4]) + borderWidth, tInfo.transformY(warpedBox[5]) + borderWidth);
                final PointF cornerD = new PointF(tInfo.transformX(warpedBox[6]) - borderWidth, tInfo.transformY(warpedBox[7]) + borderWidth);
                // Draw border
                final Path pathBorder = new Path();
                pathBorder.moveTo(cornerA.x, cornerA.y);
                pathBorder.lineTo(cornerB.x, cornerB.y);
                pathBorder.lineTo(cornerC.x, cornerC.y);
                pathBorder.lineTo(cornerD.x, cornerD.y);
                pathBorder.lineTo(cornerA.x, cornerA.y);
                pathBorder.close();
                canvas.drawPath(pathBorder, mPaintBorder);

                // Draw texts
                PointF cornerA_line = cornerA;
                PointF cornerB_line = cornerB;
                for (int i = card.getFields().size() - 1; i >= 0; --i) {
                    final CCardUtils.Field field = card.getFields().get(i);

                    // Text
                    final String text = field.getValue();
                    Rect boundsText = new Rect();
                    mPaintText.getTextBounds(text, 0, text.length(), boundsText);
                    final RectF rectText = new RectF(
                            cornerA_line.x,
                            cornerA_line.y - boundsText.height(),
                            cornerA_line.x + boundsText.width(),
                            cornerA_line.y
                    );
                    final Path pathText = new Path();
                    pathText.moveTo(cornerA_line.x, cornerA_line.y);
                    pathText.lineTo(cornerA_line.x + boundsText.width(), cornerB_line.y);
                    pathText.addRect(rectText, Path.Direction.CCW);
                    pathText.close();
                    canvas.drawPath(pathText, mPaintTextBackground);
                    canvas.drawTextOnPath(text, pathText, 0, 0, mPaintText);

                    // Move to the next line
                    cornerA_line.y -= (boundsText.height() * 1.3f) + mPaintText.getStrokeWidth();
                    cornerB_line.y -= (boundsText.height() * 1.3f) + mPaintText.getStrokeWidth();
                }

                // Draw text confidence
                String confidence = "";
                for (final CCardUtils.Field field : card.getFields()) {
                    confidence += (confidence.isEmpty() ? "" : " - ") + String.format("%.2f%%", field.getConfidence());
                }
                Rect boundsTextConfidence = new Rect();
                mPaintTextConfidence.getTextBounds(confidence, 0, confidence.length(), boundsTextConfidence);
                final RectF rectTextConfidence = new RectF(
                        cornerD.x,
                        cornerD.y,
                        cornerD.x + boundsTextConfidence.width(),
                        cornerD.y + boundsTextConfidence.height()
                );
                final Path pathTextConfidence = new Path();
                final double dx = cornerC.x - cornerD.x;
                final double dy = cornerC.y - cornerD.y;
                final double angle = Math.atan2(dy, dx);
                final double cosT = Math.cos(angle);
                final double sinT = Math.sin(angle);
                final float Cx = cornerD.x + rectTextConfidence.width();
                final float Cy = cornerC.y;
                final PointF cornerCC = new PointF((float)(Cx * cosT - Cy * sinT), (float)(Cy * cosT + Cx * sinT));
                final PointF cornerDD = new PointF((float)(cornerD.x * cosT - cornerD.y * sinT), (float)(cornerD.y * cosT + cornerD.x * sinT));
                pathTextConfidence.moveTo(cornerDD.x, cornerDD.y + boundsTextConfidence.height());
                pathTextConfidence.lineTo(cornerCC.x, cornerCC.y + boundsTextConfidence.height());
                pathTextConfidence.addRect(rectTextConfidence, Path.Direction.CCW);
                pathTextConfidence.close();
                canvas.drawPath(pathTextConfidence, mPaintTextConfidenceBackground);
                canvas.drawTextOnPath(confidence, pathTextConfidence, 0, 0, mPaintTextConfidence);
            }
        }
    }
}