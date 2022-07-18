package fr.naruse.servermanager.filemanager;

import fr.naruse.api.config.Configuration;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SaverUtils {

    public static final File SAVE_FOLDER = new File("server saves");
    public static final Configuration CONFIGURATION = new Configuration(new File(SAVE_FOLDER, "keys.json"));

    public static String getFileName(String key){
        Configuration.ConfigurationSection section = CONFIGURATION.getSection(key);
        if(section != null){
            return section.get("fileName");
        }
        return null;
    }

    public static void delete(String templateName) {
        CONFIGURATION.set(templateName, null);
        CONFIGURATION.save();
    }

    public static class ZipHelper {

        public static void zipFolder(File source, File destination) {

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destination))) {

                Files.walkFileTree(source.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

                        if (attributes.isSymbolicLink()) {
                            return FileVisitResult.CONTINUE;
                        }

                        try (FileInputStream fis = new FileInputStream(file.toFile())) {

                            Path targetFile = source.toPath().relativize(file);
                            zos.putNextEntry(new ZipEntry(targetFile.toString()));

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }

                            zos.closeEntry();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                });

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static void unzipFolder(File source, Path target) throws IOException {

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source))) {

                // list files in zip
                ZipEntry zipEntry = zis.getNextEntry();

                while (zipEntry != null) {

                    boolean isDirectory = false;
                    // example 1.1
                    // some zip stored files and folders separately
                    // e.g data/
                    //     data/folder/
                    //     data/folder/file.txt
                    if (zipEntry.getName().endsWith(File.separator)) {
                        isDirectory = true;
                    }

                    Path newPath = zipSlipProtect(zipEntry, target);

                    if (isDirectory) {
                        Files.createDirectories(newPath);
                    } else {

                        // example 1.2
                        // some zip stored file path only, need create parent directories
                        // e.g data/folder/file.txt
                        if (newPath.getParent() != null) {
                            if (Files.notExists(newPath.getParent())) {
                                Files.createDirectories(newPath.getParent());
                            }
                        }

                        // copy files, nio
                        Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);

                        // copy files, classic
                    /*try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }*/
                    }

                    zipEntry = zis.getNextEntry();

                }
                zis.closeEntry();

            }

        }

        // protect zip slip attack
        private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
                throws IOException {

            // test zip slip vulnerability
            // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

            Path targetDirResolved = targetDir.resolve(zipEntry.getName());

            // make sure normalized file still has targetDir as its prefix
            // else throws exception
            Path normalizePath = targetDirResolved.normalize();
            if (!normalizePath.startsWith(targetDir)) {
                throw new IOException("Bad zip entry: " + zipEntry.getName());
            }

            return normalizePath;
        }
    }

}
