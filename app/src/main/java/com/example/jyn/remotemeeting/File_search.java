package com.example.jyn.remotemeeting;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by JYN on 2017-11-17.
 */

public class File_search {

    public static String TAG = File_search.class.getSimpleName();

    public static ArrayList<String> file_search(String request) {

        String mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] format = null;

        String[] source = {
                mRoot + "/Download",
                mRoot + "/KakaoTalkDownload",
                mRoot + "/DCIM"
        };

        if(request.equals("pdf")) {
            format = new String[]{".pdf"};
        }
        else if(request.equals("img")) {
            format = new String[]{".jpg", ".png", ".bmp"};
        }
        else if(request.equals("all")) {
            format = new String[]{".pdf", ".jpg", ".png", ".bmp"};
        }

        ArrayList<String> mFileNames = new ArrayList<String>();
        ArrayList<String> mFolderNames = new ArrayList<String>();

        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        // source: 파일 찾을 path
        // path가 여러개임, 그래서 for문
        for(int j=0; j<source.length; j++) {
            File files = new File(source[j]);

            // 해당 path의 파일이 폴더일때만
            if(files.isDirectory()) {
                File[] fileList = files.listFiles();

                // 해당 폴더의 파일이 존재할때만
                if(fileList.length > 0) {

                    // 해당 폴더안에 있는 파일안에서
                    for(int i = 0 ; i<fileList.length ; i++) {
                        File file = fileList[i];

                        // 또 폴더가 있으면 다시 어레이리스트에 절대경로를 넣어서 저 밑 로직에서 다시 이용
                        if (file.isDirectory()) {
                            try {
                                mFolderNames.add(file.getCanonicalPath());
//                                Log.d(TAG, "folder_name: "+file.getCanonicalPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // 파일이 내가 원하는 format을 가지고 있으면 어레이리스트에 파일이름 추가
                        // format이 여러개임, 그래서 for문
                        for(int k=0; k <format.length; k++) {
                            if(file.getName().endsWith(format[k])) {
                                try {
                                    mFileNames.add(file.getCanonicalPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
//                                Log.d(TAG, format[k] +"_file_name: "+file.getName());
                            }
                        }
                    }

                    // 아까 기본 path의 폴더안에 다시 폴더가 있는 경우, 새로운 어레이리스트에 절대경로를 넣어놨음
                    // 그걸 이용해서 다시 해당 폴더 안에 원하는 format의 파일이 있는지 확인하여, 어레이 리스트에 파일 이름 추가
                    for(int p=0; p<mFolderNames.size(); p++) {
                        File files_2 = new File(mFolderNames.get(p));

                        if(files_2.isDirectory() && !files_2.getName().equals(".thumbnails")) {
                            File[] fileList_2 = files_2.listFiles();

                            if(fileList_2.length > 0) {
                                for(int i = 0 ; i<fileList_2.length ; i++) {
                                    File file = fileList_2[i];

                                    for(int k=0; k <format.length; k++) {
                                        if(file.getName().endsWith(format[k])) {
                                            try {
                                                mFileNames.add(file.getCanonicalPath());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
//                                            Log.d(TAG, format[k] +"_file_name: "+file.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "mFileNames 개수: " + mFileNames.size());
        return mFileNames;
    }
}
