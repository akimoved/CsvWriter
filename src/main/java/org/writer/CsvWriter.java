package org.writer;

import org.writer.annotation.CsvColumn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса Writable для записи объектов в CSV формат.
 * Использует рефлексию для извлечения значений полей из объектов и аннотации для настройки вывода.
 */
public class CsvWriter implements Writable {

    private static final String DELIMITER = ",";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Записывает список объектов в CSV файл.
     * В выходной файл будут включены только поля, аннотированные @CsvColumn.
     *
     * @param data список объектов для записи
     * @param fileName имя выходного файла
     * @throws IllegalArgumentException если data равен null или пустой
     * @throws RuntimeException если произошла ошибка при записи файла
     */
    @Override
    public void writeToFile(List<?> data, String fileName) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be null or empty");
        }

        Class<?> clazz = data.get(0).getClass();
        List<FieldInfo> fields = getAnnotatedFields(clazz);

        if (fields.isEmpty()) {
            throw new IllegalArgumentException("No @CsvColumn annotated fields found in class " + clazz.getName());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Записываем заголовок
            String header = fields.stream()
                    .map(FieldInfo::columnName)
                    .collect(Collectors.joining(DELIMITER));
            writer.write(header);
            writer.write(LINE_SEPARATOR);

            // Записываем строки данных
            for (Object obj : data) {
                String row = fields.stream()
                        .map(fieldInfo -> getFieldValue(obj, fieldInfo.field()))
                        .map(this::formatValue)
                        .collect(Collectors.joining(DELIMITER));
                writer.write(row);
                writer.write(LINE_SEPARATOR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + fileName, e);
        }
    }

    /**
     * Получает все поля, аннотированные @CsvColumn, из класса.
     * Поля сортируются по атрибуту order.
     *
     * @param clazz класс для анализа
     * @return список информации о полях, отсортированный по order
     */
    private List<FieldInfo> getAnnotatedFields(Class<?> clazz) {
        List<FieldInfo> fieldInfos = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(CsvColumn.class)) {
                CsvColumn annotation = field.getAnnotation(CsvColumn.class);
                String columnName = annotation.name().isEmpty() ? field.getName() : annotation.name();
                fieldInfos.add(new FieldInfo(field, columnName, annotation.order()));
            }
        }

        // Сортируем по order
        fieldInfos.sort(Comparator.comparingInt(FieldInfo::order));
        return fieldInfos;
    }

    /**
     * Извлекает значение поля из объекта с использованием рефлексии.
     *
     * @param obj объект, из которого извлекается значение
     * @param field поле для извлечения
     * @return значение поля, или null если извлечение не удалось
     */
    private Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accessing field: " + field.getName(), e);
        }
    }

    /**
     * Форматирует значение для вывода в CSV.
     * Обрабатывает null значения, коллекции и специальные символы, требующие экранирования.
     *
     * @param value значение для форматирования
     * @return отформатированное строковое значение
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }

        // Обрабатываем коллекции
        if (value instanceof Collection<?> collection) {
            String joined = collection.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";"));
            return escapeValue(joined);
        }

        // Обрабатываем enum
        if (value instanceof Enum<?>) {
            return value.toString();
        }

        return escapeValue(value.toString());
    }

    /**
     * Экранирует специальные символы в CSV значениях.
     * Оборачивает значения в кавычки, если они содержат запятые, кавычки или переносы строк.
     *
     * @param value значение для экранирования
     * @return экранированное значение
     */
    private String escapeValue(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Экранируем кавычки путем их удвоения
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return value;
    }

    /**
     * Внутренний класс для хранения метаданных поля.
     */
    private record FieldInfo(Field field, String columnName, int order) {
    }
}