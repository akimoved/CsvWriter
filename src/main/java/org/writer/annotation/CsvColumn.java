package org.writer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания полей, которые необходимо сохранить в CSV-файл.
 * Поля без аннотаций не будут сохранены.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {

    /**
     * Название колонки в CSV-файле. Если не указано, будет использовано название поля.
     *
     * @return имя колонки
     */
    String name() default "";

    /**
     * Порядковый номер колонки в CSV-файле, колонки располагаются слева-направо по возрастанию номера.
     *
     * @return порядковый номер колонки
     */
    int order() default 0;
}
