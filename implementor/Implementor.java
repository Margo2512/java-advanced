package info.kgeorgiy.ja.kadochnikova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;

/**
 * Class for implementation and Jar.
 */

public class Implementor implements Impler, JarImpler  {
    /**
     * create assembling.
     */
    private StringBuilder assembling;

    /**
     * default constructor.
     */
    public Implementor() {}
    /**
     * a constant String that contains the string "Impl".
     */
    private static final String IMPL = "Impl";
    /**
     * a constant String that contains the string "return ".
     */
    private static final String RETURN = "return ";
    /**
     * a constant String that contains the string "true".
     */
    private static final String TRUE = "true";
    /**
     * a constant String that contains the string "0".
     */
    private static final String ZERO = "0";
    /**
     * a constant String that contains the string "null".
     */
    private static final String NULL = "null";
    /**
     * a constant String that contains the string ";".
     */
    private static final String SEMICOLON = ";";
    /**
     * a constant String that contains the string "public class ".
     */
    private static final String PUBLIC_CLASS = "public class ";
    /**
     * a constant String that contains the string " implements ".
     */
    private static final String IMPLEMENTS = " implements ";
    /**
     * a constant String that contains the string "{".
     */
    private static final String LEFT_CURLY_BRACE = "{";
    /**
     * a constant String that contains the string "package ".
     */
    private static final String PACKAGE = "package ";
    /**
     * a constant String that contains the string "(".
     */
    private static final String LEFT_PARENTHESIS = "(";
    /**
     * a constant String that contains the string ")".
     */
    private static final String RIGHT_PARENTHESIS = ")";
    /**
     * a constant String that contains the string "}".
     */
    private static final String RIGHT_CURLY_BRACE = "}";
    /**
     * a constant String that contains the string " ".
     */
    private static final String EMPTY_STRING = " ";
    /**
     * a constant String that contains the string ".".
     */
    private static final String POINT = ".";
    /**
     * a constant String that contains the string " arg".
     */
    private static final String ARG = " arg";
    /**
     * a constant String that contains the string ", ".
     */
    private static final String COMMA = ", ";
    /**
     * a constant String that contains the string " throws ".
     */
    private static final String THROWS = " throws ";
    /**
     * a constant String that contains the string "abstract".
     */
    private static final String ABSTRACT = "abstract";
    /**
     * a constant String that contains the string "transient".
     */
    private static final String TRANSIENT = "transient";
    /**
     * a constant String that contains the string "Impl.java".
     */
    private static final String IMPL_JAVA = "Impl.java";

    /**
     * The appendReturnValue method produces the result of returning values according to the type of the return value.
     * @param returnType type of return value.
     */

    private void appendReturnValue(Class<?> returnType) {
        if (returnType.equals(void.class)) {
            return;
        }
        assembling
                .append(RETURN)
                .append(returnType.equals(boolean.class) ? TRUE : returnType.isPrimitive() ? ZERO : NULL)
                .append(SEMICOLON);
    }

    /**
     * The interfaced method that generates implementation code for intarface.
     * @param Classes method to be implemented.
     * @throws ImplerException if the class is not an interface - throws "Not isInterface" error, if the class is private - throws "isPrivate" error.
     */
    public void interfaced(Class<?> Classes) throws ImplerException {
        if (!Classes.isInterface()) {
            throw new ImplerException("Not isInterface");
        }
        if (Modifier.isPrivate(Classes.getModifiers())) {
            throw new ImplerException("isPrivate");
        }
        assembling.append(heading(Classes));
        method(Classes);
    }

    /**
     * The heading method that implements the class header.
     * @param Classes the name that will implement the interface.
     * @return The method creates and implements a class header structure that will implement the given interface.
     */
    public String heading(Class<?> Classes) {
        String name = Classes.getSimpleName() + IMPL;
        StringBuilder assembl = new StringBuilder();
        assembl
                .append(PUBLIC_CLASS)
                .append(name).append(IMPLEMENTS)
                .append(Classes.getCanonicalName())
                .append(LEFT_CURLY_BRACE);
        if (!Classes.getPackageName().equals(EMPTY_STRING)){
            assembl.insert(0, PACKAGE + Classes.getPackageName() + SEMICOLON);
        }
        return assembl.toString();
    }

    /**
     * This parametrs method takes parameters with parameters and adds it to the assembly.
     * @param method type object Method, for which you need to get a list of parameters.
     */
    public void parametrs(Method method) {
        assembling.append(LEFT_PARENTHESIS);
        Class<?>[] p = method.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            assembling.append(p[i].getCanonicalName()).append(ARG).append(i);
            if (i != p.length - 1) {
                assembling.append(COMMA);
            }
        }
        assembling.append(RIGHT_PARENTHESIS);
    }

    /**
     * This genThrows method causes problems arising from generic exceptions that are thrown by the method and throw it in the assembly buffer.
     * @param method type object Method, for which you need to get a list of exception types.
     */
    public void genThrows(Method method) {
        if (method.getExceptionTypes().length > 0) {
            assembling.append(THROWS);
        }
        for (int i = 0; i < method.getExceptionTypes().length; i++) {
            Class<?> exceptionType = method.getExceptionTypes()[i];
            assembling.append(exceptionType.getName());
            if (i != method.getExceptionTypes().length - 1) {
                assembling.append(COMMA);
            }
        }
    }

    /**
     * The method that forms a string with all methods.
     * @param Classes the class object whose methods will be processed.
     */
    public void method(Class<?> Classes) {
        for (Method method : Classes.getMethods()) {
            assembling.append(Modifier.toString(method.getModifiers()).
                            replace(ABSTRACT, EMPTY_STRING)
                            .replace(TRANSIENT, EMPTY_STRING))
                    .append(EMPTY_STRING)
                    .append(method.getReturnType().getTypeName())
                    .append(EMPTY_STRING)
                    .append(method.getName());
            parametrs(method);
            genThrows(method);
            assembling.append(LEFT_CURLY_BRACE);
            appendReturnValue(method.getReturnType());
            assembling.append(RIGHT_CURLY_BRACE);
        }
    }

    /**
     * The implement method that responsible for implementing the interface and writing to a file.
     * @param Classes a type token representing the class/interface for which the implementation is being implemented.
     * @param path root directory.
     * @throws ImplerException if there was an error generating or writing the file.
     */
    public void implement(Class<?> Classes, Path path) throws ImplerException {
        String files = File.separator;
        assembling = new StringBuilder();
        Path packagePath = path.resolve(Classes.getPackageName().replace(POINT, files));
        Path filePath = packagePath.resolve(Classes.getSimpleName() + IMPL_JAVA);

        try {
            interfaced(Classes);
            assembling.append(RIGHT_CURLY_BRACE);
            Files.createDirectories(packagePath);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(assembling.toString());
            } catch (IOException e) {
                throw new ImplerException("Error creating file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new ImplerException("Error creating file: " + e.getMessage());
        }
    }

    /**
     * A implementJar method that uses an interface or class compiles and packages it into a Jar.
     * @param token the token type to create the implementation.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if there was an error generating or compiling the file.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(jarFile.getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Couldn't create a temp dir", e);
        }
        Path classFile = tempDir.resolve(token.getPackageName().replace(".", File.separator))
                .resolve(token.getSimpleName() + "Impl.java");
        implement(token, tempDir);
        compile(token, tempDir, classFile);
        String classFileName = token.getPackageName().replace(".","/") + "/" + token.getSimpleName() + "Impl.class";
        Path classDir = tempDir.resolve(token.getPackageName().replace(".", File.separator))
                .resolve(token.getSimpleName() + "Impl.class");
        try {
            createJarFile(jarFile, classDir, classFileName);
        } catch (IOException e) {
            throw new ImplerException("Error", e);
        }
    }

    /**
     * The createJarFile method, which creates a Jar file, creates an interface of the compiled classes.
     * @param jarFile type object Path, the path to the jar file to create.
     * @param classDir type object Path, the path to the directory where the class file is stored.
     * @param classFileName type object String, the name of the class file to be included in the jar.
     * @throws IOException if an I/O error occurs while creating the jar file.
     */
    private void createJarFile(Path jarFile, Path classDir, String classFileName) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(classFileName));
            Files.copy(classDir, jarOutputStream);
        }
    }

    /**
     * The compile method that compile files.
     * @param token class for which files need to be compiled.
     * @param root type object Path, the root directory where the compiled class files are created.
     * @param files the path to the file with the source code to compile.
     * @throws ImplerException if compilation failed with an error.
     */
    private static void compile(Class<?> token, final Path root, Path files) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler, include tools.jar to classpath");
        }
        final String classpath;
        try {
            classpath = root + File.pathSeparator + Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final String[] args = {files.toString(), "-cp", classpath, "-encoding", "utf-8"};
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code");
        }
    }

    /**
     * The main class for creating interfaces and packaging them in JAR archives.
     * This program requires two or three arguments.
     * @param args an array of strings, command line arguments that are passed through the program.
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || (args.length == 3 && !args[0].equals("-jar")) || args.length > 3) {
            System.err.println("Incorrect number of args");
            return;
        }
        try {
            Implementor implementor = new Implementor();
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
            if (args.length == 3) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ImplerException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}