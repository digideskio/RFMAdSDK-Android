/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.rfm.sdk.ui.mediator.RFMCustomBannerListener;
import com.rfm.sdk.ui.mediator.RFMCustomBanner;

import java.util.Map;
import java.util.Set;

public class RFMMediatorAdmDFPBanner implements RFMCustomBanner {
    Context mContext;
    RFMCustomBannerListener mListener;
    private PublisherAdView mPubAdView;
    private static final String LOG_TAG = "RFMMediatorAdmDFPBanner";
    private static final String PARAM_AD_ID = "adUnitId";
    private AdView mBannerAd;

    /**
     * Implementation for requesting Banner Ad from Admob via RFM Custom event
     *
     * @param context
     * @param params , will have adUnitId, width, height and other parameters specified for Custom Event
     * @param listener
     */
    @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomBannerListener listener) {

        mContext = context;
        mListener = listener;
        // Create a Banner Ad
        if(createBannerAd(params)) {
            // Add a listener for callbacks
            createAdListener();
            // request Ad
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request DFP Ad, site Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(mPubAdView != null) {
            mPubAdView.setAdListener(null);
            mPubAdView.destroy();
            mPubAdView = null;
            mContext = null;
            Log.v(LOG_TAG, "Clean up Admob DFP Banner");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        return true;
    }

    /**
     * Utility method to create Banner Ad
     *
     * @param adParams
     * @return true for success / false for failure
     */
    protected boolean createBannerAd(Map<String, String> adParams)  {
        String adUnitId = null;
        printAdParams(adParams);
        if(adParams != null) {
            adUnitId = adParams.get(PARAM_AD_ID);
        }

        Log.v(LOG_TAG, "Request Ad from DFP with Ad Unit Id " + adUnitId);
        if(adUnitId == null) {
            return false;
        }
        mPubAdView = new PublisherAdView(mContext);
        mPubAdView.setAdUnitId(adUnitId);
        mPubAdView.setAdSizes(getAdSize(adParams));
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        mPubAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(LOG_TAG, "DFP Banner Ad loaded from RFMSDK");
                if (mListener != null) {
                    mListener.onAdLoaded(mPubAdView);
                }
            }

            /** Called when an ad failed to load. */
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(LOG_TAG, "DFP Banner Ad failed from RFMSDK");
                if (mListener != null) {
                    mListener.onAdFailed("DFP Banner failed with error code " + errorCode);
                }
            }

            /**
             * Called when an Activity is created in front of the app (e.g. an interstitial is shown, or an
             * ad is clicked and launches a new Activity).
             */
            @Override
            public void onAdOpened() {
                Log.d(LOG_TAG, "DFP Banner Ad Opened");
                if (mListener != null) {
                    mListener.onAdExpanded();
                }
            }

            /** Called when an ad is closed and about to return to the application. */
            @Override
            public void onAdClosed() {
                Log.d(LOG_TAG, "DFP Banner Ad Closed ");
                if (mListener != null) {
                    mListener.onAdCollapsed();
                }
            }

            /**
             * Called when an ad is clicked and going to start a new Activity that will leave the
             * application (e.g. breaking out to the Browser or Maps application).
             */
            @Override
            public void onAdLeftApplication() {
                Log.d(LOG_TAG, "DFP Banner Ad left Application");
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    private void loadAd() {
        PublisherAdRequest adRequest =
                new PublisherAdRequest.Builder().build();
        if(mPubAdView != null) {
            mPubAdView.loadAd(adRequest);
        }
    }

    /**
     * Utility method to set width and height of Ad
     *
     * @param params, RFM SDK will always include 'width' and 'height' in params
     *                width x height specified on RFMAdView will be available here
     * @return AdSize
     */
    protected AdSize getAdSize(Map<String, String> params) {
        AdSize adSize = AdSize.BANNER;
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
            adSize = new AdSize((int)adWidth,  (int)adHeight);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(LOG_TAG, "Failed to get valid size params from RFM SDK, requesting ad with default banner size 320 x 50");
            adSize = AdSize.BANNER;
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
