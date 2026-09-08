package ua.lviv.bas.cinema.common;

import java.util.ArrayList;
import java.util.List;

public final class CacheableList<T> extends ArrayList<T> {

    public CacheableList() {
        super();
    }

    public CacheableList(List<T> source) {
        super(source);
    }
}
