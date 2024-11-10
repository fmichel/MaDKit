package madkit.reflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import madkit.kernel.Agent;
import madkit.kernel.MadkitClassLoader;

import java.util.HashSet;

public class ClassScanner {

    public static <T> List<Class<? extends T>> findSubclasses(Class<T> superClass) throws IOException, ClassNotFoundException {
        List<Class<? extends T>> subclasses = new ArrayList<>();
        Set<String> classNames = getClassNames();
        classNames.removeIf(c -> c.contains("javafx") || 
      		  c.contains("it.uni") || 
      		  c.contains("org.") || 
      		  c.contains("picocli") || 
      		  c.contains("com."));
        System.err.println(classNames);

        for (String className : classNames) {
            try {
               Class<?> clazz = Class.forName(className, false, ClassScanner.class.getClassLoader());
                if (superClass.isAssignableFrom(clazz) 
               		 && !clazz.equals(superClass) 
               		 && !Modifier.isAbstract(clazz.getModifiers())) {
                    subclasses.add((Class<? extends T>) clazz);
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
            	e.printStackTrace();
            }
        }

        return subclasses;
    }

    private static Set<String> getClassNames() throws IOException {
        Set<String> classNames = new HashSet<>();
        String classpath = System.getProperty("jdk.module.path");
        String[] paths = classpath.split(File.pathSeparator);

        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                classNames.addAll(findClassesInDirectory(file, ""));
            } else if (file.getName().endsWith(".jar")) {
                classNames.addAll(findClassesInJar(file));
            }
        }
        

        return classNames;
    }

    private static Set<String> findClassesInDirectory(File directory, String packageName) {
        Set<String> classNames = new HashSet<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classNames.addAll(findClassesInDirectory(file, packageName + file.getName() + "."));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + file.getName().substring(0, file.getName().length() - 6);
                    classNames.add(className);
                }
            }
        }

        return classNames;
    }

    private static Set<String> findClassesInJar(File jarFile) throws IOException {
        Set<String> classNames = new HashSet<>();
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
            Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }

    public static void main(String[] args) {
        try {
            List<Class<? extends Agent>> subclasses = findSubclasses(Agent.class);
            for (Class<? extends Agent> subclass : subclasses) {
                System.out.println(subclass.getName());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
