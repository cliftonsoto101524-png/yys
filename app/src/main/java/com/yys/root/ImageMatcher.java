package com.yys.root;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced image matching engine with multi-resolution support.
 * Uses OpenCV template matching with multiple algorithms.
 */
public class ImageMatcher {

    private static final String TAG = "ImageMatcher";

    public static class MatchResult {
        public final boolean found;
        public final int x;
        public final int y;
        public final double confidence;
        public final int width;
        public final int height;

        public MatchResult(boolean found, int x, int y, double confidence, int w, int h) {
            this.found = found;
            this.x = x;
            this.y = y;
            this.confidence = confidence;
            this.width = w;
            this.height = h;
        }

        public int centerX() { return x + width / 2; }
        public int centerY() { return y + height / 2; }
    }

    private float mThreshold;
    private int[] mScreenSize;

    public ImageMatcher() {
        mThreshold = ConfigManager.getInstance().getMatchThreshold();
    }

    /**
     * Update screen resolution for adaptive matching.
     */
    public void setScreenSize(int width, int height) {
        mScreenSize = new int[]{width, height};
    }

    public void setThreshold(float threshold) {
        mThreshold = threshold;
    }

    /**
     * Match a template image against the source bitmap.
     * Supports multi-resolution by scaling template if needed.
     */
    public MatchResult match(Bitmap source, String templatePath) {
        if (source == null || source.isRecycled()) {
            Log.w(TAG, "Source bitmap is null or recycled");
            return new MatchResult(false, 0, 0, 0, 0, 0);
        }
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            Log.w(TAG, "Template not found: " + templatePath);
            return new MatchResult(false, 0, 0, 0, 0, 0);
        }

        Bitmap template = BitmapFactory.decodeFile(templatePath);
        if (template == null) {
            Log.w(TAG, "Failed to decode template: " + templatePath);
            return new MatchResult(false, 0, 0, 0, 0, 0);
        }

        try {
            return match(source, template);
        } finally {
            template.recycle();
        }
    }

    /**
     * Match template bitmap against source bitmap.
     */
    public MatchResult match(Bitmap source, Bitmap template) {
        if (source == null || template == null) {
            return new MatchResult(false, 0, 0, 0, 0, 0);
        }

        Mat srcMat = new Mat();
        Mat tplMat = new Mat();
        Mat resultMat = new Mat();

        try {
            Utils.bitmapToMat(source, srcMat);
            Utils.bitmapToMat(template, tplMat);

            // Convert to grayscale
            Mat srcGray = new Mat();
            Mat tplGray = new Mat();
            Imgproc.cvtColor(srcMat, srcGray, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.cvtColor(tplMat, tplGray, Imgproc.COLOR_RGBA2GRAY);

            // Ensure template is not larger than source
            if (tplGray.width() > srcGray.width() || tplGray.height() > srcGray.height()) {
                Log.w(TAG, "Template larger than source, skipping");
                return new MatchResult(false, 0, 0, 0, 0, 0);
            }

            // Template matching with normalized correlation coefficient
            int resultCols = srcGray.cols() - tplGray.cols() + 1;
            int resultRows = srcGray.rows() - tplGray.rows() + 1;
            resultMat.create(resultRows, resultCols, CvType.CV_32FC1);

            Imgproc.matchTemplate(srcGray, tplGray, resultMat, Imgproc.TM_CCOEFF_NORMED);

            Core.MinMaxLocResult mmr = Core.minMaxLoc(resultMat);
            double maxVal = mmr.maxVal;
            Point maxLoc = mmr.maxLoc;

            if (maxVal >= mThreshold) {
                return new MatchResult(true,
                        (int) maxLoc.x, (int) maxLoc.y,
                        maxVal, tplGray.width(), tplGray.height());
            }

            return new MatchResult(false, 0, 0, maxVal, 0, 0);
        } finally {
            srcMat.release();
            tplMat.release();
            resultMat.release();
        }
    }

    /**
     * Multi-scale matching for handling different screen resolutions.
     * Scales template by factors 0.5x to 2.0x.
     */
    public MatchResult matchMultiScale(Bitmap source, String templatePath) {
        return matchMultiScale(source, templatePath,
                new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.75f, 2.0f});
    }

    public MatchResult matchMultiScale(Bitmap source, String templatePath, float[] scales) {
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            return new MatchResult(false, 0, 0, 0, 0, 0);
        }

        Bitmap originalTemplate = BitmapFactory.decodeFile(templatePath);
        if (originalTemplate == null) {
            return new MatchResult(false, 0, 0, 0, 0, 0);
        }

        MatchResult bestResult = new MatchResult(false, 0, 0, 0, 0, 0);

        try {
            for (float scale : scales) {
                int newW = Math.max(1, (int) (originalTemplate.getWidth() * scale));
                int newH = Math.max(1, (int) (originalTemplate.getHeight() * scale));
                Bitmap scaled = Bitmap.createScaledBitmap(originalTemplate, newW, newH, true);

                MatchResult result = match(source, scaled);
                scaled.recycle();

                if (result.confidence > bestResult.confidence) {
                    bestResult = result;
                }
                if (bestResult.found && bestResult.confidence > 0.95) {
                    break; // Good enough
                }
            }
        } finally {
            originalTemplate.recycle();
        }

        return bestResult;
    }

    /**
     * Find all occurrences of template in source.
     */
    public List<MatchResult> findAll(Bitmap source, Bitmap template, float threshold) {
        List<MatchResult> results = new ArrayList<>();
        if (source == null || template == null) return results;

        Mat srcMat = new Mat();
        Mat tplMat = new Mat();
        Mat resultMat = new Mat();

        try {
            Utils.bitmapToMat(source, srcMat);
            Utils.bitmapToMat(template, tplMat);

            Mat srcGray = new Mat();
            Mat tplGray = new Mat();
            Imgproc.cvtColor(srcMat, srcGray, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.cvtColor(tplMat, tplGray, Imgproc.COLOR_RGBA2GRAY);

            int resultCols = srcGray.cols() - tplGray.cols() + 1;
            int resultRows = srcGray.rows() - tplGray.rows() + 1;
            resultMat.create(resultRows, resultCols, CvType.CV_32FC1);

            Imgproc.matchTemplate(srcGray, tplGray, resultMat, Imgproc.TM_CCOEFF_NORMED);

            // Find all local maxima above threshold
            for (int y = 0; y < resultMat.rows(); y++) {
                for (int x = 0; x < resultMat.cols(); x++) {
                    double val = resultMat.get(y, x)[0];
                    if (val >= threshold) {
                        // Check if it's a local maximum
                        boolean isLocalMax = true;
                        int checkRadius = 5;
                        for (int dy = -checkRadius; dy <= checkRadius && isLocalMax; dy++) {
                            for (int dx = -checkRadius; dx <= checkRadius; dx++) {
                                int nx = x + dx;
                                int ny = y + dy;
                                if (nx >= 0 && nx < resultMat.cols() && ny >= 0 && ny < resultMat.rows()) {
                                    if (resultMat.get(ny, nx)[0] > val) {
                                        isLocalMax = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (isLocalMax) {
                            results.add(new MatchResult(true, x, y, val, tplGray.width(), tplGray.height()));
                        }
                    }
                }
            }
        } finally {
            srcMat.release();
            tplMat.release();
            resultMat.release();
        }

        return results;
    }

    /**
     * Match template in a specific region of the source.
     */
    public MatchResult matchInRegion(Bitmap source, String templatePath, int rx, int ry, int rw, int rh) {
        if (source == null) return new MatchResult(false, 0, 0, 0, 0, 0);
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        if (rx + rw > source.getWidth()) rw = source.getWidth() - rx;
        if (ry + rh > source.getHeight()) rh = source.getHeight() - ry;
        if (rw <= 0 || rh <= 0) return new MatchResult(false, 0, 0, 0, 0, 0);

        Bitmap region = Bitmap.createBitmap(source, rx, ry, rw, rh);
        MatchResult result = match(region, templatePath);
        region.recycle();

        if (result.found) {
            return new MatchResult(true, result.x + rx, result.y + ry,
                    result.confidence, result.width, result.height);
        }
        return result;
    }

    /**
     * Check if a color exists at given coordinates within tolerance.
     */
    public boolean checkColor(Bitmap source, int x, int y, int expectedColor, int tolerance) {
        if (source == null || x < 0 || y < 0 || x >= source.getWidth() || y >= source.getHeight()) {
            return false;
        }
        int pixel = source.getPixel(x, y);
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        int er = Color.red(expectedColor);
        int eg = Color.green(expectedColor);
        int eb = Color.blue(expectedColor);
        return Math.abs(r - er) <= tolerance && Math.abs(g - eg) <= tolerance && Math.abs(b - eb) <= tolerance;
    }

    /**
     * Find color on screen.
     */
    public Point findColor(Bitmap source, int color, int tolerance) {
        if (source == null) return null;
        for (int y = 0; y < source.getHeight(); y += 2) {
            for (int x = 0; x < source.getWidth(); x += 2) {
                if (checkColor(source, x, y, color, tolerance)) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Draw rectangle on bitmap for debugging.
     */
    public Bitmap drawMatch(Bitmap source, MatchResult result, int color) {
        if (source == null || !result.found) return source;
        Bitmap copy = source.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Utils.bitmapToMat(copy, mat);
        Imgproc.rectangle(mat,
                new Point(result.x, result.y),
                new Point(result.x + result.width, result.y + result.height),
                new Scalar(Color.red(color), Color.green(color), Color.blue(color)), 3);
        Utils.matToBitmap(mat, copy);
        mat.release();
        return copy;
    }
}
