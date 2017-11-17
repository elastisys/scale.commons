package com.elastisys.scale.commons.rest.auth;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.elastisys.scale.commons.json.types.ErrorType;

public class IsError extends TypeSafeMatcher<ErrorType> {

    private final String message;

    public IsError(String message) {
        this.message = message;
    }

    @Override
    public boolean matchesSafely(ErrorType other) {
        return Objects.equals(this.message, other.getMessage());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("ErrorType with message '%s'", this.message));
    }

    @Factory
    public static <T> Matcher<ErrorType> error(String message) {
        return new IsError(message);
    }
}