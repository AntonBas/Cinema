package ua.lviv.bas.cinema.exception.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ApiSubError.ValidationError.class, name = "validationError")
})
public sealed interface ApiSubError permits ApiSubError.ValidationError {

    record ValidationError(
            String object,
            String field,
            Object rejectedValue,
            String message
    ) implements ApiSubError {

        public ValidationError(String object, String message) {
            this(object, null, null, message);
        }
    }
}