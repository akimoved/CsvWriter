package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.writer.annotation.CsvColumn;

import java.util.List;

/**
 * Модель данных для представления студента.
 * Содержит имя студента и список его оценок.
 */
@Data
@Builder
@AllArgsConstructor
public class Student {

    /**
     * Имя студента.
     */
    @CsvColumn(name = "Student Name", order = 1)
    private String name;

    /**
     * Список оценок студента.
     * При записи в CSV оценки будут объединены через точку с запятой (;).
     *
     * <p>Пример: если оценки ["95", "88", "92"], то в CSV будет записано "95;88;92"
     */
    @CsvColumn(name = "Scores", order = 2)
    private List<String> score;
}