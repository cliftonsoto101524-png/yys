package com.yys.root;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Cloud update manager.
 * Checks for updates, downloads new versions, and manages template updates.
 */
public class CloudUpdateManager {

    private static final String TAG = "CloudUpdateManager";
    private static final int CONNECT_TIMEOUT = 15;
    private static final int READ_TIMEOUT = 30;

    private final OkHttpClient mHttpClient;
    private final Gson mGson;
    private final Handler mMainHandler;
    private final ConfigManager mConfig;

    private OnUpdateListener mListener;

    public interface OnUpdateListener {
        void onCheckStart();
        void onUpdateAvailable(String newVersion, String changelog, long fileSize);
        void onNoUpdate();
        void onDownloadProgress(long downloaded, long total);
        void onDownloadComplete(File apkFile);
        void onError(String message);
    }

    public CloudUpdateManager() {
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
        mGson = new Gson();
        mMainHandler = new Handler(Looper.getMainLooper());
        mConfig = ConfigManager.getInstance();
    }

    public void setListener(OnUpdateListener listener) {
        mListener = listener;
    }

    /**
     * Check for new version from cloud.
     */
    public void checkForUpdate() {
        String baseUrl = mConfig.getCloudUpdateUrl();
        String deviceId = mConfig.getDeviceId();
        String currentVersion = BuildConfig.VERSION_NAME;

        Request request = new Request.Builder()
                .url(baseUrl + "/check?device=" + deviceId + "&version=" + currentVersion)
                .header("User-Agent", "YysAssistant/" + currentVersion)
                .build();

        notifyCheckStart();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Update check failed", e);
                notifyError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyError("Server error: " + response.code());
                    return;
                }
                String body = response.body().string();
                try {
                    JsonObject json = mGson.fromJson(body, JsonObject.class);
                    boolean hasUpdate = json.has("hasUpdate") && json.get("hasUpdate").getAsBoolean();
                    if (hasUpdate) {
                        String version = json.get("version").getAsString();
                        String changelog = json.has("changelog") ? json.get("changelog").getAsString() : "";
                        long fileSize = json.has("fileSize") ? json.get("fileSize").getAsLong() : 0;
                        notifyUpdateAvailable(version, changelog, fileSize);
                    } else {
                        notifyNoUpdate();
                    }
                } catch (Exception e) {
                    notifyError("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Download update APK.
     */
    public void downloadUpdate(String version, File saveDir) {
        String baseUrl = mConfig.getCloudUpdateUrl();
        File outputFile = new File(saveDir, "yys_assistant_v" + version + ".apk");

        Request request = new Request.Builder()
                .url(baseUrl + "/download?version=" + version)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyError("Download failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    notifyError("Download server error: " + response.code());
                    return;
                }
                long total = response.body().contentLength();
                try (java.io.InputStream in = response.body().byteStream();
                     FileOutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    long downloaded = 0;
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;
                        notifyProgress(downloaded, total);
                    }
                    out.flush();
                    notifyDownloadComplete(outputFile);
                } catch (IOException e) {
                    notifyError("Save failed: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Check for template updates.
     */
    public void checkTemplateUpdate() {
        String baseUrl = mConfig.getCloudUpdateUrl();
        int currentTemplateVer = mConfig.getTemplateVersion();

        Request request = new Request.Builder()
                .url(baseUrl + "/templates/check?version=" + currentTemplateVer)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Template check failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                // Parse template update info and trigger download if needed
                // Implementation depends on server response format
            }
        });
    }

    /**
     * Report script execution statistics.
     */
    public void reportStats(String scriptName, long durationMs, boolean success) {
        String baseUrl = mConfig.getCloudUpdateUrl();
        String deviceId = mConfig.getDeviceId();

        FormBody body = new FormBody.Builder()
                .add("device", deviceId)
                .add("script", scriptName)
                .add("duration", String.valueOf(durationMs))
                .add("success", String.valueOf(success))
                .add("version", BuildConfig.VERSION_NAME)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/stats")
                .post(body)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) {}
        });
    }

    // Notification helpers
    private void notifyCheckStart() {
        if (mListener != null) {
            mMainHandler.post(() -> mListener.onCheckStart());
        }
    }

    private void notifyUpdateAvailable(String version, String changelog, long fileSize) {
        if (mListener != null) {
            mMainHandler.post(() -> mListener.onUpdateAvailable(version, changelog, fileSize));
        }
    }

    private void notifyNoUpdate() {
        if (mListener != null) {
            mMainHandler.post(() -> mListener.onNoUpdate());
        }
    }

    private void notifyProgress(long downloaded, long total) {
        if (mListener != null) {
            mMainHandler.post(() -> mListener.onDownloadProgress(downloaded, total));
        }
    }

    private void notifyDownloadComplete(File file) {
        if (mListener != null) {
            mMainHandler.post(() -> mListener.onDownloadComplete(file));
        }
    }

    private void notifyError(String message) {
        if (mListener != null) {
            mMainHandler.post(() -> mListener.onError(message));
        }
    }
}
