package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class JavaUtils
{
    private JavaUtils()
    {
    }

    // copied from https://stackoverflow.com/a/9797689 and modified
    /**
     * @return linked set of interfaces and super classes in order as found from clazz
     */
    private static Set<Class<?>> getClassesBfs(final Class<?> clazz, final boolean superClasses, final boolean interfaces)
    {
        final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        final Set<Class<?>> nextLevel = new LinkedHashSet<Class<?>>();

        if (!superClasses && !interfaces)
        {
            return classes;
        }

        nextLevel.add(clazz);
        do
        {
            classes.addAll(nextLevel);
            final Set<Class<?>> thisLevel = new LinkedHashSet<Class<?>>(nextLevel);
            nextLevel.clear();
            for (final Class<?> each : thisLevel)
            {
                final Class<?> superClass = each.getSuperclass();
                if (superClasses && superClass != null && superClass != Object.class)
                {
                    nextLevel.add(superClass);
                }
                if (interfaces)
                {
                    for (final Class<?> eachInt : each.getInterfaces())
                    {
                        nextLevel.add(eachInt);
                    }
                }
            }
        } while (!nextLevel.isEmpty());

        return classes;
    }

    /**
     * @return linked list of common interfaces and super classes among given classes in order as found from classes[0]
     */
    public static List<Class<?>> commonSuperClass(final boolean superClasses, final boolean interfaces, final Class<?>... classes)
    {
        if (!superClasses && !interfaces)
        {
            return new LinkedList<Class<?>>();
        }

        // start off with set from first hierarchy
        final Set<Class<?>> rollingIntersect = new LinkedHashSet<Class<?>>(getClassesBfs(classes[0], superClasses, interfaces));
        // intersect with next
        for (int i = 1; i < classes.length; i++)
        {
            rollingIntersect.retainAll(getClassesBfs(classes[i], superClasses, interfaces));
        }
        return new LinkedList<Class<?>>(rollingIntersect);
    }

    /**
     * @return first common super class or Object.class
     */
    public static Class<?> getFirstCommonSuperClass(final Class<?>... classes)
    {
        final List<Class<?>> superClasses = commonSuperClass(true, false, classes);
        return superClasses.isEmpty() ? Object.class : superClasses.get(0);
    }

    /**
     * Delete directory and all files and directories in it.
     * @param path the path of the file to delete.
     * @return true if successful.
     */
    public static boolean deleteDirectory(Path path)
    {
        if (!Files.exists(path))
        {
            return true;
        }

        try
        {
            try (final Stream<Path> paths = Files.list(path))
            {
                paths.forEach(child ->
                {
                    if (Files.isDirectory(child))
                    {
                        deleteDirectory(child);
                    }

                    try
                    {
                        Files.deleteIfExists(child);
                    }
                    catch (Exception e)
                    {
                        Log.getLogger().warn("Failed deleting: " + child, e);
                    }
                });
            }
        }
        catch (IOException e)
        {
            Log.getLogger().warn("Failed deleting: " + path, e);
            return false;
        }

        try
        {
            return Files.deleteIfExists(path);
        }
        catch (IOException e)
        {
            Log.getLogger().warn("Failed deleting: " + path, e);
            return false;
        }
    }

}
