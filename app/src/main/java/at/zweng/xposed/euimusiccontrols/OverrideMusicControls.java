package at.zweng.xposed.euimusiccontrols;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;


//        ---------------------------------------------------------------------------
//        Our target class and methods:
//
//        as decompiled from /system/priv-app/SystemUI/oat/arm64/SystemUI.odex
//        on LeEco LePro3 (LEX720)
//        running: 5.8.018S (WAXCNFN5801811012S)
//        ---------------------------------------------------------------------------
//
//
//        package com.android.systemui.controlcenter.musicvideo;
//            ....
//
//        public class LeMusicLayoutExtend extends LinearLayout implements OnClickListener {
//            ....
//
//            private void handlePrevious() {
//                this.mContext.sendBroadcast(this.mPreviousIntent);
//            }
//
//            private void handleNext() {
//                this.mContext.sendBroadcast(this.mNextIntent);
//            }
//
//            private void handlePlayPause() {
//                this.mContext.sendBroadcast(this.mPlaypauseIntent);
//            }
//            ....
//        }
//


/**
 * Xposed Module "EUI MusicControls"
 * <p>
 * Created by johnzweng <john@zweng.at> on 15.11.2016
 */
public class OverrideMusicControls implements IXposedHookLoadPackage {

    private final static String TARGET_PACKAGE = "com.android.systemui";
    private final static String LOG_PREFIX = "EUI MusicControls: ";
    private final static String LOGCAT_TAG = "EUI_MusicControls";

    /**
     * Method hook, to be called instead of original "handlePlayPause" method.
     */
    protected static final XC_MethodHook handlePlayPauseHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            // get "mContext" from target class
            Field mContextField = findField(param.thisObject.getClass(), "mContext");
            Context mContext = (Context) mContextField.get(param.thisObject);
            // send emulated headset media button event:
            sendMediaButtonKeyDownAndUp(mContext, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            // prevent that original method gets called:
            param.setResult(null);
        }
    };

    /**
     * Method hook, to be called instead of original "handleNext" method.
     */
    protected static final XC_MethodHook handleNextHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            // get "mContext" from target class
            Field mContextField = findField(param.thisObject.getClass(), "mContext");
            Context mContext = (Context) mContextField.get(param.thisObject);
            // send emulated headset media button event:
            sendMediaButtonKeyDownAndUp(mContext, KeyEvent.KEYCODE_MEDIA_NEXT);
            // prevent that original method gets called:
            param.setResult(null);
        }
    };

    /**
     * Method hook, to be called instead of original "handlePrevious" method.
     */
    protected static final XC_MethodHook handlePrevHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            // get "mContext" from target class
            Field mContextField = findField(param.thisObject.getClass(), "mContext");
            Context mContext = (Context) mContextField.get(param.thisObject);
            // send emulated headset media button event:
            sendMediaButtonKeyDownAndUp(mContext, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            // prevent that original method gets called:
            param.setResult(null);
        }
    };


    /**
     * Send MEDIA_BUTTON broadcasts with given keycode
     */
    private static void sendMediaButtonKeyDownAndUp(Context ctx, int keyCode) {
        sendMediaBtnKeyDown(ctx, keyCode);
        sendMediaBtnKeyUp(ctx, keyCode);
    }

    /**
     * Send MEDIA_BUTTON broadcasts with given keycode
     */
    private static void sendMediaBtnKeyDown(Context ctx, int keyCode) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent k = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        i.putExtra(Intent.EXTRA_KEY_EVENT, k);
        Log.d(LOGCAT_TAG,
                "sending ordered broadcast MEDIA_BUTTON, ACTION_DOWN, keyCode: "
                        + keyCode);
        ctx.sendOrderedBroadcast(i, null);
    }

    /**
     * Send MEDIA_BUTTON broadcasts with given keycode
     */
    private static void sendMediaBtnKeyUp(Context ctx, int keyCode) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MEDIA_BUTTON);
        KeyEvent k = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        i.putExtra(Intent.EXTRA_KEY_EVENT, k);
        Log.d(LOGCAT_TAG,
                "sending ordered broadcast MEDIA_BUTTON, ACTION_UP, keyCode: "
                        + keyCode);
        ctx.sendOrderedBroadcast(i, null);
    }

    /**
     * Hook methods when target package is loaded.
     *
     * @param lpparam load package params
     * @throws Throwable
     */
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            // ignore loading of all other packages
            return;
        }
        log(LOG_PREFIX + "We are in " + TARGET_PACKAGE + " application. Will try to place method hooks for the music Play buttons in EUI Control Center. :)");


        //
        // 1) Locate our target class:
        //
        Class<?> leMusicLayoutExtend;
        try {
            leMusicLayoutExtend = findClass("com.android.systemui.controlcenter.musicvideo.LeMusicLayoutExtend",
                    lpparam.classLoader);
        } catch (ClassNotFoundError e) {
            XposedBridge
                    .log(LOG_PREFIX + "Could not find matching class 'LeMusicLayoutExtend' for hooking. Sorry, I cannot do anything. This module WILL NOT WORK! :-( :-(");
            // abort if class not found..
            return;
        }


        //
        // 2) Hook the 3 methods:
        //

        // handlePlayPause()
        try {
            Method handlePlayPause = findMethodExact(leMusicLayoutExtend, "handlePlayPause");
            XposedBridge.hookMethod(handlePlayPause, handlePlayPauseHook);
            XposedBridge
                    .log(LOG_PREFIX + "successfully replaced method handlePlayPause() :)");
        } catch (NoSuchMethodError nsme) {
            XposedBridge
                    .log(LOG_PREFIX + "Sorry, method handlePlayPause() was not found. Function of 'Play/Pause' button in ControlCenter will not be changed. :-(");
        }

        // handleNext()
        try {
            Method handleNext = findMethodExact(leMusicLayoutExtend, "handleNext");
            XposedBridge.hookMethod(handleNext, handleNextHook);
            XposedBridge
                    .log(LOG_PREFIX + "successfully replaced method handleNext() :)");
        } catch (NoSuchMethodError nsme) {
            XposedBridge
                    .log(LOG_PREFIX + "Sorry, method handleNext() was not found. Function of 'Next' button in EUI ControlCenter will not changed. :-(");
        }

        // handlePrevious()
        try {
            Method handlePrevious = findMethodExact(leMusicLayoutExtend, "handlePrevious");
            XposedBridge.hookMethod(handlePrevious, handlePrevHook);
            XposedBridge
                    .log(LOG_PREFIX + "successfully replaced method handlePrevious() :)");
        } catch (NoSuchMethodError nsme) {
            XposedBridge
                    .log(LOG_PREFIX + "Sorry, method handlePrevious() was not found. Function of 'Previous' button in EUI ControlCenter will not changed. :-(");
        }
    }

}
