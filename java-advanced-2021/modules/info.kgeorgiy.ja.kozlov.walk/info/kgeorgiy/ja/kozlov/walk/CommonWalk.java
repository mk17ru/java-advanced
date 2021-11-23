package info.kgeorgiy.ja.kozlov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CommonWalk {

    private static final Set<FileVisitOption> optionSet = new HashSet<>();

    private static void walkError(final String text) throws WalkException {
        throw new WalkException(text);
    }

    public static void solve(final Path inputFilePath, final Path outputFilePath, final int depth) {
        if (outputFilePath.getParent() != null) {
            try {
                Files.createDirectories(outputFilePath.getParent());
            } catch (IOException exception) {
                System.err.println("Error of create directory " + exception.getMessage());
            }
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFilePath, StandardCharsets.UTF_8)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
                try {
                    String fileName = bufferedReader.readLine();
                    while (fileName != null) {
                        try {
                            try {
                                Files.walkFileTree(Path.of(fileName), optionSet, depth, new Visitor(bufferedWriter));
                            } catch (InvalidPathException exception) {
                                bufferedWriter.write(String.format("%016x", 0) + " " + fileName);
                                bufferedWriter.newLine();
                            }
                        } catch (IOException exception) {
                            System.err.println("Error write hash " + exception.getMessage());
                        }
                        fileName = bufferedReader.readLine();
                    }
                } catch (IOException exception) {
                    System.err.println("Error of read input file " + exception.getMessage());
                }
            } catch (IOException exception) {
                System.err.println("Open output file error: " + exception.getMessage());
            }
        } catch (IOException exception) {
            System.err.println("Open input file error: " + exception.getMessage());
        }
    }

    protected static void execute(final String[] args, final int depth) {
            if (args == null) {
                System.err.println("No arguments!");
            } else if (args.length != 2) {
                System.err.println("Arguments length should be 2");
            } else if (args[0] == null) {
                System.err.println("Null first argument, requires: <input file>");
            } else if (args[1] == null) {
                System.err.println("Null second argument, requires: <output file>");
            } else {
                try {
                    solve(getFilePath(args[0]), getFilePath(args[1]), depth);
                } catch (WalkException exception) {
                    System.err.println(exception.getMessage());
                }
            }
    }

    private static Path getFilePath(final String fileName) throws WalkException  {
        try {
            return Path.of(fileName);
        } catch (InvalidPathException exception) {
            throw new WalkException("Error with get path " + fileName);
        }
    }

}
