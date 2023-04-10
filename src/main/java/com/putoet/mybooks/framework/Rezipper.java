package com.putoet.mybooks.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Rezipper {
    private static final Logger logger = LoggerFactory.getLogger(Rezipper.class);

    private static final int BUFFER_SIZE = 4 * 1024;

    public static Optional<String> repackage(String filename) {
        final Optional<String> tnp = Rezipper.unzipEpubFile(filename);
        if (tnp.isPresent()) {
            return Rezipper.zipFolder(tnp.get());
        }

        return Optional.empty();
    }

    private static Optional<File> tempFolder() {
        try {
            return Optional.of(Files.createTempDirectory("tmp_epub").toFile());
        } catch (IOException e) {
            logger.error("Could not create temp directory!");
        }
        return Optional.empty();
    }

    private static Optional<File> tempFile() {
        try {
            return Optional.of(Files.createTempFile("tmp_epub", ".epub").toFile());
        } catch (IOException e) {
            logger.error("Could not create temp file!");
        }
        return Optional.empty();
    }

    private static Optional<String> unzipEpubFile(String filename) {
        if (filename == null || !filename.endsWith(".epub"))
            throw new IllegalArgumentException("Invalid filename '" + filename + "'");

        final File sourceFile = new File(filename);
        if (!sourceFile.isFile())
            throw new IllegalArgumentException("'" + filename + "' is not a file");

        final Optional<File> tmp = tempFolder();
        if (tmp.isEmpty())
            throw new IllegalStateException("Could not create tmp directory to unzip epub file " + filename);

        final byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream fis = new FileInputStream(filename)) {
            final ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                final File newFile = newFile(tmp.get(), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    final File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    final FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            return Optional.of(tmp.get().getAbsolutePath());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static Optional<String> zipFolder(String folder) {
        final File folderToZip = new File(folder);
        if (!folderToZip.isDirectory())
            throw new IllegalArgumentException(folder + " is not a folder!");

        final Optional<File> tmp = tempFile();
        if (tmp.isPresent()) {
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tmp.get().getAbsolutePath()))) {
                zipFolder(folderToZip, zipOut);
                return Optional.of(tmp.get().getAbsolutePath());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return Optional.empty();
    }

    private static void zipFolder(File folderToZip, ZipOutputStream zipOut) throws IOException {
        final File[] children = folderToZip.listFiles();
        if (children != null) {
            for (File childFile : children) {
                zipFile(childFile, childFile.getName(), zipOut);
            }
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }

        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }

            final File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            final ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[BUFFER_SIZE];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}
