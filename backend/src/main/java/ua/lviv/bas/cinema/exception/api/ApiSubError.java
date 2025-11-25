package ua.lviv.bas.cinema.exception.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = ApiError.ApiValidationError.class, name = "validationError") })
public abstract class ApiSubError {

}