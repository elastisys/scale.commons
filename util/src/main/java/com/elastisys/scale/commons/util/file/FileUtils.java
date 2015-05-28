package com.elastisys.scale.commons.util.file;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience file system methods.
 *
 *
 */
public class FileUtils {

	private FileUtils() {
		throw new UnsupportedOperationException(
				"Utility class not intended to be instantiated");
	}

	/**
	 * Returns <code>true</code> if the user owning the process can write to the
	 * given directory.
	 * <p/>
	 * The method will make an attempt to create a file in the directory.
	 *
	 * @param dir
	 *            A directory.
	 * @return
	 * @throws IllegalArgumentException
	 *             If the passed directory doesn't exist or is something other
	 *             than a directory.
	 */
	public static boolean canWriteTo(File directory)
			throws IllegalArgumentException {
		checkArgument(directory != null, "directory cannot be null");
		checkArgument(directory.exists(), "directory %s doesn't exist",
				directory.getAbsolutePath());
		checkArgument(directory.isDirectory(), "%s is not a directory",
				directory.getAbsolutePath());
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
		checkArgument(dirEntry != null, "null directory entry");

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
		if (!dirEntry.delete()) {
			throw new IOException("Failed to delete file: "
					+ dirEntry.getAbsolutePath());
		}
	}

	/**
	 * Lists all sub-directories in a file system directory.
	 *
	 * @param directory
	 *            A file system directory.
	 * @return
	 */
	public static List<File> listDirectories(File directory) {
		checkNotNull(directory, "directory argument is null");
		checkArgument(directory.isDirectory(), "%s is not a directory",
				directory);
		File[] directories = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
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
}
