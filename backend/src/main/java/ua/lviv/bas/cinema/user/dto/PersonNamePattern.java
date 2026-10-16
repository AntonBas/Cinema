package ua.lviv.bas.cinema.user.dto;

public final class PersonNamePattern {

    public static final String REGEX = "^\\p{L}+(?:[ '’-]\\p{L}+)*$";

    private PersonNamePattern() {
    }
}
