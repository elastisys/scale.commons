package com.elastisys.scale.commons.util.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.elastisys.scale.commons.util.precond.Preconditions;

/**
 * Convenience file system methods.
 *
 *
 */
public class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class not intended to be instantiated");
    }

    /**
     * Returns <code>true</code> if the user owning the process can write to the
     * given directory.
     * <p/>
     * The method will make an attempt to create a file in the directory.
     *
     * @param directory
     *            A directory.
     * @return
     * @throws IllegalArgumentException
     *             If the passed directory doesn't exist or is something other
     *             than a directory.
     */
    public static boolean canWriteTo(File directory) throws IllegalArgumentException {
        Preconditions.checkArgument(directory != null, "directory cannot be null");
        Preconditions.checkArgument(directory.exists(), "directory %s doesn't exist", directory.getAbsolutePath());
        Preconditions.checkArgument(directory.isDirectory(), "%s is not a directory", directory.getAbsolutePath());
        try {
            File probe = File.createTempFile("testprobe", null, directory);
            probe.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Deletes a file or directory (recursively).
     *
     * @param dirEntry
     *            A file or directory to be deleted.
     * @throws IOException
     *             Thrown if deletion fails.
     */
    public static void deleteRecursively(File dirEntry) throws IOException {
        Preconditions.checkArgument(dirEntry != null, "null directory entry");

        if (!dirEntry.exists()) {
            return;
        }
        if (dirEntry.isDirectory()) {
            File[] files = dirEntry.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                }
            }
        }
        java.nio.file.Files.delete(dirEntry.toPath());
    }

    /**
     * Lists all sub-directories in a file system directory.
     *
     * @param directory
     *            A file system directory.
     * @return
     */
    public static List<File> listDirectories(File directory) {
        Objects.requireNonNull(directory, "directory argument is null");
        Preconditions.checkArgument(directory.isDirectory(), "%s is not a directory", directory);
        File[] directories = directory.listFiles(File::isDirectory);
        return Arrays.asList(directories);
    }

    /**
     * Returns the current working directory of the JVM.
     *
     * @return the current working directory.
     */
    public static File cwd() {
        return new File(System.getProperty("user.dir"));
    }

    /**
     *
     * @param filePath
     * @throws IOException
     */
    public static void touch(File file) throws IOException {
        if (!file.exists()) {
            // create empty
            new FileOutputStream(file).close();
        }
        // update modification timestamp
        file.setLastModified(System.currentTimeMillis());
    }

    /**
     * Ensures that a given file path exists by creating the file if it doesn't
     * already exist (including missing parent directories). If the path already
     * exists and is something different than a file, or if the file neither
     * exists nor can be created, an exception will be thrown.
     *
     * @param filePath
     *            A file system path to be created.
     * @throws IllegalArgumentException
     */
    public static void ensureFileExists(String filePath) throws IllegalArgumentException {
        File file = new File(filePath);
        // try to create file if it doesn't exist (will fail if we don't
        // have sufficient permissions to write in the directory)
        if (!file.exists()) {
            try {
                Path parentDir = file.getParentFile().toPath();
                Files.createDirectories(parentDir);
                touch(file);
                return;
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format("failed to initialize file %s", filePath), e);
            }
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException(
                    String.format("the specified file path %s " + "is not a valid file", filePath));
        }
    }
}
