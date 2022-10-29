package ru.avdeev.marketsimpleapi.exceptions;

public class InsertWithIdException extends ApiException{

    public InsertWithIdException() {
        super("Can not insert entity with selected id.");
    }
}
