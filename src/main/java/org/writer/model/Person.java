package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.writer.annotation.CsvColumn;

/**
 * Модель данных для представления человека.
 * Содержит информацию об имени, фамилии и дате рождения.
 */
@Data
@Builder
@AllArgsConstructor
public class Person {

    /**
     * Имя человека.
     */
    @CsvColumn(name = "First Name", order = 1)
    private String firstName;

    /**
     * Фамилия человека.
     */
    @CsvColumn(name = "Last Name", order = 2)
    private String lastName;

    /**
     * День рождения (1-31).
     */
    @CsvColumn(name = "Day", order = 3)
    private int dayOfBirth;

    /**
     * Месяц рождения.
     */
    @CsvColumn(name = "Month", order = 4)
    private Months monthOfBirth;

    /**
     * Год рождения.
     */
    @CsvColumn(name = "Year", order = 5)
    private int yearOfBirth;

}
