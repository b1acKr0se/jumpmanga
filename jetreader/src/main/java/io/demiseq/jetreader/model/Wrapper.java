package io.demiseq.jetreader.model;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;

import java.io.File;
import java.util.List;

public class Wrapper implements ParentObject {
    private String name;
    private File imagePath;
    private List<Object> childrenList;

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public void setImagePath(File i) {
        imagePath = i;
    }

    public File getImagePath() {
        return imagePath;
    }

    @Override
    public void setChildObjectList(List<Object> list) {
        childrenList = list;
    }

    @Override
    public List<Object> getChildObjectList() {
        return childrenList;
    }
}
