package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScriptParser {
    public static void parseScript(File file) {
        System.out.println("Parsing " + file.getName() + "...");
        try {
            String[] scriptFileArr;
            List<String> items = new ArrayList<>();
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(dataInputStream));
            String str_line;

            while ((str_line = buffer.readLine()) != null) {
                str_line = str_line.trim();
                if (str_line.length() != 0 && !str_line.startsWith("#")) {
                    items.add(str_line);
                }
            }
            scriptFileArr = items.toArray(new String[0]);

            StringBuilder ParsedJavaCode = new StringBuilder();
            List<String> linkedScriptToCompile = new ArrayList<>();

            ParsedJavaCode.append("import java.io.*;\n");
            ParsedJavaCode.append("import java.util.ArrayList;\n");
            ParsedJavaCode.append("import java.util.List;\n");
            ParsedJavaCode.append("import java.util.Scanner;\n\n");

            int loopCount = 0;
            ParsedJavaCode.append("public class ").append(file.getName().split("\\.")[0]).append("{\n");
            ParsedJavaCode.append("public static void func() throws Exception {\n");
            for (int i = 0; i < scriptFileArr.length; i++) {
                String line = scriptFileArr[i].replace("\t", "");
                String lineWithoutSpace = line.replace(" ", "");

                if (lineWithoutSpace.startsWith("println")) {
                    ParsedJavaCode.append("System.out.").append(line).append(";\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("print.lines")) {
                    int j = i + 1;
                    for (; !scriptFileArr[j].replace("\t", "").replace(" ", "").startsWith("}print.lines"); j++) {
                        ParsedJavaCode.append("System.out.println(\"").append(scriptFileArr[j].replace("\"", "\\\"")).append("\");\n");
                    }
                    i = j;
                    continue;
                }

                if (lineWithoutSpace.startsWith("print")) {
                    ParsedJavaCode.append("System.out.").append(line).append(";\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("exit")) {
                    ParsedJavaCode.append("System.").append(line).append(";\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("call")) {
                    String fileToCall = subStringBetween(line, "(\"", "\")");
                    if (fileToCall.startsWith("./")) fileToCall.replace("./", file.getParent() + "/");
                    File file1 = new File(fileToCall);
                    if (file1.exists()) {
                        linkedScriptToCompile.add(fileToCall);
                        ParsedJavaCode.append(file1.getName().split("\\.")[0]).append(".func();\n");
                    }
                    continue;
                }

                if (lineWithoutSpace.startsWith("wait")) {
                    ParsedJavaCode.append("Thread.sleep(").append(subStringBetween(line, "(", ")")).append(");\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("data")) {
                    ParsedJavaCode.append(line).append(";\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("else if") || lineWithoutSpace.startsWith("if") || lineWithoutSpace.startsWith("else")) {
                    ParsedJavaCode.append(line).append("\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("}else if") || lineWithoutSpace.startsWith("}if") || lineWithoutSpace.startsWith("}else")) {
                    ParsedJavaCode.append("}\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("break")) {
                    ParsedJavaCode.append("break loop").append(--loopCount).append(";\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("while{")) {
                    ParsedJavaCode.append("loop").append(loopCount++).append(" : while(true) {\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("}while")) {
                    ParsedJavaCode.append("}\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("case")) {
                    ParsedJavaCode.append("case \"").append(subStringBetween(line, "\"", "\"")).append("\":\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("default{")) {
                    ParsedJavaCode.append("default:\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("}case") || lineWithoutSpace.startsWith("}default")) {
                    boolean isForStateBreaks = false;
                    for(int j = i - 1;!(scriptFileArr[j].startsWith("case") || scriptFileArr[j].startsWith("default"));j--) {
                        if (scriptFileArr[j].startsWith("break")) {
                            isForStateBreaks = true;
                            break;
                        }
                    }

                    if(!isForStateBreaks) ParsedJavaCode.append("break;\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("input{")) {
                    ParsedJavaCode.append("System.out.print(\"Command> \");\n");
                    ParsedJavaCode.append("switch(new Scanner(System.in).nextLine()) {\n");
                    continue;
                }

                if (lineWithoutSpace.startsWith("}input")) {
                    ParsedJavaCode.append("}\n");
                    continue;
                }

                if(lineWithoutSpace.startsWith("javaImpl:")) {
                    int j = i + 1;
                    for(;!scriptFileArr[j].replace("\t", "").replace(" ", "").startsWith("}javaImpl");j++) {
                        ParsedJavaCode.append(scriptFileArr[j]).append("\n");
                    }
                    i = j;
                }
            }
            ParsedJavaCode.append("}\n}\n");

            System.out.println("Writing " + file.getName() + "...\n");
            File toWrite = new File(Main.tempJavaFolder + "/" + file.getName().split("\\.")[0] + ".java");
            if (!toWrite.exists()) toWrite.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(toWrite));
            writer.write(String.valueOf(ParsedJavaCode));
            writer.close();

            for (String s : linkedScriptToCompile) {
                parseScript(new File(s));
            }
        } catch (Exception e) {
            System.out.println("Exception thrown!: " + e.toString());
            e.printStackTrace();
        }
    }

    private static String subStringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
}
