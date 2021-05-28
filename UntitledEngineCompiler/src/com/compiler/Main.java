package com.compiler;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Main {
    public static File tempBuildFolder;
    public static File tempJavaFolder;
    public static File tempClassFolder;

    public static void main(String[] args) throws Exception {
        String javac_ver = execCmd("javac --version");
        if (!javac_ver.contains("javac ")) {
            System.out.println("Javac is not exists on your computer!");
            System.exit(-1);
        }

        System.out.println("UntitledEngine Compiler v.0.2 with " + javac_ver);

        if (args.length < 1) {
            System.out.println("Usage:java -jar UnityEngineCompiler.jar <*.ues script file> <output folder>");
            System.exit(-1);
        } else {
            File scriptFile = new File(args[0]);
            File outputFolder = null;
            if (scriptFile.exists() && scriptFile.isFile()) {
                if (args.length < 2) {
                    outputFolder = new File(scriptFile.getParent() + "/output");
                } else {
                    outputFolder = new File(args[1]);
                }
            } else {
                System.out.println("File " + scriptFile.toString() + " is Not Found or is Not File!");
                System.exit(-1);
            }

            if (outputFolder.exists()) deleteFolder(outputFolder);
            outputFolder.mkdirs();

            tempBuildFolder = new File(outputFolder.getAbsolutePath() + "/build");
            tempJavaFolder = new File(tempBuildFolder.getAbsolutePath() + "/java");
            tempClassFolder = new File(tempBuildFolder.getAbsolutePath() + "/classes");

            tempBuildFolder.mkdirs();
            tempJavaFolder.mkdirs();
            tempClassFolder.mkdirs();

            ScriptParser.parseScript(scriptFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Main.tempJavaFolder + "/Main.java")));
            String ParsedJavaCode = "import java.io.*;\n\n" +
                    "public class Main {\n" +
                    "   public static void main(String[] args) throws Exception {\n     " +
                    scriptFile.getName().split("\\.")[0] + ".func();\n" +
                    "   }\n}\n";
            writer.write(ParsedJavaCode);
            writer.close();

            InputStream inputStream = Main.class.getResource("data.txt").openStream();
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            File targetFile = new File(tempJavaFolder.getAbsolutePath() + "/data.java");
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);

            inputStream.close();
            outStream.close();

            System.out.println("Compiling using javac...");
            StringBuilder javacOption = new StringBuilder();
            javacOption.append("javac ");

            for (File f : Objects.requireNonNull(tempJavaFolder.listFiles())) {
                if (f.isFile()) javacOption.append(f.getAbsolutePath()).append(" ");
            }
            javacOption.append("-d ").append(tempClassFolder);
            String errors = execErrorCmd(String.valueOf(javacOption));
            if (errors != null) {
                System.out.println(errors);
                System.out.println("\nTask Done with errors!");
            } else {
                System.out.println("Making Runnable jar file...");

                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "Main");
                JarOutputStream target = new JarOutputStream(new FileOutputStream(outputFolder.getParentFile().getAbsolutePath() + "/" + scriptFile.getName() + ".jar"), manifest);
                addJar(tempClassFolder, target);
                target.close();

                System.out.println("Cleaning up...");
                deleteFolder(tempBuildFolder);
                System.out.println("\nTask Done!");
            }
        }
    }

    static void deleteFolder(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteFolder(f);
                }
            }
        }
        file.delete();
    }

    private static void addJar(File source, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : Objects.requireNonNull(source.listFiles()))
                    addJar(nestedFile, target);
                return;
            }

            JarEntry entry = new JarEntry(source.getName());
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null)
                in.close();
        }
    }

    public static String execErrorCmd(String cmd) {
        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec(cmd).getErrorStream();
             Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String execCmd(String cmd) {
        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();
             Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
