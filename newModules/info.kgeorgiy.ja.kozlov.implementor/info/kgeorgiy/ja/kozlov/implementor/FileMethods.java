package info.kgeorgiy.ja.kozlov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * Class for functions and classes connected with Files
 */
public class FileMethods {
    /**
     * Default constructor
     */
    public FileMethods() {
    }

    /**
     * String representation of the public class
     */
    private final static String IMPL = "Impl";

    /**
     * Build directory for the file path
     * @param toDirectoryPath received path
     * @throws ImplerException throw exception when can't build directory
     */
    public static void buildDirectory(final Path toDirectoryPath) throws ImplerException {
        final Path par = toDirectoryPath.getParent();
        if (par != null) {
            try {
                Files.createDirectories(par.toAbsolutePath());
            } catch (IOException exception) {
                throw new ImplerException("Error can't build directory " +  exception.getMessage());
            }
        }
    }

    /**
     * Create class name for currentToken
     * @param currentToken class
     * @return current classname
     */
    public static String getClassName(final Class<?> currentToken) {
        return String.join("", currentToken.getSimpleName(), IMPL);
    }

    /**
     * Create Path for class
     * @param ending end of file
     * @param inputPath file path
     * @param currentToken received class
     * @return Class {@link Path}
     */
    public static Path createPath(final String ending, final Path inputPath, final Class<?> currentToken) {
        final Path currentPath = inputPath.resolve(currentToken.
                getPackageName().replace(".", File.separator)).resolve(
                String.join("", getClassName(currentToken), ending));
        return currentPath.toAbsolutePath();
    }


    /**
     * FileVisitor which used for clean your recursive clean the directory
     * extends {@link SimpleFileVisitor}
     */
    public static final class Visitor extends SimpleFileVisitor<Path> {

        /**
         * Invoked for a directory after entries in the directory, and all of their
         * descendants, have been visited.
         * Delete file
         *
         * @param directory received directory
         * @param exception received exception
         * @return {@link FileVisitResult#CONTINUE} to continue walking
         * @throws IOException if can't delete file
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path directory, final IOException exception) throws IOException {
            Files.delete(directory);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Invoked for a file in a directory.
         *
         * @param file       received file
         * @param attributes received attributes
         * @return {@link FileVisitResult#CONTINUE} to continue walking
         * @throws IOException if can't delete directory
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Constructor of visitor
         * use super constructor {@link SimpleFileVisitor}
         */
        public Visitor() {
            super();
        }

    }
}
