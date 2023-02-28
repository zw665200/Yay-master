package com.ql.recovery.yay.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.ql.recovery.config.Config;
import com.ql.recovery.yay.callback.Callback;
import com.ql.recovery.yay.callback.FileCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    private static final int RET_SUCCESS = 0;
    private static final int RET_WARNING = 1;
    private static final int RET_FAULT = 2;
    private static final int RET_COMMAND = 7;
    private static final int RET_MEMORY = 8;
    private static final int RET_USER_STOP = 255;

    //检查SDCard存在并且可以读写
    public static boolean isSDCardState() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取sd卡的绝对路径
     *
     * @return
     */
    public static String getSDPath(Context context) {
        File sdDir = null;
        //判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT > 29) {
                File externalFileRootDir = context.getExternalFilesDir(null);
                do {
                    externalFileRootDir = Objects.requireNonNull(externalFileRootDir).getParentFile();
                } while (Objects.requireNonNull(externalFileRootDir).getAbsolutePath().contains("/Android"));
                sdDir = Objects.requireNonNull(externalFileRootDir);
            } else {
                sdDir = Environment.getExternalStorageDirectory();
            }
        }
        if (sdDir != null) {
            return sdDir.toString();
        } else return null;
    }

    /**
     * 判断文件是否已经存在
     *
     * @param fileName 要检查的文件名
     * @return boolean, true表示存在，false表示不存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * 新建目录
     *
     * @param path 目录的绝对路径
     * @return 创建成功则返回true
     */
    public static boolean createFolder(String path) {
        if (path.equals("")) return false;
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            JLog.i("create path = " + path);
            File parent = file.getParentFile();
            if (parent != null) {
                if (parent.exists()) {
                    return file.mkdir();
                } else {
                    boolean b = parent.mkdir();
                    if (b) {
                        return file.mkdir();
                    }
                }
            }
            return false;
        }

    }

    /**
     * 创建文件
     *
     * @param path     文件所在目录的目录名
     * @param fileName 文件名
     * @return 文件新建成功则返回true
     */
    public static boolean createFile(String path, String fileName) {
        File file = new File(path + File.separator + fileName);
        if (file.exists()) {
            return false;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static String getTotalCacheSize(Context context) throws Exception {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return getFormatSize(cacheSize);
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
//            return size + "Byte";
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    /***
     * 清理所有缓存
     * @param context
     */
    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 文件路径
     * @return 删除成功则返回true
     */
    public synchronized static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
//        if (!file.exists()) return false;
//        Runtime runtime = Runtime.getRuntime();
//        String[] args = new String[]{"cmd.exe", "/c", String.format("rd %s /q /s", filePath)};
//        runtime.exec(args);
//        return true;
    }

    /**
     * 删除一个目录（可以是非空目录）
     *
     * @param dir 目录绝对路径
     */
    public static boolean deleteDirection(File dir) {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (file.isFile()) {
                boolean bl = file.delete();
//                if (bl) {
//                    JLog.i("delete dir success");
//                } else {
//                    JLog.i("delete dir failed");
//                }
            } else if (file.isDirectory()) {
                deleteDirection(file);//递归
            }
        }
        dir.delete();
        return true;
    }

    /**
     * 将字符串写入文件
     *
     * @param text     写入的字符串
     * @param fileStr  文件的绝对路径
     * @param isAppend true从尾部写入，false从头覆盖写入
     */
    public static void writeFile(String text, String fileStr, boolean isAppend) {
        try {
            File file = new File(fileStr);
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream f = new FileOutputStream(fileStr, isAppend);
            f.write(text.getBytes());
            f.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
        }
    }

    /**
     * 拷贝文件
     *
     * @param srcPath 绝对路径
     * @param destDir 目标文件所在目录
     * @return boolean true拷贝成功
     */
    public static void copyFile(String srcPath, String destDir, FileCallback callback) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            JLog.i("源文件不存在");
            if (callback != null)
                callback.onFailed("源文件不存在");
            return;
        }

        long length = srcFile.length();
        // 获取待复制文件的文件名
        String fileName = srcPath.substring(srcPath.lastIndexOf(File.separator));
        String destPath = destDir + fileName;
        if (destPath.equals(srcPath)) {
            JLog.i("源文件路径和目标文件路径重复");
            if (callback != null)
                callback.onFailed("源文件路径和目标文件路径重复");
            return;
        }

        JLog.i("export src path = " + srcPath);
        JLog.i("export des path = " + destPath);

        File destFile = new File(destPath); // 目标文件
        if (destFile.exists() && destFile.isFile()) {
            JLog.i("该路径下已经有一个同名文件");
        }

        JLog.i("copy start");
        File destFileDir = new File(destDir);
        if (!destFileDir.exists()) {
            boolean bl = destFileDir.mkdirs();
            if (!bl) {
                callback.onFailed("失败");
                return;
            }
        }

        try {
            FileInputStream fis = new FileInputStream(srcPath);
            FileOutputStream fos = new FileOutputStream(destPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] buf = new byte[1024];
            int c;
            while ((c = bis.read(buf)) != -1) {
                bos.write(buf, 0, c);
            }

            JLog.i("copy finished");
            bos.flush();
            bos.close();
            bis.close();
            fos.close();
            fis.close();

            if (callback != null) {
                callback.onSuccess(destPath);
            }

        } catch (IOException e) {
            if (callback != null) {
                callback.onFailed("导入发生错误");
            }
        }
    }

    /**
     * 重命名文件
     *
     * @param oldPath 旧文件的绝对路径
     * @param newPath 新文件的绝对路径
     * @return 文件重命名成功则返回true
     */
    public static boolean renameTo(String oldPath, String newPath) {
        if (oldPath.equals(newPath)) {
            JLog.i("FileUtils is renameTo：", "文件重命名失败：新旧文件名绝对路径相同");
            return false;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        return oldFile.renameTo(newFile);
    }

    /**
     * 计算某个文件的大小
     *
     * @param path 文件的绝对路径
     * @return 文件大小
     */
    public static long getFileSize(String path) {
        File file = new File(path);
        return file.length();
    }

    /**
     * 计算某个文件夹的大小
     *
     * @param file 目录所在绝对路径
     * @return 文件夹的大小
     */
    public static double getDirSize(File file) {
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“兆”为单位
                return (double) file.length() / 1024 / 1024;
            }
        } else {
            return 0.0;
        }
    }

    /**
     * 获取某个路径下的文件列表
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static File[] getFileList(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                return files;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getFileCounts(String path) {
        return getFiles(path).size();
    }

    /**
     * 获取某个路径下的文件
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static List<File> getFiles(String path) {
        List<File> files = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                files.add(file);
                return false;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    files.addAll(getFiles(file.getPath()));
                }
            }
        }

        return files;
    }

    public static List<File> getChildFolders(String path) {
        List<File> files = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            Collections.addAll(files, subFolders);
        }

        return files;
    }


    /**
     * 获取某个路径下的文件数量
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static List<File> getPicFiles(String path) {
        List<File> files = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                if (path.contains("/avatar/") || path.startsWith("th_") || path.contains("/image/")
                        || path.contains("/image2/") || path.contains("/WeiXin/") || path.contains("/record/")
                        || path.contains("/video/") || path.contains("/sns/") || path.endsWith("_cover")
                        || path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".gif") || path.endsWith("jpeg")) {
                    files.add(file);
                    return false;
                }
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    files.addAll(getFiles(file.getPath()));
                }
            }
        }

        return files;
    }


    /**
     * 获取某个路径下的文件
     *
     * @param path
     * @return
     */
    public static File getFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file;
        } else return null;
    }

    /**
     * 计算某个目录包含的文件数量
     *
     * @param path 目录的绝对路径
     * @return 文件数量
     */
    public static int getFileCount(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        return files.length;
    }

    /**
     * 获取SDCard 总容量大小(MB)
     *
     * @param path 目录的绝对路径
     * @return 总容量大小
     */
    public long getSDCardTotal(String path) {

        if (null != path && path.equals("")) {

            StatFs statfs = new StatFs(path);
            //获取SDCard的Block总数
            long totalBlocks = statfs.getBlockCount();
            //获取每个block的大小
            long blockSize = statfs.getBlockSize();
            //计算SDCard 总容量大小MB
            return totalBlocks * blockSize / 1024 / 1024;

        } else {
            return 0;
        }
    }

    /**
     * 获取SDCard 可用容量大小(MB)
     *
     * @param path 目录的绝对路径
     * @return 可用容量大小
     */
    public long getSDCardFree(String path) {

        if (null != path && path.equals("")) {

            StatFs statfs = new StatFs(path);
            //获取SDCard的Block可用数
            long availaBlocks = statfs.getAvailableBlocks();
            //获取每个block的大小
            long blockSize = statfs.getBlockSize();
            //计算SDCard 可用容量大小MB
            return availaBlocks * blockSize / 1024 / 1024;

        } else {
            return 0;
        }
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchFiles(String path, final String keyword) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }
            if (file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchDetailFiles(String path, final String keyword) {
        final List<File> result = new ArrayList<>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.getName().toLowerCase().equals(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }

            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchDetailFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchFiles(String path, final String keyword, String end) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            String name = file.getName().toLowerCase();
            if (name.contains(keyword.toLowerCase()) && name.endsWith(end)) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 获取文件夹的大小
     *
     * @param
     * @param
     * @return
     */
    public static long getTotalSize(File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSize(child);
        return total;
    }

    /**
     * 关键字查找文件夹
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchFolder(String path, final String keyword) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {

            if (file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 关键字查找文件
     *
     * @param path
     * @param keyword
     * @return
     */
    public static List<File> searchDbFiles(String path, final String keyword) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }
            if (file.getName().toLowerCase().equals(keyword.toLowerCase())) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchDbFiles(file.getPath(), keyword));
                }
            }
        }

        return result;
    }

    /**
     * 查找目录下所有文件
     *
     * @param path
     * @return
     */
    public static List<File> searchFiles(String path) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory()) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath()));
                }
            }
        }

        return result;
    }


    /**
     * 查找目录下所有文件
     *
     * @param path
     * @return
     */
    public static List<File> searchFiles(String path, Boolean stop) {
        final List<File> result = new ArrayList<File>();
        File folder = new File(path);
        File[] subFolders = folder.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }

            if (file.isFile()) {
                result.add(file);
                return true;
            }
            return false;
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isDirectory() && !stop) {
                    // 如果是文件夹，则递归调用本方法
                    result.addAll(searchFiles(file.getPath(), stop));
                }
            }
        }

        return result;
    }


    /**
     * 保存图片到缓存路径
     *
     * @param context
     * @param bitmap
     */
    public static void saveImageToCache(Context context, Bitmap bitmap, Bitmap.CompressFormat format, FileCallback callback) {
        String fileName = System.currentTimeMillis() + "." + format;
        File file = new File(context.getExternalCacheDir(), fileName);
        if (bitmap == null) {
            JLog.i("image is null");
            callback.onFailed("图片为空");
            return;
        }

        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            if (bitmap.compress(format, 100, outputStream)) {
                outputStream.flush();
                outputStream.close();
                callback.onSuccess(file.getAbsolutePath());
            }
        } catch (Exception e) {
            callback.onFailed("处理失败");
        }
    }

    /**
     * 保存图片到缓存路径
     *
     * @param context
     * @param bitmap
     */
    public static void saveImageToCache(Context context, Bitmap bitmap, String fileName, FileCallback callback) {
        if (bitmap == null) {
            JLog.i("image is null");
            callback.onFailed("image is empty");
            return;
        }

        File rootFile = context.getExternalFilesDir("video");
        if (rootFile == null) return;

        String target = rootFile.getAbsolutePath() + File.separator + AppUtil.md5Encode(fileName) + ".jpg";

        File file = new File(target);
        if (file.exists()) {
            callback.onSuccess(target);
            return;
        }

        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                outputStream.flush();
                outputStream.close();
                callback.onSuccess(file.getAbsolutePath());
            }
        } catch (Exception e) {
            callback.onFailed("save failed");
        }
    }

    /**
     * 根据压缩比例保存图片
     *
     * @param context
     * @param file
     * @param format  格式
     */
    public static void saveJPGImage(Context context, File file, Bitmap.CompressFormat format, Callback callback) {

        String filePath;
        String fileName = System.currentTimeMillis() + "";

        if (RomUtil.isMiui() || RomUtil.isEmui()) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName;
        } else {
            // Meizu 、Oppo
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + fileName;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            JLog.i("image is null");
            return;
        }

        saveSignImage(context, file.getAbsolutePath(), filePath, fileName, bitmap, format, callback);
    }

    private static void saveSignImage(Context context, String srcPath, String targetPath, String fileName,
                                      Bitmap bitmap, Bitmap.CompressFormat format, Callback callback) {
        try {
            //设置保存参数到ContentValues中
            ContentValues contentValues = new ContentValues();
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            //设置文件时间
            contentValues.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis());
            contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());

            //设置文件类型
            if (format == Bitmap.CompressFormat.PNG) {
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            } else {
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替，RELATIVE_PATH是相对路径不是绝对路径
                if (RomUtil.isMiui() || RomUtil.isEmui()) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera/");
                } else {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");
                }
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
            } else {
                contentValues.put(MediaStore.Images.Media.DATA, targetPath);
            }

            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(format, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }

                if (callback != null) {
                    JLog.i("callback不为空");
                    callback.onSuccess();
                } else {
                    JLog.i("callback为空");
                }

                //刷新相册
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                localIntent.setData(uri);
                context.sendBroadcast(localIntent);
            }
        } catch (Exception e) {
        }
    }


    public static void UnZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                JLog.i(outPathString + File.separator + szName);
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    JLog.i("Create the file:" + outPathString + File.separator + szName);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                // 获取文件的输出流
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // 读取（字节）字节到缓冲区
                while ((len = inZip.read(buffer)) != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    /**
     * 保存文件到Picture目录
     *
     * @param filePath 文件绝对路径
     * @param callback 回调
     */
    public static void saveFileToPictures(String filePath, FileCallback callback) {
        File file = new File(filePath);
        if (file.exists()) {
            String outPath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/PhotoEdit";
            copyFile(filePath, outPath, callback);
        } else {
            callback.onFailed("文件不存在");
        }
    }


    /**
     * 保存图片
     *
     * @param context
     * @param file
     */
    public static void saveImage(Context context, File file) {
        if (!file.exists()) {
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            return;
        }

        saveImage(context, bitmap, 100, null);
    }

    /**
     * 根据压缩比例保存图片
     *
     * @param context
     * @param file
     */
    public static void saveImage(Context context, File file, Callback callback) {
        if (!file.exists()) {
            if (callback != null)
                callback.onFailed("文件不存在");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            callback.onFailed("文件不存在");
            return;
        }

        saveImage(context, bitmap, 100, callback);
    }


    /**
     * 根据压缩比例保存图片
     *
     * @param context
     * @param bitmap
     */
    public static void saveImage(Context context, Bitmap bitmap, Callback callback) {
        if (bitmap == null) {
            callback.onFailed("文件不存在");
            return;
        }

        saveImage(context, bitmap, 100, callback);
    }

    /**
     * 根据压缩比例保存图片
     *
     * @param context
     * @param percent
     */
    public static void saveImage(Context context, Bitmap bitmap, int percent, Callback callback) {
        if (bitmap == null) {
            if (callback != null)
                callback.onFailed("image is null");
            return;
        }

        String filePath;
        String fileName = System.currentTimeMillis() + "";

        if (RomUtil.isMiui() || RomUtil.isEmui()) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName;
        } else {
            // Meizu 、Oppo
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + fileName;
        }

        saveSignImage(context, filePath, fileName, bitmap, percent, Bitmap.CompressFormat.JPEG, callback);
    }


    /**
     * 保存PNG图片
     *
     * @param context
     * @param file
     */
    public static void savePNGImage(Context context, File file) {

        if (!file.exists()) {
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            return;
        }

        savePNGImage(context, bitmap, null);
    }

    /**
     * 保存PNG图片
     *
     * @param context
     * @param bitmap
     */
    public static void savePNGImage(Context context, Bitmap bitmap, Callback callback) {
        if (bitmap == null) {
            if (callback != null)
                callback.onFailed("image is null");
            return;
        }

        String filePath;
        String fileName = System.currentTimeMillis() + "";

        if (RomUtil.isMiui() || RomUtil.isEmui()) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName;
        } else {
            // Meizu 、Oppo
            filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + fileName;
        }

        saveSignImage(context, filePath, fileName, bitmap, 100, Bitmap.CompressFormat.PNG, callback);
    }

    private static void saveSignImage(
            Context context,
            String targetPath,
            String fileName,
            Bitmap bitmap,
            int percent,
            Bitmap.CompressFormat format,
            Callback callback) {
        try {
            //设置保存参数到ContentValues中
            ContentValues contentValues = new ContentValues();
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            //设置文件时间
            contentValues.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis());
            contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());

            //设置文件类型
            if (format == Bitmap.CompressFormat.PNG) {
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            } else {
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替，RELATIVE_PATH是相对路径不是绝对路径
                if (RomUtil.isMiui() || RomUtil.isEmui()) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera/");
                } else {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");
                }
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
            } else {
                contentValues.put(MediaStore.Images.Media.DATA, targetPath);
            }

            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(format, percent, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }

                if (callback != null) {
                    JLog.i("callback不为空");
                    callback.onSuccess();
                } else {
                    JLog.i("callback为空");
                }

                //刷新相册
                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                localIntent.setData(uri);
                context.sendBroadcast(localIntent);
            }
        } catch (Exception e) {
        }
    }


    /**
     * 保存视频
     *
     * @param context
     * @param file
     */
    public static void saveVideo(Context context, File file) {
        String filePath;

        switch (Config.INSTANCE.getROM()) {
            case Config.ROM_MIUI:
            case Config.ROM_EMUI:
                filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
                break;
            default:
                filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";
                break;
        }

        String name = System.currentTimeMillis() + "";

        try {

            ContentValues contentValues = getVideoContentValues(name);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //执行insert操作，向系统文件夹中添加文件,EXTERNAL_CONTENT_URI代表外部存储器，该值不变
                ContentResolver resolver = context.getContentResolver();
                Uri mediaUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri uri = resolver.insert(mediaUri, contentValues);
                if (uri != null) {
                    JLog.i("uri is not null");
                    //若生成了uri，则表示该文件添加成功,使用流将内容写入该uri中即可
                    ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "rw", null);
                    if (pfd != null) {
                        FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                        FileInputStream in = new FileInputStream(file);
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                        pfd.close();

                        contentValues.clear();
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                        contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                        contentValues.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
                        contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                        resolver.update(uri, contentValues, null, null);
                    }
                } else {
                    JLog.i("uri is null");
                    FileUtil.copyFile(file.getAbsolutePath(), filePath, new FileCallback() {
                        @Override
                        public void onFailed(@NonNull String message) {

                        }

                        @Override
                        public void onSuccess(@NonNull String filePath) {

                        }
                    });

                }
            } else {
                //执行insert操作，向系统文件夹中添加文件,EXTERNAL_CONTENT_URI代表外部存储器，该值不变
                ContentResolver resolver = context.getContentResolver();
                Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (uri != null) {
                    JLog.i("uri is not null");
                    //若生成了uri，则表示该文件添加成功,使用流将内容写入该uri中即可
                    ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "rw", null);
                    if (pfd != null) {
                        FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                        FileInputStream in = new FileInputStream(file);
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                        pfd.close();

                        contentValues.clear();
                        resolver.update(uri, contentValues, null, null);
                    }
                } else {
                    JLog.i("uri is null");
                    FileUtil.copyFile(file.getAbsolutePath(), filePath, new FileCallback() {
                        @Override
                        public void onFailed(@NonNull String message) {

                        }

                        @Override
                        public void onSuccess(@NonNull String filePath) {

                        }
                    });

                }
            }

            //刷新相册
            Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            localIntent.setData(Uri.parse("file://" + filePath + name));
            context.sendBroadcast(localIntent);


        } catch (Exception e) {
            JLog.i("save error = " + e);
        }

    }

    public static ContentValues getVideoContentValues(String fileName) {
        //设置保存参数到ContentValues中
        ContentValues contentValues = new ContentValues();
        //设置文件名
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //android Q中不再使用DATA字段，而用RELATIVE_PATH代替,RELATIVE_PATH是相对路径不是绝对路径
            if (RomUtil.isMiui() || RomUtil.isEmui()) {
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Camera/");
            } else {
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/");
            }

            contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            contentValues.put(MediaStore.Video.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
        }

        //设置文件类型
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

        return contentValues;
    }

    public static void downloadVideo(Context context, @NonNull String url, @NonNull Callback callback) {
        try {
            JLog.i("download url = " + url);
            URL u = new URL(url);
            InputStream is = u.openStream();
            String target = getSDPath(context) + Config.INSTANCE.getPICTURE_PATH() + System.currentTimeMillis() + ".mp4";
            JLog.i("target = " + target);
            File file = new File(target);
            if (!file.exists()) {
                boolean bl = file.createNewFile();
            }
            OutputStream os = new FileOutputStream(target);
            byte[] buf = new byte[4 * 1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                os.write(buf, 0, read);
            }

            callback.onSuccess();
            JLog.i("download finished");

            os.flush();
            os.close();
            is.close();
        } catch (IOException e) {
            JLog.i("download error:" + e.toString());
            callback.onFailed("保存视频失败");
        }
    }

    public static void downloadVideo(Context context, @NonNull String url, @NonNull FileCallback callback) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) u.openConnection();
            InputStream is = httpURLConnection.getInputStream();
            File rootFile = context.getExternalFilesDir("video");
            if (rootFile == null) return;

            String target = rootFile + File.separator + AppUtil.md5Encode(url) + ".mp4";
            File file = new File(target);
            if (file.exists()) {
                callback.onSuccess(target);
                return;
            }

            if (!file.exists()) {
                boolean bl = file.createNewFile();
            }

            OutputStream os = new FileOutputStream(target);
            byte[] buf = new byte[4 * 1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                os.write(buf, 0, read);
            }

            os.flush();
            os.close();
            is.close();

            JLog.i("download finished");
            callback.onSuccess(target);

        } catch (IOException e) {
            JLog.i("download error:" + e);
        }
    }

    public static void downloadPartOfVideo(Context context, @NonNull String url, @NonNull FileCallback callback) {
        try {
//            JLog.i("download url = " + url);
            URL u = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) u.openConnection();
            InputStream is = httpURLConnection.getInputStream();
            File rootFile = context.getExternalFilesDir("video");
            if (rootFile == null) return;

            String target = rootFile + File.separator + AppUtil.md5Encode(url) + ".mp4";
            File file = new File(target);
            if (file.exists()) {
                callback.onSuccess(target);
                return;
            }

            if (!file.exists()) {
                boolean bl = file.createNewFile();
            }

            OutputStream os = new FileOutputStream(target);
            byte[] buf = new byte[4 * 1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                os.write(buf, 0, read);
            }

            os.flush();
            os.close();
            is.close();

            JLog.i("download finished");
            callback.onSuccess(target);

        } catch (IOException e) {
            JLog.i("download error:" + e);
        }
    }

    public static String getFileExtensionFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:

            if (!filename.isEmpty() &&
                    Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";

    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }

        return output.toByteArray();
    }

    public static void byteToFile(byte[] bytes, String destPath) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            FileOutputStream out = new FileOutputStream(destPath);
            byte[] buf = new byte[1024];
            int c;
            while ((c = bis.read(buf)) != -1) {
                out.write(buf, 0, c);
            }

            JLog.i("save finished");
            out.close();
            bis.close();

        } catch (IOException e) {
            JLog.i("save error:" + e);
        }
    }


    public static String getRealPathFromUri(Context context, Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

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
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
