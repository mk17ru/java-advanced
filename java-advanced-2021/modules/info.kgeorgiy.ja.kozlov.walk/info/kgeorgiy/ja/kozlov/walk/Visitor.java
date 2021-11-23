package info.kgeorgiy.ja.kozlov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Visitor extends SimpleFileVisitor<Path> {

    private static final int BUFFER_SIZE = 2048;

    private final BufferedWriter bufferedWriter;

    private static final byte[] buffer = new byte[BUFFER_SIZE];


    public Visitor(final BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        hashEval(file);
        return FileVisitResult.CONTINUE;
    }

    /*@Override
    public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException {
        if (!isRecursive) {
            printOutput(directory.toString(), 0);
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }*/

    private void printOutput(final String fileName, final long hash) throws IOException {
        bufferedWriter.write(String.format("%016x", hash) + " " + fileName);
        bufferedWriter.newLine();
    }



    private void hashEval(final Path file) throws IOException {
        try (InputStream inputStreamReader = Files.newInputStream(file)) {
            long answer = 0, size;
            while ((size = inputStreamReader.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                answer = hashPJW(answer, size);
            }
            printOutput(file.toString(), answer);
        } catch (IOException ignored) {
            printOutput(file.toString(), 0);
        }
    }

    private static long hashPJW(long h, final long length) {
        for (int i = 0; i < length; i++) {
            h = (h << 8) + (buffer[i] & 0xff);
            final long high = h & 0xff00_0000_0000_0000L;
            if (high != 0) {
                h ^= high >> 48;
                h &= ~high;
            }
        }
        return h;
    }


}
