package io.demiseq.jetreader.utils;

import java.util.Comparator;

import io.demiseq.jetreader.model.Chapter;

/**
 * Created by Thanh on 8/9/2015.
 */
public class ChapterNameComparator implements Comparator<Chapter> {
    private final static AlphanumComparator alphaNum = new AlphanumComparator();

    @Override
    public int compare(Chapter a, Chapter b) {
        return alphaNum.compare(b.getName(), a.getName());
    }
}
