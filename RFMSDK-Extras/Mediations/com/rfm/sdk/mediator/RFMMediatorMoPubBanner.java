/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.rfm.sdk.ui.mediator.RFMCustomBannerListener;
import com.rfm.sdk.ui.mediator.RFMCustomBanner;

import java.util.Map;
import java.util.Set;

public class RFMMediatorMoPubBanner implements RFMCustomBanner {
    private Context mContext;
    private MoPubView mMoPubView;
    RFMCustomBannerListener mListener;
    private final String LOG_TAG = "RFMMediatorMoPubBanner";
    private static final String PARAM_AD_ID = "adUnitId";

    /**
     * Implementation for requesting Banner Ad from MoPub via RFM Custom event
     *
     * @param context
     * @param params , will have adUnitId, width, height and other parameters specified for Custom Event
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
                mListener.onAdFailed("Failed to request MoPub Ad, ad unit Id missing");
            }
        }
    }


    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(mMoPubView != null) {
            mMoPubView.setBannerAdListener(null);
            mMoPubView.destroy();
            mContext=null;
            Log.v(LOG_TAG, "Clean up Mopub banner");
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
    private boolean createBannerAd(Map<String, String> adParams) {
        printAdParams(adParams);
        String adUnitId = null;
        if(adParams != null) {
            adUnitId = adParams.get(PARAM_AD_ID);
        }

        if(adUnitId == null) {
            return false;
        }
        Log.v(LOG_TAG, "Requesting Banner from MoPub with adUnitId " + adUnitId);
        mMoPubView = new MoPubView(mContext);
        mMoPubView.setAdUnitId(adUnitId);
        setAdSize(adParams);
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        mMoPubView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView moPubView) {
                if (mListener != null) {
                    mListener.onAdLoaded(mMoPubView);
                }
            }

            @Override
            public void onBannerFailed(MoPubView moPubView, MoPubErrorCode moPubErrorCode) {
                if (mListener != null) {
                    mListener.onAdFailed(moPubErrorCode.toString());
                }
            }

            @Override
            public void onBannerClicked(MoPubView moPubView) {
                if (mListener != null) {
                    mListener.onAdClicked();
                }
            }

            @Override
            public void onBannerExpanded(MoPubView moPubView) {
                if (mListener != null) {
                    mListener.onAdExpanded();
                }
            }

            @Override
            public void onBannerCollapsed(MoPubView moPubView) {
                if (mListener != null) {
                    mListener.onAdCollapsed();
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        mMoPubView.loadAd();
    }

    /**
     * Utility method to set width and height of Ad
     *
     * @param params, RFM SDK will always include 'width' and 'height' in params
     *                width x height specified on RFMAdView will be available here
     */
    private void setAdSize(Map<String, String> params){
        float width = 320;
        float height = 50;
        if(params !=null) {
            try {
                if (params.containsKey(PARAM_WIDTH)) {
                    width = Float.parseFloat(params.get(PARAM_WIDTH));
                }
                if (params.containsKey(PARAM_HEIGHT)) {
                    height = Float.parseFloat(params.get(PARAM_HEIGHT));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(LOG_TAG, "Failed to get valid size params from RFM SDK, requesting ad with default banner size 320 x 50");
                width = 320;
                height=50;
            }
        }
        try {
            int layoutWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, mContext.getResources().getDisplayMetrics());
            int layoutHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, mContext.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
            mMoPubView.setLayoutParams(lParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
