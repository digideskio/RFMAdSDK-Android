/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdSize;
import com.rfm.sdk.ui.mediator.RFMCustomBannerListener;
import com.rfm.sdk.ui.mediator.RFMCustomBanner;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import java.util.Map;
import java.util.Set;

public class RFMMediatorFBANBanner implements RFMCustomBanner {
    Context mContext;
    RFMCustomBannerListener mListener;
    private AdView mBannerAd;
    private static final String LOG_TAG = "RFMMediatorFBANBanner";
    private static final String PARAM_AD_ID = "placementId";

    /**
     * Implementation for requesting Banner Ad from FBAN via RFM Custom event
     *
     * @param context
     * @param params , will have placementId, width, height and other parameters specified for Custom Event
     * @param listener
     */
    @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomBannerListener listener) {
        mContext = context;
        mListener = listener;
        if(createBannerAd(params)) {
            createAdListener();
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request FBAN Banner, placement Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(mBannerAd != null) {
            mBannerAd.destroy();
            mBannerAd.setAdListener(null);
            mContext=null;
            Log.v(LOG_TAG, "Clean up FBAN Banner");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        return false;
    }

    /**
     * Utility method to create Banner Ad
     *
     * @param params
     * @return true for success / false for failure
     */
    protected boolean createBannerAd(Map<String, String> params) {
        printAdParams(params);
        String placementId = null;
        if(params != null) {
            placementId = params.get(PARAM_AD_ID);
        }
        Log.v(LOG_TAG, "Requesting Ad from FBAN with placement Id " + placementId);
        if(placementId == null) {
            //return false;
            placementId = "116331455112023";
        }

        mBannerAd = new AdView(mContext, placementId, getAdSize(params));
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        mBannerAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(LOG_TAG, "FB Banner Ad failed");
                if (mListener != null) {
                    mListener.onAdFailed("FB Banner failed with error code " + adError.getErrorCode()+ " and message "+adError.getErrorMessage());
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(LOG_TAG, "FB Banner Ad loaded");
                if(mListener != null) {
                    mListener.onAdLoaded(mBannerAd);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(LOG_TAG, "FB Banner Ad clicked");
                if(mListener != null) {
                    mListener.onAdClicked();
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        mBannerAd.loadAd();
    }

    /**
     * Utility method to set width and height of Ad
     *
     * @param params, RFM SDK will always include 'width' and 'height' in params
     *                width x height specified on RFMAdView will be available here
     * @return AdSize
     */
    protected AdSize getAdSize(Map<String, String> params) {
        AdSize adSize = AdSize.BANNER_320_50;
        if(params ==null) {
            return adSize;
        }
        try {
            float adWidth=320;
            float adHeight=50;
            if (params.containsKey(PARAM_WIDTH)) {
                adWidth = Float.parseFloat(params.get(PARAM_WIDTH));
            }
            if (params.containsKey(PARAM_HEIGHT)) {
                adHeight = Float.parseFloat(params.get(PARAM_HEIGHT));
            }
            adSize = AdSize.fromWidthAndHeight((int)adWidth, (int)adHeight);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(LOG_TAG, "Failed to get valid size params from RFM SDK, requesting ad with default banner size 320 x 50");
            adSize = AdSize.BANNER_320_50;
        }
        return adSize;
    }

    /**
     * Utility method to print all the parameters sent from RFM SDK
     * @param params
     */
    protected void printAdParams(Map<String, String> params) {
        if(params!= null) {
            Log.v(LOG_TAG, "Ad params from RFM Server");
            Set<String> keys = params.keySet();
            for(String keyStr:keys) {
                Log.v(LOG_TAG, "Key:"+keyStr+" | Value:"+params.get(keyStr));
            }
        } else {
            Log.v(LOG_TAG, "No additional Ad params available from RFM Server");
        }
    }
}
