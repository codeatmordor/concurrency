package com.code.mordor.asyncmethod;

import java.util.Optional;

public interface AsyncCallback<T> {

    void onComplete(T value, Optional<Exception> ex);
}
