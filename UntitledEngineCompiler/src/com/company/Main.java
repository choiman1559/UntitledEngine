package com.company;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static File tempBuildFolder;
    public static File tempJavaFolder;
    public static File tempClassFolder;

    public static void main(String[] args) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec(PlatformUtil.isWindows() ? "cls" : "clear");

        String javac_ver = execCmd("javac --version");
        if(!javac_ver.contains("javac ")) {
            System.out.println("Javac is not exists on your computer!");
            System.exit(-1);
        }

        String jar_ver = execCmd("jar --version");
        if(!jar_ver.contains("jar ")) {
            System.out.println("Jar is not exists on your computer!");
            System.exit(-1);
        }

        System.out.println("UntitledEngine Compiler v.0.1 with " + javac_ver);

        if(args.length < 1) {
            System.out.println("Usage:java -jar UnityEngineCompiler.jar <*.ues script file> <output folder>");
            System.exit(-1);
        } else {
            File scriptFile = new File(args[0]);
            File outputFolder = null;
            if(scriptFile.exists() && scriptFile.isFile()) {
                if (args.length < 2) {
                    outputFolder = new File(scriptFile.getParent() + "/output");
                    if(!outputFolder.exists()) outputFolder.mkdirs();
                } else {
                    outputFolder = new File(args[1]);
                    if(!outputFolder.exists()) outputFolder.mkdirs();
                }
            } else {
                System.out.println("File " + scriptFile.toString() + "is Not Found or is Not File!");
                System.exit(-1);
            }

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

            System.out.println("\nCompiling using javac...\n");
            StringBuilder javacOption = new StringBuilder();
            javacOption.append("javac ");

            for(File f : Objects.requireNonNull(tempJavaFolder.listFiles())) {
                if(f.isFile()) javacOption.append(f.getAbsolutePath()).append(" ");
            }
            javacOption.append("-d ").append(tempClassFolder);
            String errors = execErrorCmd(String.valueOf(javacOption));
            if(errors != null) {
                System.out.println(errors);
                System.out.println("\nTask Done with errors!");
            } else {
                System.out.println("Task Done!");
            }
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
