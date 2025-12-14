package com.rickbot.healing_backend.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AdminBackupService {

    private final Path backupsDir = Paths.get("backups");

    public AdminBackupService() {
        try {
            Files.createDirectories(backupsDir);
        } catch (IOException ignored) {}
    }

    public Map<String, Object> createBackup() {
        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replaceAll(":", "-");
        String name = "backup-" + ts + ".zip";
        Path out = backupsDir.resolve(name);

        // Directories to include
        Path[] includes = new Path[] { Paths.get("data"), Paths.get("logs"), Paths.get("target" ) };

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out.toFile()))) {
            for (Path p : includes) {
                if (Files.exists(p)) {
                    addPathToZip(zos, p.toFile(), p.getFileName().toString());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create backup", ex);
        }

        Map<String,Object> res = new HashMap<>();
        res.put("file", out.toString());
        res.put("name", name);
        return res;
    }

    public Map<String, Object> restoreLatest() {
        try {
            File dir = backupsDir.toFile();
            File[] files = dir.listFiles((d, n) -> n.endsWith(".zip"));
            if (files == null || files.length == 0) throw new RuntimeException("No backup files available");
            // find latest by lastModified
            File latest = files[0];
            for (File f : files) if (f.lastModified() > latest.lastModified()) latest = f;

            // unzip latest into application root (overwrite)
            unzip(latest.toPath(), Paths.get("."));

            Map<String,Object> res = new HashMap<>();
            res.put("restored", latest.getName());
            return res;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to restore backup", ex);
        }
    }

    public Map<String, Object> restoreFromUpload(java.util.zip.ZipInputStream zis) throws IOException {
        // Extract stream into project root
        Path targetDir = Paths.get(".");
        java.util.zip.ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            Path outPath = targetDir.resolve(entry.getName()).normalize();
            if (!outPath.startsWith(targetDir.toAbsolutePath())) {
                zis.closeEntry();
                continue;
            }
            if (entry.isDirectory()) {
                Files.createDirectories(outPath);
            } else {
                Files.createDirectories(outPath.getParent());
                try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                }
            }
            zis.closeEntry();
        }
        Map<String,Object> resp = new HashMap<>();
        resp.put("restored", "uploaded");
        return resp;
    }

    private void unzip(Path zipPath, Path targetDir) throws IOException {
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = targetDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(targetDir.toAbsolutePath())) {
                    // security: skip entries that would extract outside target
                    zis.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void addPathToZip(ZipOutputStream zos, File file, String base) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addPathToZip(zos, child, base + "/" + child.getName());
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry entry = new ZipEntry(base);
                zos.putNextEntry(entry);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
        }
    }

}
