package the.miner.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provide method for handling file, folder
 */
public class GMFileUtils {

    /**
     * Join file paths
     *
     * @param rootPath root path
     * @param paths    sub paths
     * @return joined path by "/" symbol
     */
    public static File joinFile(String rootPath, String... paths) {
        File root = new File(rootPath);
        for (String path : paths) {
            root = new File(root.getPath(), path);
        }
        return root;
    }

    /**
     * Join file path
     *
     * @param root  root file path
     * @param paths sub file paths
     * @return joined path by "/" symbol
     */
    public static File joinFile(File root, String... paths) {
        return joinFile(root.getPath(), paths);
    }

    /**
     * Replace slash path (\) used in window by (/) in linux environment
     *
     * @param path path
     * @return path with slash of linux environment
     */
    public static String toLinuxPath(String path) {
        return path.replace("\\", "/");
    }

    /**
     * Get file name without extension
     *
     * @param fileName name with extension
     * @return name without extension
     */
    public static String getFileNameWithoutExtension(String fileName) {
        int extIdx = fileName.lastIndexOf(".");
        return (extIdx > 0) ? fileName.substring(0, extIdx) : fileName;
    }

    /**
     * Get file extension
     *
     * @param fileName file name
     * @return extension of file. For example: ".jpg" or ".png"
     */
    public static String getFileExtension(String fileName) {
        int extIdx = fileName.lastIndexOf(".");
        return (extIdx > 0) ? fileName.substring(extIdx) : "";
    }

    /**
     * Delete file
     *
     * @param file file
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
        }
    }

    /**
     * Delete file
     *
     * @param filePath absolute file path
     */
    public static void deleteFile(String filePath) {
        deleteFile(new File(filePath));
    }

    /**
     * Delete directory
     *
     * @param dir directory path
     */
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
            dir.delete();
        }
    }

    /**
     * Delete directory
     *
     * @param dirPath absolute directory path
     */
    public static void deleteDir(String dirPath) {
        deleteDir(new File(dirPath));
    }

    /**
     * Delete file or directory
     *
     * @param fileOrDir file or directory
     */
    public static void delete(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            deleteDir(fileOrDir);
        } else {
            deleteFile(fileOrDir);
        }
    }

    /**
     * Delete file or directory
     *
     * @param fileOrDirPath absolute file or directory path
     */
    public static void delete(String fileOrDirPath) {
        delete(new File(fileOrDirPath));
    }

    /**
     * Move file to specific directory
     *
     * @param file file
     * @param dir  target directory
     * @return true if moving successfully
     */
    public static boolean moveFileToDir(File file, File dir) {
        return renameTo(file, new File(dir, file.getName()));
    }

    /**
     * Move file to specific file path
     *
     * @param source source file
     * @param dest   destination file
     * @return true if moving successfully
     */
    public static boolean moveFile(File source, File dest) {
        return renameTo(source, dest);
    }

    /**
     * Rename/move file to another directory
     *
     * @param srcFile  absolute old file path
     * @param destFile absolute new file path
     * @return true if renaming successfully
     */
    private static boolean renameTo(File srcFile, File destFile) {
        try {
            if (srcFile.renameTo(destFile)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Rename file
     *
     * @param file    file
     * @param newName new file name without extension
     * @return true if renaming successfully
     */
    public static boolean rename(File file, String newName) {
        String ext = getFileExtension(file.getName());
        return renameTo(file, new File(file.getParentFile(), newName + ext));
    }

    /**
     * Copy file to specific path
     *
     * @param src source file path
     * @param dst destination file path
     * @throws IOException exception
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Check where file is image or not
     *
     * @param file         file
     * @param supportTypes supported image type (extension of file)
     * @return true if file is image
     */
    public static boolean isImage(File file, String[] supportTypes) {
        // check file is file or directory
        if (!file.isFile()) {
            return false;
        }

        // check extension to determine file is image or not
        String fileExt = getFileExtension(file.getName());
        for (String ext : supportTypes) {
            if (fileExt.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save data to file
     *
     * @param data byte array
     * @param file file to saved
     * @throws IOException exception
     */
    public static void byteToFile(byte[] data, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(data);
        bos.flush();
        bos.close();
    }

    /**
     * Convert file to Json string
     *
     * @param file json file
     * @return json string
     */
    public static String fileToJson(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }
}
