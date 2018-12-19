package sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

public class Plugins {

    private static ArrayList<Path> plugins;

    private static ArrayList<List<String>> allKeywords;

    public static ArrayList<Path> getPlugins(String pathToFolder) {
        plugins = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(pathToFolder))) {
            paths.filter(Files::isRegularFile).forEach(i -> plugins.add(i));
        }
        catch(IOException ex) {
            System.out.println("IOException handled 1");
        }
        return plugins;
    }

    public static ArrayList<List<String>> getKeywords() {
        allKeywords = new ArrayList<>();
        List<String> list;
        Path path;
        try {
            for(Path i: Plugins.getPlugins(StringConstants.pathToFolder)) {
                list = new Vector<>();
                path = Paths.get(StringConstants.pathToFolder + "\\" + i.getFileName());
                list.add(i.getFileName().toString().substring(0, i.getFileName().
                        toString().lastIndexOf(".")) + ":");
                list.addAll(Files.readAllLines(path));
                allKeywords.add(list);
            }
        }
        catch (IOException ex) {
            System.out.println("IOException handled 2");
        }
        return allKeywords;
    }
}
