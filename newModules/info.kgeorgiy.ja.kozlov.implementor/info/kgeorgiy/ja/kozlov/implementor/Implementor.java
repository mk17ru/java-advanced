package info.kgeorgiy.ja.kozlov.implementor;

import info.kgeorgiy.java.advanced.implementor.JarImpler;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.util.function.BiFunction;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.net.URISyntaxException;
import java.nio.file.*;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;


import static info.kgeorgiy.ja.kozlov.implementor.FileMethods.*;

/**
 * Realisation of interface {@link JarImpler}
 * Create implementation of given class or interface method {@link JarImpler#implement(Class, Path)}
 * Produces <var>.jar</var> file implementing class or interface {@link JarImpler#implementJar(Class, Path)}
 * Create generation of the interfaces and classes
 *
 * @author Kozlov Mihail
 */
public class Implementor implements JarImpler {

    /**
     * String representation of the return
     */
    private final static String RETURN = "return";
    /**
     * String representation of the super
     */
    private final static String SUPER_CLASS = "super(";

    /**
     * String representation of the throws
     */
    private final static String THROWS = "throws";

    /**
     * String representation of the line separator
     */
    private final static String NEXT_LINE = System.lineSeparator();

    /**
     * String representation of the extends
     */
    private final static String EXTENDS = " extends ";

    /**
     * String representation of the implements
     */
    private final static String IMPLEMENTS = " implements ";

    /**
     * String representation of the java-files
     */
    private final static String JAVA_FILE = ".java";

    /**
     * String representation of the class-files
     */
    private final static String CLASS_FILE = ".class";

    /**
     * Directory cleaner used after {@link JarImpler#implementJar(Class, Path)} or {@link JarImpler#implement(Class, Path)}
     */
    private final static Visitor CLEANER_DIRECTORIES = new Visitor();

    /**
     * String representation of the package
     */
    private static final String PACKAGE = "package";

    /**
     * String representation of the public class
     */
    private static final String PUBLIC_CLASS = "public class";

    /**
     * Constructor of Implementor class
     */
    public Implementor() {
    }

    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * CheckArguments by function {@link Implementor#isSupportedToken(Class)}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Token and root can't be null!");
        }
        isSupportedToken(token);
        root = createPath(JAVA_FILE, root, token);
        buildDirectory(root);
        try (BufferedWriter bw = Files.newBufferedWriter(root)) {
            implementClass(token, bw);
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new ImplerException("Exception with write in File " + exception.getMessage());
        }
    }

    /**
     * Produces code implementing class or interface specified by provided {@code currentToken} and {@code BufferedWriter}.
     *
     * @param currentToken type token to create implementation for.
     * @param bw           BufferedWriter which write class {@link BufferedWriter}
     * @throws ImplerException when implementation cannot be generated.
     */
    private void implementClass(Class<?> currentToken, BufferedWriter bw) throws ImplerException {
        buildTopic(bw, currentToken);
        if (!currentToken.isInterface()) {
            buildConstructor(bw, currentToken);
        }
        buildMethods(bw, currentToken);
        buildEnding(bw);
    }

    /**
     * Check token:
     * <ul>
     *     <li> token is not {@link Enum} and {@link Class#isEnum()} {@code == false}</li>
     *     <li>token is not array {@link Class#isArray()}{@code == false}</li>
     *     <li> token is not primitive {@link Class#isPrimitive()}{@code == false}</li>
     *     <li> token is not final </li>
     *     <li> token is not private </li>
     * </ul>
     *
     * @param token type token
     * @throws ImplerException if token is illegal for implementing.
     */
    private void isSupportedToken(Class<?> token) throws ImplerException {
        int modifiers = token.getModifiers();
        if (token.isEnum() || token == Enum.class || token.isArray()
                || token.isPrimitive() || Modifier.isFinal(modifiers)
                || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Illegal class for implements");
        }
    }

    /**
     * Get MethodsWrapper Set from array of method
     *
     * @param methods received method
     * @return set methods wrapper
     */
    private Set<MethodWrapper> getSetMethods(final Method[] methods) {
        return Arrays.stream(methods).map(MethodWrapper::new).collect(Collectors.toSet());
    }

    /**
     * Collect for given token(class or interface) methods to Set
     *
     * @param token received token
     * @return set methods
     */
    private Set<MethodWrapper> collectMethods(final Class<?> token) {
        Set<MethodWrapper> setMethods = new HashSet<>();
        Class<?> tempToken = token;
        while (tempToken != null) {
            setMethods.addAll(getSetMethods(tempToken.getDeclaredMethods()));
            tempToken = tempToken.getSuperclass();
        }
        if (token != null) {
            setMethods.addAll(getSetMethods(token.getMethods()));
        }
//        System.out.println(token.getName() + ":");
//        for (MethodWrapper method : setMethods) {
//            System.out.println(method.getMethod().getName() + " " + method.method.getModifiers());
//        }
//        System.out.println(token.getName() + "______________________________________");
//        for (MethodWrapper method : filterAbstractMethods(setMethods)) {
//            System.out.println(method.getMethod().getName() + " " + method.method.getModifiers());
//        }
        return filterAbstractMethods(setMethods);
    }

    /**
     * Build topic for the implementing class
     *
     * @param bw           BufferedWriter which write class {@link BufferedWriter}
     * @param currentClass class which should be implementing
     * @throws ImplerException error with writing {@link Implementor#write(StringBuilder, BufferedWriter)}
     */
    private void buildTopic(final BufferedWriter bw, final Class<?> currentClass) throws ImplerException {
        StringBuilder result = new StringBuilder();
        if (!currentClass.getPackageName().isEmpty()) {
            result.append(PACKAGE).append(" ").append(currentClass.getPackageName())
                    .append(";").append(NEXT_LINE);
        }
        result.append(PUBLIC_CLASS).append(" ").append(getClassName(currentClass))
                .append(!currentClass.isInterface() ? EXTENDS : IMPLEMENTS);
        result.append(currentClass.getCanonicalName()).append(" {").append(NEXT_LINE);
        write(result, bw);
    }

    /**
     * Filter set of the {@link MethodWrapper} by {@link Modifier#isAbstract(int)}
     *
     * @param setMethods set of the {@link MethodWrapper}
     * @return result set of the {@link MethodWrapper}
     */
    private Set<MethodWrapper> filterAbstractMethods(final Set<MethodWrapper> setMethods) {
        return setMethods.stream().filter(method -> Modifier.isAbstract(method.getMethod().getModifiers()))
                .collect(Collectors.toSet());
    }

    /**
     * Build constructors received class or interface
     * Private constructors are not considered
     *
     * @param bw           BufferedWriter which write class {@link BufferedWriter}
     * @param currentClass received Class
     * @throws ImplerException throws exception if all class constructors is Illegal
     */
    private void buildConstructor(final BufferedWriter bw, final Class<?> currentClass) throws ImplerException {
        List<Constructor<?>> cons = Arrays.stream(currentClass.getDeclaredConstructors())
                .filter(it -> !Modifier.isPrivate(it.getModifiers())).collect(Collectors.toList());
        for (Constructor<?> con : cons) {
            genExecutable(createConstructorTopic(currentClass), this::getArguments, bw, getBodyOfConstructor(con), con);}
        if (cons.size() == 0) {
            throw new ImplerException("All class constructors is Illegal");
        }
    }

//    private StringBuilder constructorToString(final Constructor<?> constructor, final Class<?> currentClass) {
//        return executableToString(this::createConstructorBody, currentClass);
//    }

    /**
     * Main class for implementing classes or interfaces
     *
     * @param arguments <ul>
     *                  <li>3 or 2 arguments</li>
     *                  <li>3 for JarImpler</li>
     *                  <li>2 for Implement</li>
     *                  </ul>
     */
    public static void main(String[] arguments) {
        try {
            if (arguments == null || (arguments.length != 2 && arguments.length != 3)) {
                throw new ImplerException("Wrong arguments expected -jar class-name file.jar or -jar class-name file.jar");
            }
            for (String argument : arguments) {
                if (argument == null) {
                    throw new NullPointerException("Arguments can't be null!");
                }
            }
            if (arguments.length == 2) {
                new Implementor().implement(Class.forName(arguments[0]), Path.of(arguments[1]));
            } else if (!arguments[0].equals("-jar")) {
                throw new ImplerException("Wrong first arguments expected -jar class-name file.jar");
            } else {
                new Implementor().implementJar(Class.forName(arguments[1]), Path.of(arguments[2]));
            }
        } catch (ImplerException | NullPointerException exception) {
            System.err.println(exception.getMessage());
        } catch (ClassNotFoundException exception) {
            System.err.println("Can't found class " + exception.getMessage());

        }
    }

    /**
     * Build body constructor
     *
     * @param cons Constructor
     * @return String body constructor
     */
    private String getBodyOfConstructor(final Constructor<?> cons) {
        return String.join("", SUPER_CLASS, getArguments(cons.getParameters(), false), ")");
    }

    /**
     * Create topic of constructor
     *
     * @param currentToken class
     * @return topic
     */
    private String createConstructorTopic(Class<?> currentToken) {
        return getClassName(currentToken);
    }

    /**
     * Create topic of method
     *
     * @param method method
     * @return topic
     */
    private String createMethodTopic(Method method) {
        return String.join(" ", method.getReturnType().getCanonicalName(), method.getName());
    }

    /**
     * Build method for implementing
     *
     * @param bw    writer
     * @param token class
     * @throws ImplerException if can't build method
     */
    private void buildMethods(final BufferedWriter bw, final Class<?> token) throws ImplerException {
        final Set<MethodWrapper> methods = collectMethods(token);
        for (MethodWrapper meth : methods) {
            genExecutable(createMethodTopic(meth.getMethod()), this::getArguments, bw,
                    getBodyOfMethod(meth.getMethod()), meth.getMethod());
        }
    }


    /**
     * Get argumnets of string
     *
     * @param params   params
     * @param needType type
     * @return string of arguments
     */
    private String getArguments(final Parameter[] params, final boolean needType) {
        return Arrays.stream(params).map(param -> String.join(" ",
                needType ? param.getType().getCanonicalName() : "",
                param.getName())).collect(Collectors.joining(", "));
    }

    /**
     * Return default method return value {@link String}
     *
     * @param method current method {@link Method}
     * @return return default value {@link String}
     */
    private String getBodyOfMethod(final Method method) {
        Class<?> methodReturnType = method.getReturnType();
        String returnValue;
        if (boolean.class.equals(methodReturnType)) {
            returnValue = " false";
        } else if (void.class.equals(methodReturnType)) {
            returnValue = "";
        } else if (methodReturnType.isPrimitive()) {
            returnValue = " 0";
        } else {
            returnValue = " null";
        }
        return RETURN + returnValue;
    }

    /**
     * Generate Executable.
     * <ul>
     *     <li>Use {@link Implementor#collectTopic(StringBuilder, Executable, String, BiFunction)} for topic.</li>
     *     <li>Use {@link Implementor#collectBody(StringBuilder, String)} for body.</li>
     *  </ul>
     *
     *
     * @param createTopic  string topic
     * @param createParams parameters
     * @param bw           writer
     * @param body         string of module
     * @param current      executable
     * @throws ImplerException if can't create Executable generation
     */
    private void genExecutable(final String createTopic,
                               final BiFunction<Parameter[], Boolean, String> createParams,
                               final BufferedWriter bw, final String body, final Executable current)
            throws ImplerException {
        StringBuilder sb = new StringBuilder();
        // CREATE TOPIC
        collectTopic(sb, current, createTopic, createParams);
        // CREATE BODY OF EXECUTABLE
        collectBody(sb, body);
        write(sb, bw);
    }

    /**
     * Create body of method with default body
     *
     * @param sb   received StringBuilder
     * @param body received String body of method
     */
    private void collectBody(final StringBuilder sb, final String body) {
        sb.append(NEXT_LINE).append("\t\t").append(body).append(";");
        sb.append(NEXT_LINE).append("\t").append("}").append(NEXT_LINE);
    }

    /**
     * Generate topic of method
     * <ul>
     *     <li>Name</li>
     *     <li>Arguments</li>
     *     <li>Throws exceptions</li>
     * </ul>
     *
     * @param sb           received StringBuilder
     * @param current      received Executable
     * @param createTopic  received String name of method
     * @param createParams received for generating method parameters
     */
    private void collectTopic(final StringBuilder sb, final Executable current, final String createTopic,
                              final BiFunction<Parameter[], Boolean, String> createParams) {
        sb.append("\t").append(updateModifiers(current.getModifiers())).append(" ").append(createTopic).append("(");
        sb.append(createParams.apply(current.getParameters(), true)).append(") ");
        Class<?>[] currentExceptions = current.getExceptionTypes();
        sb.append(currentExceptions.length != 0 ? String.join(" ", THROWS, Arrays.stream(currentExceptions)
                .map(Class::getCanonicalName).collect(Collectors.joining(", "))) : "");
        sb.append(" ").append("{");
    }

    /**
     * Write something to the <var>bw</var>.
     * When char id is more than 127 than use {@code String.format("\\u%04x", argument)}
     * @param toPrint write  to the writer <var>bw</var>
     * @param bw      writer
     * @throws ImplerException cause by {@link BufferedWriter#write(int)}
     */
    public void write(final StringBuilder toPrint, final BufferedWriter bw) throws ImplerException {
        try {
            for (final char i : toPrint.toString().toCharArray()) {
                if (i > 127) {
                    bw.write(String.format("\\u%04x", (int) i));
                    continue;
                }
                bw.write(i);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new ImplerException("Error with writing to output file" + exception.getMessage());
        }
    }

    /**
     * Write ending of the class
     *
     * @param bw received BufferedWriter
     * @throws ImplerException when can't write the ending.
     */
    private void buildEnding(final BufferedWriter bw) throws ImplerException {
        write(new StringBuilder("}" + NEXT_LINE), bw);
    }

    /**
     * Remove {@link Modifier#NATIVE}, {@link Modifier#TRANSIENT}, {@link Modifier#ABSTRACT} from the class modifiers
     *
     * @param modifiers received
     * @return current class modifiers
     */
    private String updateModifiers(final int modifiers) {
        return Modifier.toString(modifiers & ~Modifier.NATIVE & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     *
     * Generated class classes name is same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (jarFile == null || token == null) {
            throw new ImplerException("Arguments can't be null!");
        }
        Path currentDirectory;
        buildDirectory(jarFile);
        try {
            currentDirectory = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temporaryDirectory");
        } catch (IOException exception) {
            throw new ImplerException("Error can't build temp directory " + exception.getMessage());
        }
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            implement(token, currentDirectory);
            compileJar(currentDirectory, token);
            try (JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
                ZipEntry zipEntry = new ZipEntry(String.join("/",
                        token.getPackageName().replace('.', '/'),
                        String.join("", getClassName(token), CLASS_FILE)));
                outputStream.putNextEntry(zipEntry);
                Files.copy(createPath(CLASS_FILE, currentDirectory, token), outputStream);
            } catch (IOException exception) {
                throw new ImplerException("Can't write jar file: " + exception.getMessage());
            }
        } finally {
            try {
                Files.walkFileTree(currentDirectory, CLEANER_DIRECTORIES);
            } catch (final IOException exception) {
                exception.printStackTrace();
                System.err.println("Error with clean files in the directories!" + exception.getMessage());
            }
        }
    }


    /**
     * Compile Jar file of given class to the given path
     *
     * @param currentDirectory received directory for token
     * @param token            received token
     * @throws ImplerException cause by one of these problems:
     *                         <ul>
     *                             <li>{@link URISyntaxException}</li>
     *                             <li>Can't find java compiler {@link JavaCompiler}</li>
     *                             <li>Can't compile class with method
     *                             {@link JavaCompiler#run(InputStream, OutputStream, OutputStream, String...)}</li>
     *                         </ul>
     */
    private void compileJar(Path currentDirectory, Class<?> token) throws ImplerException {
        final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        if (javaCompiler == null) {
            throw new ImplerException("Can't find java compiler");
        }
        final int compilerCode;
        try {
            String[] commonArguments = new String[]{"-encoding", "UTF-8"};
            String[] pathArguments;
            if (token.getProtectionDomain().getCodeSource() != null) {
                pathArguments = new String[]{"-cp",
                        Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                        createPath(JAVA_FILE, currentDirectory, token).toString()};
            } else {
                pathArguments = new String[]{"--patch-module",
                        String.join("=", token.getModule().getName(), currentDirectory.toString()),
                        createPath(JAVA_FILE, currentDirectory, token).toString()};
            }
            String[] compileArguments = new String[commonArguments.length + pathArguments.length];
            System.arraycopy(commonArguments, 0, compileArguments, 0, commonArguments.length);
            System.arraycopy(pathArguments, 0, compileArguments, commonArguments.length, pathArguments.length);
            compilerCode = javaCompiler.run(null, null, null, compileArguments);
        } catch (URISyntaxException exception) {
            throw new ImplerException("Error with uri: " + exception.getMessage());
        }
        if (compilerCode != 0) {
            throw new ImplerException("Can't compile " + token);
        }
    }

    /**
     * Class for wrap the Method to compare for the:
     * <ul>
     *     <li>{@link Method#getName()}</li>
     *     <li>{@link Method#getParameterTypes()}</li>
     *     <li>{@link Method#getReturnType()}</li>
     * </ul>
     */
    private static final class MethodWrapper {
        /**
         * Method which is wrapped
         */
        private final Method method;

        /**
         * Returned method which was wrapped
         *
         * @return method
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Method which compare object with current method
         * return {@code true} if it is a {@link MethodWrapper}
         * and if {@link Method#getReturnType()} and {@link Method#getName()} and {@link Method#getParameterTypes()} equals
         *
         * @param object received object
         * @return boolean which mean equals or not equals
         */
        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            } else if (object == null) {
                return false;
            } else if (!(object instanceof MethodWrapper)) {
                return false;
            }
            Method that = ((MethodWrapper) object).getMethod();
            return Objects.equals(that.getName(), method.getName())
                    && Arrays.equals(that.getParameterTypes(), method.getParameterTypes())
                    && that.getReturnType().equals(method.getReturnType());
        }

        /**
         * Evaluate hash code of method
         *
         * @return hash code of object
         */
        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(this.method.getParameterTypes()),
                    Objects.hash(method.getName()), Objects.hash(method.getReturnType()));
        }

        /**
         * Constructor of method wrapper
         *
         * @param method received method
         */
        private MethodWrapper(final Method method) {
            this.method = method;
        }

    }


}

