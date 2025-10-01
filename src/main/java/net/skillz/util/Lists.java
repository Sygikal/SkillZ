package net.skillz.util;

import org.apache.commons.compress.utils.Iterators;

import java.util.ArrayList;
import java.util.Iterator;

public class Lists {
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> iterator) {
        ArrayList<E> list = newArrayList();
        Iterators.addAll(list, iterator);
        return list;
    }

    private Lists() {
    }
}
