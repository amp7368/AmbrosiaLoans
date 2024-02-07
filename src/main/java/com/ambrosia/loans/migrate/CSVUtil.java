package com.ambrosia.loans.migrate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CSVUtil {

    private static final char SEPARATOR = '\t';

    public static <T> List<T> loadCSV(Class<T> beanType, String fileName) {
        File file = ImportModule.get().getFile(fileName);
        List<T> loaded = new ArrayList<>();
        Gson gson = gson();
        try (BufferedReader csv = new BufferedReader(new FileReader(file))) {
            String[] fullHeader = csv.readLine().split(String.valueOf(SEPARATOR));

            while (true) {
                String line = csv.readLine();
                if (line == null) break;
                String[] values = readLine(fullHeader.length, line);

                T bean = beanType.getConstructor().newInstance();
                for (int i = 0; i < fullHeader.length; i++) {
                    readField(beanType, bean, fullHeader[i], gson, values[i]);
                }

                loaded.add(bean);
            }
            return loaded;
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void readField(Class<T> beanType, T bean, String header, Gson gson, String stringValue)
        throws NoSuchFieldException, IllegalAccessException {
        Field field = beanType.getDeclaredField(header);
        if (stringValue.isBlank()) return;
        if (isIntNumber(field))
            stringValue = stringValue.replace(",", "");
        Object value = gson.fromJson("\"%s\"".formatted(stringValue), field.getType());
        field.trySetAccessible();
        field.set(bean, value);
    }

    @NotNull
    private static String[] readLine(int length, String line) {
        String[] values = new String[length];
        StringBuilder val = new StringBuilder();
        int index = 0;
        for (char c : line.toCharArray()) {
            if (c == SEPARATOR) {
                values[index++] = val.toString();
                val.setLength(0);
            } else
                val.append(c);
        }
        values[index] = val.toString();
        return values;
    }

    private static boolean isIntNumber(Field field) {
        Class<?> type = field.getType();
        return type == int.class ||
            type == long.class ||
            type == double.class ||
            type.isAssignableFrom(Long.class) ||
            type.isAssignableFrom(Double.class) ||
            type.isAssignableFrom(Integer.class);
    }

    @NotNull
    private static Gson gson() {
        return new GsonBuilder()
            .setDateFormat("M/dd/yyyy")
            .create();
    }
}
