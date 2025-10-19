package org.writer;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Включает базовые тесты функциональности CsvWriter и тесты с генерацией данных через Datafaker.
 */
class CsvWriterTest {

    private CsvWriter csvWriter;
    private Faker faker;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvWriter = new CsvWriter();
        faker = new Faker();
    }

    // ============================================
    // Базовые тесты функциональности
    // ============================================

    /**
     * Проверяет запись простых объектов Person в CSV файл и корректность заголовков и данных.
     */
    @Test
    void testWriteSimplePersonData() throws IOException {
        List<Person> people = Arrays.asList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1990)
                        .build(),
                Person.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .dayOfBirth(23)
                        .monthOfBirth(Months.JULY)
                        .yearOfBirth(1985)
                        .build()
        );

        Path outputFile = tempDir.resolve("people.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size()); // Заголовок + 2 строки данных

        // Проверяем заголовок
        assertEquals("First Name,Last Name,Day,Month,Year", lines.get(0));

        // Проверяем строки данных
        assertEquals("John,Doe,15,MARCH,1990", lines.get(1));
        assertEquals("Jane,Smith,23,JULY,1985", lines.get(2));
    }

    /**
     * Проверяет запись объектов Student с коллекциями.
     * Коллекции должны объединяться через точку с запятой.
     */
    @Test
    void testWriteStudentDataWithCollections() throws IOException {
        List<Student> students = Arrays.asList(
                Student.builder()
                        .name("Alice")
                        .score(Arrays.asList("95", "88", "92"))
                        .build(),
                Student.builder()
                        .name("Bob")
                        .score(Arrays.asList("78", "85", "80"))
                        .build()
        );

        Path outputFile = tempDir.resolve("students.csv");

        csvWriter.writeToFile(students, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size());

        // Проверяем заголовок
        assertEquals("Student Name,Scores", lines.get(0));

        // Проверяем данные - коллекции должны быть объединены через точку с запятой
        assertEquals("Alice,95;88;92", lines.get(1));
        assertEquals("Bob,78;85;80", lines.get(2));
    }

    /**
     * Проверяет экранирование специальных символов.
     * Значения с запятыми должны оборачиваться в кавычки.
     */
    @Test
    void testEscapeSpecialCharacters() throws IOException {
        List<Person> people = Collections.singletonList(
                Person.builder()
                        .firstName("O'Brien")
                        .lastName("Smith, Jr.")
                        .dayOfBirth(1)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        Path outputFile = tempDir.resolve("special.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());

        // Фамилия содержит запятую, должна быть в кавычках
        assertTrue(lines.get(1).contains("\"Smith, Jr.\""));
    }

    /**
     * Проверяет преобразование null значений в пустые строки.
     */
    @Test
    void testHandleNullValues() throws IOException {
        List<Person> people = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName(null) // null значение
                        .dayOfBirth(15)
                        .monthOfBirth(null) // null enum
                        .yearOfBirth(1990)
                        .build()
        );

        Path outputFile = tempDir.resolve("nulls.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());

        // Null значения должны быть представлены пустыми строки
        String dataRow = lines.get(1);
        String[] values = dataRow.split(",", -1); // -1, чтобы сохранить пустые строки
        assertEquals("John", values[0]);
        assertEquals("", values[1]); // null lastName
        assertEquals("15", values[2]);
        assertEquals("", values[3]); // null month
        assertEquals("1990", values[4]);
    }

    /**
     * Проверяет, что пустой список данных вызывает исключение.
     */
    @Test
    void testEmptyDataListThrowsException() {
        List<Person> emptyList = Collections.emptyList();
        Path outputFile = tempDir.resolve("empty.csv");

        assertThrows(IllegalArgumentException.class, () -> csvWriter.writeToFile(emptyList, outputFile.toString()));
    }

    /**
     * Проверяет, что null список данных вызывает исключение.
     */
    @Test
    void testNullDataListThrowsException() {
        Path outputFile = tempDir.resolve("null.csv");

        assertThrows(IllegalArgumentException.class, () -> csvWriter.writeToFile(null, outputFile.toString()));
    }

    /**
     * Проверяет, что порядок полей соблюдается согласно атрибуту order в аннотации.
     */
    @Test
    void testFieldOrderingIsRespected() throws IOException {
        List<Person> people = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1990)
                        .build()
        );

        Path outputFile = tempDir.resolve("order.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        String header = lines.get(0);

        // Поля должны быть в порядке, указанном в @CsvColumn(order=...)
        // Person: firstName(1), lastName(2), dayOfBirth(3), monthOfBirth(4), yearOfBirth(5)
        assertEquals("First Name,Last Name,Day,Month,Year", header);
    }

    /**
     * Проверяет использование кастомных имен колонок из аннотации.
     */
    @Test
    void testCustomColumnNames() throws IOException {
        List<Person> people = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1990)
                        .build()
        );

        Path outputFile = tempDir.resolve("custom.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);

        // Проверяем, что используются кастомные имена из @CsvColumn(name=...)
        assertTrue(lines.get(0).contains("First Name"));
        assertTrue(lines.get(0).contains("Last Name"));
    }

    /**
     * Проверяет обработку пустых коллекций в полях.
     */
    @Test
    void testEmptyCollectionInField() throws IOException {
        List<Student> students = Collections.singletonList(
                Student.builder()
                        .name("Alice")
                        .score(Collections.emptyList())
                        .build()
        );

        Path outputFile = tempDir.resolve("empty_collection.csv");

        csvWriter.writeToFile(students, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertEquals("Alice,", lines.get(1)); // Пустая коллекция превращается в пустую строку
    }

    /**
     * Проверяет работу с различными значениями enum.
     */
    @Test
    void testMultipleEnumValues() throws IOException {
        List<Person> people = Arrays.asList(
                Person.builder()
                        .firstName("January")
                        .lastName("Person")
                        .dayOfBirth(1)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(2000)
                        .build(),
                Person.builder()
                        .firstName("December")
                        .lastName("Person")
                        .dayOfBirth(31)
                        .monthOfBirth(Months.DECEMBER)
                        .yearOfBirth(2000)
                        .build()
        );

        Path outputFile = tempDir.resolve("enums.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.get(1).contains("JANUARY"));
        assertTrue(lines.get(2).contains("DECEMBER"));
    }

    /**
     * Проверяет экранирование кавычек в данных.
     * Кавычки должны удваиваться и значение должно оборачиваться в кавычки.
     */
    @Test
    void testQuotesInData() throws IOException {
        List<Person> people = Collections.singletonList(
                Person.builder()
                        .firstName("John \"Johnny\"")
                        .lastName("Doe")
                        .dayOfBirth(1)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        Path outputFile = tempDir.resolve("quotes.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        String dataRow = lines.get(1);

        // Кавычки должны быть экранированы удвоением и обернуты в кавычки
        assertTrue(dataRow.contains("\"John \"\"Johnny\"\"\""));
    }

    // ============================================
    // Тесты с использованием Datafaker
    // ============================================

    /**
     * Проверяет запись большого набора данных (100 записей) сгенерированных через Datafaker.
     */
    @Test
    void testLargeDataSetWithDatafaker() throws IOException {
        // Генерируем 100 случайных людей используя Datafaker
        List<Person> people = IntStream.range(0, 100)
                .mapToObj(i -> createPersonWithFaker(faker))
                .toList();

        Path outputFile = tempDir.resolve("large_dataset.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(101, lines.size()); // Заголовок + 100 строк данных

        // Проверяем наличие заголовка
        assertEquals("First Name,Last Name,Day,Month,Year", lines.get(0));

        // Проверяем, что все строки имеют правильное количество колонок
        for (int i = 1; i < lines.size(); i++) {
            String[] columns = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            assertEquals(5, columns.length, "Строка " + i + " должна иметь 5 колонок");
        }
    }

    /**
     * Проверяет генерацию студентов с оценками через Datafaker.
     */
    @Test
    void testDatafakerStudents() throws IOException {
        List<Student> students = IntStream.range(0, 50)
                .mapToObj(i -> Student.builder()
                        .name(faker.name().fullName())
                        .score(IntStream.range(0, 4)
                                .mapToObj(j -> String.valueOf(faker.number().numberBetween(60, 100)))
                                .toList())
                        .build())
                .toList();

        Path outputFile = tempDir.resolve("students_datafaker.csv");

        csvWriter.writeToFile(students, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(51, lines.size()); // Заголовок + 50 студентов

        // Проверяем, что оценки правильно форматированы (разделены точкой с запятой)
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            assertTrue(line.contains(";") || line.matches(".*,\\d+$"),
                    "Строка должна содержать оценки, разделенные точкой с запятой");
        }
    }

    /**
     * Проверяет генерацию реалистичных персональных данных.
     */
    @Test
    void testGenerateRealisticPersonData() throws IOException {
        // Генерируем реалистичные данные людей
        List<Person> people = IntStream.range(0, 20)
                .mapToObj(i -> createPersonWithFaker(faker))
                .toList();

        Path outputFile = tempDir.resolve("realistic_people.csv");

        csvWriter.writeToFile(people, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(21, lines.size());

        // Проверяем структуру реалистичных данных
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            assertNotNull(parts[0]); // firstName
            assertNotNull(parts[1]); // lastName
            assertTrue(Integer.parseInt(parts[2]) >= 1 && Integer.parseInt(parts[2]) <= 28); // day
            assertTrue(parts[3].matches("[A-Z]+")); // month enum
            assertTrue(Integer.parseInt(parts[4]) >= 1970 && Integer.parseInt(parts[4]) <= 2000); // year
        }
    }

    /**
     * Проверяет генерацию студентов со случайными оценками.
     */
    @Test
    void testGenerateStudentsWithRandomScores() throws IOException {
        // Генерируем студентов со случайными тестовыми оценками
        List<Student> students = IntStream.range(0, 30)
                .mapToObj(i -> {
                    // Генерируем 3-5 оценок на студента
                    int numScores = faker.number().numberBetween(3, 6);
                    List<String> scores = IntStream.range(0, numScores)
                            .mapToObj(j -> String.valueOf(faker.number().numberBetween(50, 100)))
                            .toList();

                    return Student.builder()
                            .name(faker.name().fullName())
                            .score(scores)
                            .build();
                })
                .toList();

        Path outputFile = tempDir.resolve("students_random_scores.csv");

        csvWriter.writeToFile(students, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(31, lines.size());

        // Проверяем формат оценок
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",", 2);
            String scoresStr = parts[1];

            // Оценки должны быть разделены точкой с запятой
            String[] scores = scoresStr.split(";");
            assertTrue(scores.length >= 3 && scores.length <= 5);

            for (String score : scores) {
                int scoreValue = Integer.parseInt(score.trim());
                assertTrue(scoreValue >= 50 && scoreValue <= 100);
            }
        }
    }

    /**
     * Тест производительности с большим набором данных (1000 записей).
     */
    @Test
    void testPerformanceWithLargeDataset() throws IOException {
        // Большой набор данных (1000 записей)
        long startTime = System.currentTimeMillis();

        List<Person> largeDataset = IntStream.range(0, 1000)
                .mapToObj(i -> createPersonWithFaker(faker))
                .toList();

        Path outputFile = tempDir.resolve("large_performance_test.csv");

        csvWriter.writeToFile(largeDataset, outputFile.toString());

        // Отсчитываем время выполнения записи
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1001, lines.size()); // Заголовок + 1000 записей

        // Проверка производительности - должно завершиться за разумное время (< 5 секунд)
        assertTrue(duration < 5000, "Запись 1000 записей должна занимать менее 5 секунд, заняло: " + duration + "мс");

        System.out.println("Тест производительности: 1000 записей записано за " + duration + "мс");
    }

    /**
     * Вспомогательный метод для создания Person используя конкретный экземпляр Faker.
     *
     * @param faker экземпляр Faker
     * @return сгенерированный объект Person
     */
    private Person createPersonWithFaker(Faker faker) {
        return Person.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .dayOfBirth(faker.number().numberBetween(1, 28))
                .monthOfBirth(Months.values()[faker.number().numberBetween(0, 12)])
                .yearOfBirth(faker.number().numberBetween(1970, 2000))
                .build();
    }
}