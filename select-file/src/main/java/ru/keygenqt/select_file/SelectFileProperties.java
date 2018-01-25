package ru.keygenqt.select_file;

import java.util.ArrayList;
import java.util.List;

public class SelectFileProperties {

    public boolean choosePhoto = true;
    public int max_files_count = 1;
    public int max_files_size = 5000000; // 5M
    public List<String> mimes = new ArrayList<>();
    public List<String> selected = new ArrayList<>();

    public SelectFileProperties setMimes(List<String> val) {
        this.mimes = val;
        return this;
    }

    public SelectFileProperties setSelected(List<String> val) {
        this.selected = val;
        return this;
    }

    public SelectFileProperties setMaxFilesCount(int val) {
        this.max_files_count = val;
        return this;
    }

    public SelectFileProperties setMaxFilesSize(int val) {
        this.max_files_size = val;
        return this;
    }

    public SelectFileProperties setChoosePhoto(boolean val) {
        this.choosePhoto = val;
        return this;
    }
}
