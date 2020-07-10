package com.chinacreator;

import android.graphics.Bitmap;
import android.util.Base64;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.mineki.CardReaders.IDCardInfo;
import cn.mineki.CardReaders.IUsbReaderCallback;
import cn.mineki.CardReaders.UsbReader;
import cn.mineki.Utils.CertImageUtils;
import cn.mineki.Utils.Logs;

/**
 * This class echoes a string called from JavaScript.
 */
public class LondenValidator extends CordovaPlugin {
    // 是否初始化成功
    private static boolean initReaderSuccess = false;
    // USB是否连接的状态
    private static boolean usbConnect = false;
    // 正在读取中
    private static boolean isReading = false;
    // 读卡器对象
    private UsbReader idReader = null;
    // 卡信息对象
    private IDCardInfo idCardInfo = null;
    // 授权信息
    private byte[] byLic = null;
    // 授权文件名称
    private static final String LIC_FILENAME = "license.bin";
    // 读取超时时间
    private static final int TIMEOUT = 5000;
    // 图片工具
    private CertImageUtils imageUtils;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Logs.setsIsLogEnabled(false);
        imageUtils = new CertImageUtils(this.cordova.getContext());
        try {
            InputStream inputStream = this.webView.getView().getResources().getAssets().open(LIC_FILENAME);
            byLic = new byte[inputStream.available()];
            inputStream.read(byLic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("hello")) {
            String name = args.getString(0);
            this.hello(name, callbackContext);
            return true;
        } else if (action.equals("initReader")) {
            this.initReader(callbackContext);
            return true;
        } else if (action.equals("readCard")) {
            this.readCard(callbackContext);
            return true;
        } else if (action.equals("status")) {
            this.status(callbackContext);
            return true;
        }
        return false;
    }

    private void initReader(CallbackContext callbackContext) {
        if (initReaderSuccess) {
            callbackContext.error("设备已初始化");
            return;
        }
        if (idReader == null) {
            idReader = UsbReader.getInstance(cordova.getContext());
        }

        idReader.InitReader(byLic, new IUsbReaderCallback() {
            @Override
            public void ReaderInitSucc() {
                idReader.setLicense(byLic);
                initReaderSuccess = idReader.GetAct();
                callbackContext.success("初始化成功");
            }

            @Override
            public void ReaderInitError() {
                initReaderSuccess = false;
                callbackContext.error("初始化失败");
            }

            @Override
            public void UsbAttach() {
                usbConnect = true;
            }

            @Override
            public void UsbDeAttach() {
                usbConnect = false;
            }
        });
    }

    private void readCard(CallbackContext callbackContext) {
        if (idReader == null || !initReaderSuccess) {
            callbackContext.error("设备未初始化");
            return;
        }
        if (isReading) {
            callbackContext.error("正在读取中");
            return;
        }
        idCardInfo = null;
        isReading = true;
        long start = System.currentTimeMillis();
        try {
            while (idCardInfo == null && initReaderSuccess) {
                idCardInfo = idReader.ReadBaseCardInfo(new String[1]);
                if (idCardInfo != null) {
                    JSONObject data = new JSONObject();
                    data.put("name", idCardInfo.getName());
                    data.put("address", idCardInfo.getAddress());
                    data.put("birthday", idCardInfo.getBirthday());
                    data.put("cardNum", idCardInfo.getCardNum());
                    data.put("cardTypeCode", idCardInfo.getCardTypeCode());
                    data.put("cardTypeName", idCardInfo.getCardTypeName());
                    data.put("nation", idCardInfo.getNation());
                    data.put("sex", idCardInfo.getSex());
                    data.put("sexCode", idCardInfo.getSexCode());
                    Bitmap photo = idCardInfo.getPhoto();
                    data.put("avatar", LondenValidator.bitmapToBase64(photo));
                    Bitmap[] images = new Bitmap[2];
                    if (imageUtils.makeIDCardImages(idCardInfo, images)) {
                        data.put("frontImg", LondenValidator.bitmapToBase64(images[0]));
                        data.put("backImg", LondenValidator.bitmapToBase64(images[1]));
                    }
                    callbackContext.success(data);
                } else if (System.currentTimeMillis() - start >= TIMEOUT) {
                    callbackContext.error("读取超时");
                    break;
                }
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        } finally {
            isReading = false;
        }
    }

    private void status(CallbackContext callbackContext) {
        JSONObject status = new JSONObject();
        try {
            status.put("isUsbConnect", usbConnect);
            status.put("isInit", initReaderSuccess);
            status.put("isReading", isReading);
            callbackContext.success(status);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    private void hello(String name, CallbackContext callbackContext) {
        if (name != null && name.length() > 0) {
            callbackContext.success(name + "，hello!");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "data:image/jpg;base64," + result;
    }
}
