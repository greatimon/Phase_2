package com.example.jyn.remotemeeting.DataClass;

import android.app.Application;

/**
 * Created by JYN on 2017-11-17.
 */

public class File_info extends Application {

    private String file_name_include_path;
    private String file_name;
    private String file_name_except_format;
    private String file_format;
    private int file_page_count;

    File_info() {}

    public File_info(String file_name_include_path, String file_name, int file_page_count) {
        this.file_name_include_path = file_name_include_path;
        this.file_name = file_name;

        String[] temp_file_name_variable = file_name.split("[.]");

        String temp = "";
        for(int i=0; i<=temp_file_name_variable.length-2; i++) {
            temp += temp_file_name_variable[i];
        }
        this.file_name_except_format = temp;
        this.file_format = temp_file_name_variable[temp_file_name_variable.length-1];

        this.file_page_count = file_page_count;
    }

    public String getFile_name_include_path() {
        return file_name_include_path;
    }

    public void setFile_name_include_path(String file_name_include_path) {
        this.file_name_include_path = file_name_include_path;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_name_except_format() {
        return file_name_except_format;
    }

    public void setFile_name_except_format(String file_name_except_format) {
        this.file_name_except_format = file_name_except_format;
    }

    public String getFile_format() {
        return file_format;
    }

    public void setFile_format(String file_format) {
        this.file_format = file_format;
    }

    public int getFile_page_count() {
        return file_page_count;
    }

    public void setFile_page_count(int file_page_count) {
        this.file_page_count = file_page_count;
    }
}
