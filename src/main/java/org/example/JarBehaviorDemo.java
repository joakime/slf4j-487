package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JarBehaviorDemo
{
    public static void main(String[] args) throws IOException
    {
        Path userHomePath = Paths.get(System.getProperty("user.home"));
        Path localMavenRepoPath = userHomePath.resolve(".m2/repository/");
        Path slf4jApiPath = localMavenRepoPath.resolve("org/slf4j/slf4j-api/2.0.0-alpha1/slf4j-api-2.0.0-alpha1.jar");

        if (!Files.exists(slf4jApiPath))
        {
            throw new FileNotFoundException("Unable to find slf4j-api: " + slf4jApiPath);
        }

        URI slf4jUri = URI.create("jar:" + slf4jApiPath.toUri().toASCIIString());

        demoJarBehavior(slf4jUri, "DEFAULT", null);
        demoJarBehavior(slf4jUri, "RUNTIME", "runtime");
        demoJarBehavior(slf4jUri, "JAVA11", 11);
        demoJarBehavior(slf4jUri, "JAVA14", Runtime.Version.parse("14"));
    }

    public static void demoJarBehavior(URI slf4jUri, String title, Object multiReleaseEnvSetting)
    {
        System.out.println("-------------------------------------");
        System.out.printf("-- JAR Behavior - %s%n", title);

        // JarFs environment behavior
        Map<String, Object> env = new HashMap<>();
        if (multiReleaseEnvSetting != null)
            env.put("multi-release", multiReleaseEnvSetting);

        try (FileSystem jarFs = FileSystems.newFileSystem(slf4jUri, env))
        {
            Path root = jarFs.getRootDirectories().iterator().next();
            Files.walk(root)
                .filter(Files::isRegularFile)
                // comment next line to see all files
                .filter((p)->p.getFileName().toString().contains("module-info"))
                .sorted()
                .forEach((path) ->
            {
                try
                {
                    System.out.printf("%s - %,d b%n", path, Files.size(path));
                }
                catch (IOException e)
                {
                    System.out.printf("%s - (-1) b%n", path);
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
