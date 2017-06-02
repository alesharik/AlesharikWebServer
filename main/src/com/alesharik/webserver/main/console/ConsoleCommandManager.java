package com.alesharik.webserver.main.console;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.logger.Prefixes;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@ClassPathScanner
@Prefixes({"[ConsoleCommand]", "[ConsoleCommandManager]"})
public final class ConsoleCommandManager {
    private static final Map<String, ConsoleCommand> commands = new ConcurrentHashMap<>();

    @ListenInterface(ConsoleCommand.class)
    public static void listenCommand(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            ConsoleCommand instance = (ConsoleCommand) constructor.newInstance();
            commands.put(instance.getName(), instance);
        } catch (NoSuchMethodException e) {
            System.err.println("Class " + clazz.getCanonicalName() + " doesn't have empty constructor!");
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            System.err.println("Exception in class " + clazz.getCanonicalName());
            e.printStackTrace();
        }
    }

    @Nonnull
    public static Set<ConsoleCommand> getCommands() {
        return new HashSet<>(commands.values());
    }

    @Nullable
    public static ConsoleCommand getCommand(String name) {
        return commands.get(name);
    }

    public static boolean containsCommand(String name) {
        return commands.containsKey(name);
    }
}
