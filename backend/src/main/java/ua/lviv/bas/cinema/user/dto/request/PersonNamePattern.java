package ua.lviv.bas.cinema.user.dto.request;

final class PersonNamePattern {

    static final String REGEX = "^\\p{L}+(?:[ '’-]\\p{L}+)*$";

    private PersonNamePattern() {
    }
}
