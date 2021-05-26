package Engine;

import com.sun.istack.internal.Nullable;
import javafx.scene.Scene;
import jp.uphy.javafx.console.ConsoleView;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameEngineParser {

    static ConsoleView consoleView;

    public static void processGame(File file) throws Exception {
        String[] scriptFileArr;
        List<String> items = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(file);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(dataInputStream));
        String str_line;

        while ((str_line = buffer.readLine()) != null) {
            str_line = str_line.trim();
            if (str_line.length()!=0 && !str_line.startsWith("#")) {
                items.add(str_line);
            }
        }
        scriptFileArr = items.toArray(new String[0]);
        consoleView = new ConsoleView();

        boolean isFullScreen = false;
        boolean setScreenResizeable = false;
        int setScreenWeight = 1024;
        int setScreenHeight = 720;
        String setScreenTitle = "Game Running...";

        boolean isJavaPrefsCalled = false;
        for (int i = 0; i < scriptFileArr.length; i++) {
            String line = scriptFileArr[i].replace("\t","");
            if(line.replace(" ","").startsWith("java:")) {
                isJavaPrefsCalled = true;
                int j = i + 1;
                for(;!scriptFileArr[j].replace(" ", "").startsWith("}java");j++) {
                    if(scriptFileArr[j].startsWith("#")) continue;
                    String[] args = scriptFileArr[j].split("=");
                    switch (args[0]) {
                        case "javafx.isFullScreen":
                            isFullScreen = args[1].equals("true");
                            break;

                        case "javafx.setScreenWeight":
                            setScreenWeight = Integer.parseInt(args[1]);
                            break;

                        case "javafx.setScreenHeight":
                            setScreenHeight = Integer.parseInt(args[1]);
                            break;

                        case "javafx.setScreenTitle":
                            setScreenTitle = args[1];
                            break;

                        case "javafx.setScreenResizeable":
                            setScreenResizeable = args[1].equals("true");
                            break;
                    }
                }
                i = j;
            }
            if(line.replace(" ","").startsWith("main:")) {
                if(!isJavaPrefsCalled) {
                    throw new RuntimeException("Called `main:` before calling `java:`.\nat file: " + file.getAbsolutePath() + " at line: " + i);
                } else {
                    GameEngineMain.scene = new Scene(consoleView, setScreenWeight, setScreenHeight);
                    GameEngineMain.scene.getStylesheets().add(GameEngineMain.class.getResource("style.css").toString());
                    GameEngineMain.window.setResizable(setScreenResizeable);
                    GameEngineMain.window.setFullScreen(isFullScreen);
                    GameEngineMain.window.setTitle(setScreenTitle);
                    GameEngineMain.window.setScene(GameEngineMain.scene);
                    GameEngineMain.window.show();
                    System.setOut(consoleView.getOut());
                    System.setIn(consoleView.getIn());

                    System.out.println("Untitled Text Game Engine v." + GameEngineMain.Version);
                    System.out.println("Made by. choiman1559, under GNU GPL 3.0+ license.");
                    System.out.println();

                    int finalI = i;
                    new Thread((() -> {
                        try {
                            processMainTag(finalI, scriptFileArr);
                        } catch (Exception e) {
                            GameEngineMain.showExceptionThrowDialog(e);
                        }
                    })).start();
                    break;
                }
            }
        }
    }

    private static void processMainTag(int mainStartIndex, String[] main) throws Exception {
        processTags(mainStartIndex + 1, "main" ,main,  "");
    }

    private static int processTags(int mainStartIndex, String TagType , String[] main, @Nullable String... args) throws Exception {
        int var = main.length;
        for(int i = mainStartIndex; (TagType.equals("main") ? i < main.length : !main[i].replace(" ", "").startsWith("}" + TagType)); i++) {
            String line = main[i];
            String lineWithoutSpace = line.replace(" ", "");

            if(lineWithoutSpace.startsWith("while{")) {
                processTags(i + 1, "while", main, args[0]);
                continue;
            }

            if(lineWithoutSpace.startsWith("break")) {
                return -1;
            }

            if(lineWithoutSpace.startsWith("input{")) {
                System.out.print("Command> ");
                Scanner sc = new Scanner(consoleView.getIn());
                String input = sc.nextLine();
                i = processTags(i + 1, "input", main, input);
                continue;
            }

            if(TagType.equals("input") && line.startsWith("case")) {
                if(args[0].equals(subStringBetween(line, "\"", "\""))) {
                    return processTags(i + 1, "case", main, args[0]);
                }
            }

            if(TagType.equals("input") && lineWithoutSpace.startsWith("default{")) {
                return processTags(i + 1, "default", main, args[0]);
            }

            if(lineWithoutSpace.startsWith("print.lines{")) {
                int j = i + 1;
                for(;!main[j].replace(" ","").startsWith("}print.lines");j++) {
                    System.out.println(main[j]);
                }
                i = j;
                continue;
            }

            if(lineWithoutSpace.startsWith("println")) {
                System.out.println(subStringBetween(line,"(", ")"));
                continue;
            }

            if(lineWithoutSpace.startsWith("print")) {
                System.out.print(subStringBetween(line,"(", ")"));
                continue;
            }

            if(lineWithoutSpace.startsWith("clear")) {
                consoleView.clear();
                continue;
            }

            if(lineWithoutSpace.startsWith("exit")) {
                System.exit(Integer.parseInt(subStringBetween(lineWithoutSpace, "(", ")")));
            }

            if(lineWithoutSpace.startsWith("wait")) {
                int waitFor = Integer.parseInt(subStringBetween(lineWithoutSpace, "(", ")"));
                Thread.sleep(waitFor);
            }
            var = i;
        }
        return var;
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
