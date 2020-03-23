/*
 * Copyright (C) 2016-2020 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateCreditCard-SDK
 * WebSite: https://www.doubango.org/webapps/credit-card-ocr/
 */
package org.doubango.ultimateCreditCard.common;

import android.media.Image;

import java.util.concurrent.atomic.AtomicInteger;

public class CCardImage {

    Image mImage;
    final AtomicInteger mRefCount;

    private CCardImage(final Image image) {
        assert image != null;
        mImage = image;
        mRefCount = new AtomicInteger(0);
    }

    public static CCardImage newInstance(final Image image) {
        return new CCardImage(image);
    }

    public final Image getImage() {
        assert mRefCount.intValue() >= 0;
        return mImage;
    }

    public CCardImage takeRef() {
        assert mRefCount.intValue() >= 0;
        if (mRefCount.intValue() < 0) {
            return null;
        }
        mRefCount.incrementAndGet();
        return this;
    }

    public void releaseRef() {
        assert mRefCount.intValue() >= 0;
        final int refCount = mRefCount.decrementAndGet();
        if (refCount <= 0) {
            mImage.close();
            mImage = null;
        }
    }

    @Override
    protected synchronized void finalize() {
        if (mImage != null && mRefCount.intValue() < 0) {
            mImage.close();
        }
    }
}