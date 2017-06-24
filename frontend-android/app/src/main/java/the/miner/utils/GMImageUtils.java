package the.miner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provide function for converting image from a format to other
 */
public final class GMImageUtils {

    /**
     * Convert bitmap to bytes
     *
     * @param bitmap bitmap image
     * @return bytes
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Convert bytes to bitmap
     *
     * @param data image's data in bytes
     * @return bitmap image
     */
    public static Bitmap bytesToBitmap(byte[] data) {
        if (data == null) return null;

        Bitmap bitmap = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            stream.write(data);
            byte[] bmpData = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Convert bitmap to stream
     *
     * @param bmp bitmap image
     * @return data stream
     */
    public static InputStream bitmapToStream(Bitmap bmp) {
        return new ByteArrayInputStream(bitmapToBytes(bmp));
    }

    /**
     * Convert stream to bitmap
     *
     * @param stream input stream
     * @return bitmap
     */
    public static Bitmap streamToBitmap(InputStream stream) {
        return BitmapFactory.decodeStream(stream);
    }

    /**
     * Decode file to bitmap
     *
     * @param imagePath absolute image file path
     * @return bitmap
     */
    @Deprecated
    public static Bitmap fileToBitmap(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }

    /**
     * Load bitmap from file with specific dimension
     *
     * @param imagePath absolute image file path
     * @param width     width of image viewer
     * @param height    height image viewer
     * @return scaled bitmap
     */
    public static Bitmap fileToBitmap(String imagePath, int width, int height) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);

        // Determine how much to scale down the image
        int scaleFactor = calculateInSampleSize(bmOptions, width, height);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(imagePath, bmOptions);
    }

    /**
     * Decode file to bitmap
     *
     * @param imageFile image  file
     * @return bitmap
     */
    @Deprecated
    public static Bitmap fileToBitmap(File imageFile) {
        return fileToBitmap(imageFile.getPath());
    }

    /**
     * Load bitmap from file
     *
     * @param imageFile image  file
     * @param width     width of image viewer
     * @param height    height image viewer
     * @return bitmap
     */
    public static Bitmap fileToBitmap(File imageFile, int width, int height) {
        return fileToBitmap(imageFile.getAbsolutePath(), width, height);
    }

    /**
     * Create rounded image
     *
     * @param bitmap bitmal image
     * @param pixels radius in pixel
     * @return rounded bitmap
     * @see https://ruibm.com/2009/06/16/rounded-corner-bitmaps-on-android/
     */
    public static Bitmap getRoundedBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Compress image.
     * Refer: http://stackoverflow.com/questions/28424942/decrease-image-size-without-losing-its-quality-in-android
     *
     * @param filePath  image file path
     * @param maxWidth  scaled width
     * @param maxHeight scaled height. If height=0, image will remain ratio
     * @return scaled compressed image file path
     */
    public static String compressImage(String filePath, float maxWidth, float maxHeight) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        // By setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // You try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // return if current image's width < expected width
        if (actualWidth < maxWidth) {
            return filePath;
        }

        // max Height and width values of the compressed image is taken as 816x612
        float imgRatio = actualWidth / actualHeight;
        if (maxHeight == 0) {
            maxHeight = maxWidth * actualHeight / actualWidth;
        }
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        // inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;
        //this options allow android to claim the bitmap memory if it runs low on memory
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(
                    scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(filePath);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filePath;

    }

    /**
     * Calculated sample size of bitmap
     *
     * @param options   bitmap options
     * @param reqWidth  required width
     * @param reqHeight required height
     * @return sample size
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * Save bitmap image to file
     *
     * @param bmp      bitmap image
     * @param filePath file path to save
     * @return file
     */
    public static File bitmapToFile(Bitmap bmp, String filePath) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new File(filePath);
    }
}
