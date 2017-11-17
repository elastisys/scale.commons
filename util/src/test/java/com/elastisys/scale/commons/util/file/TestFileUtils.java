package com.elastisys.scale.commons.util.file;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

/**
 * Exercises teh {@link FileUtils} class.
 */
public class TestFileUtils {

    private static final File DIR_OWNED_BY_USER = new File("target/somedir");

    @Before
    public void beforeTestMethod() {
        assertTrue(DIR_OWNED_BY_USER.mkdir());
        assertTrue(DIR_OWNED_BY_USER.exists());
        assertTrue(DIR_OWNED_BY_USER.isDirectory());
    }

    @After
    public void afterTestMethod() throws IOException {
        FileUtils.deleteRecursively(DIR_OWNED_BY_USER);
    }

    /**
     * Should be possible to write to a directory created by this user.
     */
    @Test
    public void canWriteTo() {
        // make sure dir is empty at start
        assertTrue(dirFiles(DIR_OWNED_BY_USER).isEmpty());

        assertTrue(FileUtils.canWriteTo(DIR_OWNED_BY_USER));

        // make sure no probe file is left
        assertTrue(dirFiles(DIR_OWNED_BY_USER).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canWriteToWithNullDirectory() {
        FileUtils.canWriteTo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void canWriteToWithNonExistingDirectory() {
        FileUtils.canWriteTo(new File("/non/existing/directory"));
    }

    /**
     * Should not be possible to write to a directory that requires root access.
     */
    @Test
    public void canWriteToWithDirectoryRequiringPrivilegedAccess() {
        assertFalse(FileUtils.canWriteTo(new File("/root")));
    }

    @Test
    public void deleteRecursivelyWithFile() throws IOException {
        File fileToBeDeleted = new File(DIR_OWNED_BY_USER, "file.txt");
        Files.touch(fileToBeDeleted);
        assertThat(dirFiles(DIR_OWNED_BY_USER), is(asList("file.txt")));

        FileUtils.deleteRecursively(fileToBeDeleted);

        assertTrue(dirFiles(DIR_OWNED_BY_USER).isEmpty());
    }

    @Test
    public void deleteRecursivelyWithDirStructure() throws IOException {
        // create a directory structure to be wiped out
        File rootDir = DIR_OWNED_BY_USER;
        File dir1 = new File(rootDir, "dir1");
        File dir2 = new File(rootDir, "dir2");
        File dir3 = new File(rootDir, "dir2/dir3");
        File file1 = new File(dir1, "1.txt");
        File file2 = new File(dir2, "2.txt");
        File file3 = new File(dir3, "3.txt");
        assertTrue(dir1.mkdirs());
        assertTrue(dir2.mkdirs());
        assertTrue(dir3.mkdirs());
        Files.touch(file1);
        Files.touch(file2);
        Files.touch(file3);

        // verify directory content
        List<File> dirEntries = new ArrayList<>();
        Files.fileTreeTraverser().preOrderTraversal(rootDir).forEach(dirEntries::add);
        Collections.sort(dirEntries);
        assertThat(dirEntries, is(asList(rootDir, dir1, file1, dir2, file2, dir3, file3)));

        FileUtils.deleteRecursively(rootDir);

        assertFalse(rootDir.exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteRecursivelyWithNullDir() throws IOException {
        FileUtils.deleteRecursively(null);
    }

    /**
     * Try create a file in a pre-existing directory.
     */
    @Test
    public void ensureFileExists() throws IOException {
        recreateDir(new File("target/dir"));

        File fileToCreate = new File("target/dir/some.file");
        assertFalse(fileToCreate.exists());
        FileUtils.ensureFileExists(fileToCreate.getAbsolutePath());
        assertTrue(fileToCreate.isFile());
    }

    /**
     * Create a file when parent directories also need to be created.
     */
    @Test
    public void ensureFileExistsWithMissingParentDirs() throws IOException {
        recreateDir(new File("target/dir"));

        File fileToCreate = new File("target/dir/deep/down/file.txt");
        assertFalse(fileToCreate.exists());
        FileUtils.ensureFileExists(fileToCreate.getAbsolutePath());
        assertTrue(fileToCreate.isFile());
    }

    /**
     * Should raise an {@link Exception} on failure to create the file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensureFileExistsWithInsufficientPrivileges() {
        FileUtils.ensureFileExists("/root/some.file");
    }

    private List<String> dirFiles(File dir) {
        return asList(dir.list());
    }

    /**
     * Removes a directory and all its content, and recreates an empty directory
     * with the same name/path.
     *
     * @param dir
     * @throws IOException
     */
    private void recreateDir(File dir) throws IOException {
        FileUtils.deleteRecursively(dir);
        dir.mkdirs();
        assertTrue(dir.isDirectory());
    }

}
