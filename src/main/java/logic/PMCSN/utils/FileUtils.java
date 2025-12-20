package logic.PMCSN.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Collections;  // Per Collections.reverse()

public class FileUtils {

    public static void deleteDirectory(String dir) {
        File root = new File(dir);
        List<File> directories = new ArrayList<>();
        Stack<File> queue = new Stack<>();  // Stack per la coda
        queue.add(root);
        
        while (!queue.isEmpty()) {
            File d = queue.pop();  // Usa pop() invece di removeLast()
            directories.add(d);
            File[] files = d.listFiles();
            if (files != null) {
                List<File> children = new ArrayList<>();  // Usa ArrayList per modificare la lista
                for (File file : files) {
                    children.add(file);
                }
                // Cancella i file
                children.stream().filter(File::isFile).forEach(File::delete);
                // Aggiungi le directory alla coda
                for (File child : children) {
                    if (child.isDirectory()) {
                        queue.add(child);
                    }
                }
            }
        }

        // Inverti l'ordine e cancella le directory
        Collections.reverse(directories);  // Usa Collections.reverse per invertire la lista
        for (File dirToDelete : directories) {
            dirToDelete.delete();
        }
    }

    public static void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}