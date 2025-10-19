package org.writer;

import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Главный класс для демонстрации функциональности CSV Writer.
 * Содержит примеры записи различных типов данных в CSV формат.
 */
public class Main {
    /**
     * Точка входа в приложение.
     * Демонстрирует три примера использования CsvWriter:
     * 1. Запись данных о людях
     * 2. Запись данных студентов с оценками
     * 3. Запись данных со специальными символами
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {

        // Удаление тестовых файлов в начале выполнения
        deleteFileIfExists("people.csv");
        deleteFileIfExists("students.csv");
        deleteFileIfExists("special_cases.csv");

        CsvWriter writer = new CsvWriter();

        // Пример 1: Запись объектов Person в CSV
        System.out.println("\nПример 1: Записываем данные о людях...");
        List<Person> people = Arrays.asList(
                Person.builder()
                        .firstName("Иван")
                        .lastName("Иванов")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1990)
                        .build(),
                Person.builder()
                        .firstName("Мария")
                        .lastName("Смирнова")
                        .dayOfBirth(23)
                        .monthOfBirth(Months.JULY)
                        .yearOfBirth(1985)
                        .build(),
                Person.builder()
                        .firstName("Петр")
                        .lastName("Петров")
                        .dayOfBirth(8)
                        .monthOfBirth(Months.DECEMBER)
                        .yearOfBirth(1992)
                        .build()
        );

        writer.writeToFile(people, "people.csv");
        System.out.println("Данные о людях записаны в файл people.csv");

        // Пример 2: Запись объектов Student с коллекциями в CSV
        System.out.println("\nПример 2: Записываем данные студентов с оценками...");
        List<Student> students = Arrays.asList(
                Student.builder()
                        .name("Алиса Иванова")
                        .score(Arrays.asList("95", "88", "92", "90"))
                        .build(),
                Student.builder()
                        .name("Борис Сидоров")
                        .score(Arrays.asList("78", "85", "80", "82"))
                        .build(),
                Student.builder()
                        .name("Вера Кузнецова")
                        .score(Arrays.asList("92", "94", "89", "96"))
                        .build()
        );

        writer.writeToFile(students, "students.csv");
        System.out.println("Данные студентов записаны в файл students.csv");

        // Пример 3: Запись данных со специальными символами
        System.out.println("\nПример 3: Записываем данные со специальными символами...");
        List<Person> specialCases = Arrays.asList(
                Person.builder()
                        .firstName("Дэвид")
                        .lastName("О'Лири")
                        .dayOfBirth(2)
                        .monthOfBirth(Months.MAY)
                        .yearOfBirth(1958)
                        .build(),
                Person.builder()
                        .firstName("Габриэль")
                        .lastName("Гарсиа-Маркес")
                        .dayOfBirth(6)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1927)
                        .build()
        );

        writer.writeToFile(specialCases, "special_cases.csv");
        System.out.println("Специальные случаи записаны в файл special_cases.csv");

        System.out.println("\nВсе примеры успешно выполнены!");
    }

    /**
     * Удаляет файл, если он существует.
     *
     * @param filename имя файла для удаления
     */
    private static void deleteFileIfExists(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Удален существующий файл: " + filename);
            } else {
                System.out.println("Не удалось удалить файл: " + filename);
            }
        }
    }
}