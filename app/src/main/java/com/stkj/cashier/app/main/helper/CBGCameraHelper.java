package com.stkj.cashier.app.main.helper;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.stkj.cashier.App;

import com.stkj.cashier.app.main.model.PauseFacePassDetect;
import com.stkj.cashier.app.main.model.ResumeFacePassDetect;
import com.stkj.cashier.app.mode.AmountFragment;
import com.stkj.cashier.cbgfacepass.CBGFacePassHandlerHelper;
import com.stkj.cashier.common.camera.CameraHelper;
import com.stkj.cashier.common.core.ActivityHolderFactory;
import com.stkj.cashier.common.core.ActivityWeakRefHolder;
import com.stkj.cashier.common.permissions.callback.PermissionCallback;
import com.stkj.cashier.common.permissions.request.CameraPermissionRequest;
import com.stkj.cashier.common.utils.EventBusUtils;
import com.stkj.cashier.deviceinterface.DeviceManager;
import com.stkj.cashier.permission.AppPermissionHelper;
import com.stkj.cashier.utils.util.ToastUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mcv.facepass.FacePassException;
import mcv.facepass.types.FacePassImage;
import mcv.facepass.types.FacePassImageType;

/**
 * 旷视人脸检测帮助类
 */
public class CBGCameraHelper extends ActivityWeakRefHolder {

    public final static String TAG = "CBGCameraHelper";
    private SurfaceView irPreview;
    private SurfaceView preview;
    private CameraHelper cameraHelper;
    private CameraHelper irCameraHelper;
    private CBGFacePassHandlerHelper facePassHandlerHelper;
    private CBGFacePassHandlerHelper.OnDetectFaceListener onDetectFaceListener;
    private boolean isFaceDualCamera;
    private long beforeTime = 0;
    private AmountFragment amountFragment;
    private int width = 0;
    private int height = 0;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public CBGCameraHelper(@NonNull Activity activity) {
        super(activity);
        EventBusUtils.registerEventBus(this);
    }

    public void setOnDetectFaceListener(CBGFacePassHandlerHelper.OnDetectFaceListener onDetectFaceListener) {
        this.onDetectFaceListener = onDetectFaceListener;
    }

    public void setPreviewView(SurfaceView surfaceView, SurfaceView irPreview, boolean isFaceDualCamera) {
        Activity activityWithCheck = getHolderActivityWithCheck();
        isFaceDualCamera = false;
        Log.i(TAG, "limeopenCamera activityWithCheck 56: " + (activityWithCheck == null));
        if (activityWithCheck == null) {
            return;
        }
        this.preview = surfaceView;
        this.irPreview = irPreview;
        this.isFaceDualCamera = isFaceDualCamera;
        AppPermissionHelper.with((FragmentActivity) activityWithCheck)
                .requestPermission(new CameraPermissionRequest(), new PermissionCallback() {
                    @Override
                    public void onGranted() {
                        facePassHandlerHelper = (CBGFacePassHandlerHelper) ActivityHolderFactory.get(CBGFacePassHandlerHelper.class, activityWithCheck);
                    }
                });
    }

    /**
     * 开始人脸检测
     */
    public void prepareFacePassDetect() {
        try {
            Activity activityWithCheck = getHolderActivityWithCheck();
            if (activityWithCheck == null) {
                return;
            }
            if (this.preview == null) {
                return;
            }

            int cameraDisplayOrientation = DeviceManager.getInstance().getCameraDisplayOrientation();
            if (cameraHelper == null) {
                cameraHelper = new CameraHelper(activityWithCheck);
                int cameraId = DeviceManager.getInstance().getBackCameraId();
                cameraHelper.setCameraId(cameraId);
                cameraHelper.setDisplayOrientation(cameraDisplayOrientation);
            }

            int irCameraDisplayOrientation = DeviceManager.getInstance().getIRCameraDisplayOrientation();
            if (irCameraHelper == null && isFaceDualCamera) {
                irCameraHelper = new CameraHelper(activityWithCheck);
                int irCameraId = DeviceManager.getInstance().getIRCameraId();
                irCameraHelper.setCameraId(irCameraId);
                irCameraHelper.setDisplayOrientation(irCameraDisplayOrientation);
            }

            if (cameraHelper != null) {
                if (cameraHelper.hasPreviewView()) {
                    cameraHelper.startPreview();
                } else {
                    cameraHelper.setNeedPreviewCallBack(true);
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                    cameraHelper.setCameraHelperCallback(new CameraHelper.OnCameraHelperCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera, int displayOrientation, int previewOrientation) {
                            try {
                                if (amountFragment != null && (!amountFragment.mIsPayingValue() && !amountFragment.isRefund())) {
                                    return;
                                }

                                if (System.currentTimeMillis() - beforeTime < 200){
                                  return;
                                }

                                beforeTime = System.currentTimeMillis();
                                Log.d(TAG, "limeonPreviewFrame : " + 123);


                                if (width == 0 || height == 0){
                                    Camera.Parameters parameters = camera.getParameters();
                                     width = parameters.getPreviewSize().width;
                                     height = parameters.getPreviewSize().height;
                                     App.setWidth(width);
                                     App.setHeight(height);
                                Log.d(TAG, "limeonPreviewFramewidth: " + width + "   height: " + height);
                                }


                                        int orientation = DeviceManager.getInstance().needUseCameraPreviewOrientation() ? previewOrientation : displayOrientation;
                                        FacePassImage facePassImage = null;
                                        try {
                                            facePassImage = new FacePassImage(data, width, height, orientation, FacePassImageType.NV21);
                                        } catch (FacePassException e) {
                                            throw new RuntimeException(e);
                                        }
                                        //runUIThreadWithCheck(() -> {
                                        if (isFaceDualCamera) {

                                            if (facePassHandlerHelper.detectionResult == null ||  facePassHandlerHelper.detectionResult.faceList == null || facePassHandlerHelper.detectionResult.faceList.length < 1) {
                                                Log.i(TAG, "limeonPreviewFrame : " + 131);
                                                facePassHandlerHelper.addRgbFrame(facePassImage);
                                            }
                                        } else {
                                                Log.d(TAG, "limeonPreviewFrame : " + 161);
                                                facePassHandlerHelper.addFeedFrame(facePassImage);

                                        }
                                        //});




                            } catch (Exception e) {
                                Log.e(TAG, "limeonPreviewFrame Error processing preview frame: " + e.getMessage());
                            }
                        }
                    });
                        }
                    });

                    cameraHelper.prepare(preview);
                }
            }

            if (facePassHandlerHelper != null) {
                facePassHandlerHelper.setOnDetectFaceListener(onDetectFaceListener);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error preparing face pass detect: " + e.getMessage());
        }
    }

//    public void prepareFacePassDetect() {
//        try {
//            Log.i(TAG, "limeopenCamera prepareFacePassDetect 76");
//            Activity activityWithCheck = getHolderActivityWithCheck();
//            if (activityWithCheck == null) {
//                return;
//            }
//            if (this.preview == null) {
//                return;
//            }
//            Log.i(TAG, "limeopenCamera prepareFacePassDetect 84");
//            int cameraDisplayOrientation = DeviceManager.getInstance().getCameraDisplayOrientation();
//            if (cameraHelper == null) {
//                cameraHelper = new CameraHelper(activityWithCheck);
//                int cameraId = DeviceManager.getInstance().getBackCameraId();
//                cameraHelper.setCameraId(cameraId);
//                cameraHelper.setDisplayOrientation(cameraDisplayOrientation);
//            }
//            Log.i(TAG, "limeopenCamera prepareFacePassDetect 92");
//            int irCameraDisplayOrientation = DeviceManager.getInstance().getIRCameraDisplayOrientation();
//            if (irCameraHelper == null && isFaceDualCamera) {
//                irCameraHelper = new CameraHelper(activityWithCheck);
//                int irCameraId = DeviceManager.getInstance().getIRCameraId();
//                cameraHelper.setCameraId(irCameraId);
//                irCameraHelper.setDisplayOrientation(irCameraDisplayOrientation);
//            }
//            Log.i(TAG, "limeopenCamera prepareFacePassDetect 100");
//            if (cameraHelper != null) {
//                Log.i(TAG, "limeopenCamera prepareFacePassDetect 102");
//                if (cameraHelper.hasPreviewView()) {
//            cameraHelper.startPreview();
//                    Log.i(TAG, "limeopenCamera prepareFacePassDetect 106");
//                } else {
//                    Log.i(TAG, "limeopenCamera prepareFacePassDetect 108");
//                    cameraHelper.setNeedPreviewCallBack(true);
//                    Log.i(TAG, "limeopenCamera prepareFacePassDetect 110");
//                    cameraHelper.setCameraHelperCallback(new CameraHelper.OnCameraHelperCallback() {
//                        @Override
//                        public void onPreviewFrame(byte[] data, Camera camera, int displayOrientation, int previewOrientation) {
//                            try {
//                                Log.i(TAG, "limeopenCamera prepareFacePassDetect 114");
//                                if (amountFragment != null && !amountFragment.mIsPayingValue()){
//                                    return;
//                                }
////                                if (!facePassHandlerHelper.isStartFrameDetectTask()) {
////                                    return;
////                                }
//                                Log.i(TAG, "limeopenCamera prepareFacePassDetect 117");
//                                Camera.Parameters parameters = camera.getParameters();
//                                int width = parameters.getPreviewSize().width;
//                                int height = parameters.getPreviewSize().height;
//                                int orientation = DeviceManager.getInstance().needUseCameraPreviewOrientation() ? previewOrientation : displayOrientation;
//                                FacePassImage facePassImage = new FacePassImage(data, width, height, orientation, FacePassImageType.NV21);
//                                runUIThreadWithCheck(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (isFaceDualCamera) {
//                                            facePassHandlerHelper.addRgbFrame(facePassImage);
//                                        } else {
//                                            facePassHandlerHelper.addFeedFrame(facePassImage);
//                                        }
//                                    }
//                                });
//                            } catch (Throwable e) {
//                                Log.e(TAG, "limeopenCamera openCameraById 134: " + e.getMessage());
//                            }
//                        }
//                    });
//                    Log.i(TAG, "limeopenCamera prepareFacePassDetect 140");
//                    cameraHelper.prepare(preview);
//                    Log.i(TAG, "limeopenCamera prepareFacePassDetect 142");
//                }
//            }
//            Log.i(TAG, "limeopenCamera prepareFacePassDetect 145");
//            //双目识别
//            if (irPreview != null && irCameraHelper != null) {
//                Log.i(TAG, "limeopenCamera prepareFacePassDetect 145");
//                if (irCameraHelper.hasPreviewView()) {
////                    irCameraHelper.startPreview();
//                } else {
//                    irCameraHelper.setNeedPreviewCallBack(true);
//                    irCameraHelper.setCameraHelperCallback(new CameraHelper.OnCameraHelperCallback() {
//                        @Override
//                        public void onPreviewFrame(byte[] data, Camera camera, int displayOrientation, int previewOrientation) {
//                            try {
//                                if (!facePassHandlerHelper.isStartFrameDetectTask()) {
//                                    return;
//                                }
//                                Camera.Parameters parameters = camera.getParameters();
//                                int width = parameters.getPreviewSize().width;
//                                int height = parameters.getPreviewSize().height;
//                                int orientation = DeviceManager.getInstance().needUseIRCameraPreviewOrientation() ? previewOrientation : displayOrientation;
//                                FacePassImage facePassImage = new FacePassImage(data, width, height, orientation, FacePassImageType.NV21);
//                                runUIThreadWithCheck(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        facePassHandlerHelper.addIRFrame(facePassImage);
//                                    }
//                                });
//                            } catch (Throwable e) {
//                                Log.e(TAG, "limeopenCamera openCameraById 169: " + e.getMessage());
//                            }
//                        }
//                    });
//                    irCameraHelper.prepare(irPreview, true);
//                }
//            }
//            //人脸识别回调
//            if (facePassHandlerHelper != null) {
//                facePassHandlerHelper.setOnDetectFaceListener(onDetectFaceListener);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "limeopenCamera openCameraById 181: " + e.getMessage());
//        }
//
//
//    }

    private boolean needResumeFacePassDetect;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPauseFacePassDetect(PauseFacePassDetect eventBus) {
        if (facePassHandlerHelper != null && facePassHandlerHelper.isStartFrameDetectTask()) {
            needResumeFacePassDetect = true;
            stopFacePassDetect();
            ToastUtils.showLong("人脸检测功能已停止");
        } else {
            needResumeFacePassDetect = false;
        }
    }

    public AmountFragment getAmountFragment() {
        return amountFragment;
    }

    public void setAmountFragment(AmountFragment amountFragment) {
        this.amountFragment = amountFragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResumeFacePassDetect(ResumeFacePassDetect eventBus) {
        if (needResumeFacePassDetect) {
            startFacePassDetect();
            ToastUtils.showLong("人脸检测功能已恢复");
        }
        needResumeFacePassDetect = false;
    }

    /**
     * 开启人脸检测
     */
    public void startFacePassDetect() {
        if (facePassHandlerHelper != null) {
            facePassHandlerHelper.clearQueue();
            facePassHandlerHelper.startFeedFrameDetectTask();
            facePassHandlerHelper.startRecognizeFrameTask();
        }
    }

    /**
     * 停止人脸检测
     */
    public void stopFacePassDetect() {
        if (facePassHandlerHelper != null) {
            facePassHandlerHelper.stopFeedFrameDetectTask();
            facePassHandlerHelper.stopRecognizeFrameTask();
            facePassHandlerHelper.resetHandler();
        }
    }

    @Override
    public void onClear() {
        EventBusUtils.unRegisterEventBus(this);
        stopFacePassDetect();
    }

    /**
     * 释放相机
     */
    public void releaseCameraHelper() {
        stopFacePassDetect();
        //人脸识别回调
        if (facePassHandlerHelper != null) {
            facePassHandlerHelper.setOnDetectFaceListener(null);
        }
        if (cameraHelper != null) {
            cameraHelper.onClear();
            cameraHelper = null;
        }
        if (irCameraHelper != null) {
            irCameraHelper.onClear();
            irCameraHelper = null;
        }
    }
}
