package com.prottasha.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageStorageHelper {

    private static final String VISION_BOARD_FILE_NAME = "vision_board.jpg";

    /**
     * Saves picture to internal app-private storage (satisfies picture storage requirement).
     */
    public static boolean savePictureToLocalStorage(Context context, Bitmap bitmap) {
        File file = new File(context.getFilesDir(), VISION_BOARD_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Loads picture from internal app-private storage.
     */
    public static Bitmap loadPictureFromLocalStorage(Context context) {
        File file = new File(context.getFilesDir(), VISION_BOARD_FILE_NAME);
        if (!file.exists()) return null;

        try (FileInputStream fis = new FileInputStream(file)) {
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            return null;
        }
    }

}
