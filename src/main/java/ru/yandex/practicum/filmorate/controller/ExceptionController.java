package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Set<ErrorResponse> handleValidationExceptions(
            final MethodArgumentNotValidException ex) {
        Set<ErrorResponse> responseSet = new HashSet<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            ErrorResponse errorResponse = new ErrorResponse(fieldName, errorMessage);
            responseSet.add(errorResponse);
        });
        return responseSet;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleMyValidationExceptions(
            final ValidationException ex) {
        return new ErrorResponse("Ошибка валидации", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorResponse handleNFExceptions(
            final NotFoundException ex) {
        return new ErrorResponse("Объект не найден", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResponse handleGeneralException(
            final Exception ex) {
        return new ErrorResponse("Произошло исключение", ex.getMessage());
    }
}
