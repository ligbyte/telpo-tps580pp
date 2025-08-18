package com.stkj.cashier.util.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DatabaseBackupUtils {

    public final static String TAG = "DatabaseBackupUtils";
    /**
     * 将 face.db 数据库备份到指定路径
     *
     * @param context      应用上下文
     * @param databaseName 数据库名称（不带路径）
     * @param targetPath   目标路径（如 /storage/18B4-C4D6）
     * @return 备份是否成功
     */
    public static boolean backupDatabaseToPath(Context context, String databaseName, String targetPath) {
        // 获取数据库文件路径
        File dbFile = context.getDatabasePath(databaseName);
        if (!dbFile.exists()) {
            Log.d(TAG, "limesql 数据库文件不存在  31 ");
            return false;
        }

        // 构造目标路径
        File targetDir = new File(targetPath);
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                Log.d(TAG, "limesql 无法创建目标目录  38 ");
                return false;
            }
        }

        // 目标文件路径
        File backupFile = new File(targetDir, databaseName);

        try (FileInputStream inputStream = new FileInputStream(dbFile);
             FileOutputStream outputStream = new FileOutputStream(backupFile);
             FileChannel inputChannel = inputStream.getChannel();
             FileChannel outputChannel = outputStream.getChannel()) {

            // 复制数据
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            Log.d(TAG, "limesql 数据库备份成功！路径   " + backupFile.getAbsolutePath());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "limesql 数据库备份失败  59 ");
            return false;
        }
    }

    /**
     * 动态获取外置存储设备的路径（如 U 盘路径）
     *
     * @param context 应用上下文
     * @return 外置存储路径，如果未找到则返回 null
     */
    public static String getExternalStoragePath(Context context) {
        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);
        for (File file : externalFilesDirs) {
            if (file != null && !file.equals(context.getExternalFilesDir(null))) {
                // 去除应用专属目录部分，返回根路径
                String path = file.getAbsolutePath();
                return path.substring(0, path.indexOf("/Android"));
            }
        }
        return null;
    }
}
