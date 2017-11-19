package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.Activity.Register_file_to_project_A;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.File_search;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.R;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-11-17.
 */

public class RecyclerView_adapter extends RecyclerView.Adapter<RecyclerView_adapter.ViewHolder> {

    private Context context;
    private ArrayList<File_info> files;
    private static ArrayList<String> checked_files;
    private int itemLayout;
    private String request;
    public static String TAG = RecyclerView_adapter.class.getSimpleName();

    /** RecyclerAdapter 생성자 */
    public RecyclerView_adapter(Context context, int itemLayout, final String request) {
        Log.d(TAG, "ViewHolder_ RecyclerView_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.request = request;

        files = new ArrayList<>();
        checked_files = new ArrayList<>();

        if(request.equals("project")) {
//            add_plus_icon();
            add_from_shared();
        }
        /** 로컬 파일 arrayList에 add */
        else if (!request.equals("project")) {
            Log.d(TAG, "request: " + request);

            ArrayList<String> mFileNames = File_search.file_search(request);

            File_info_create_and_add(mFileNames);
        }

        /** 테스트 - 어레이 더미 코드 */
//        for (int i = 0; i < 15; i++) {
//            addItem(i);
//        }
    }

    /** 뷰홀더 */
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_img)
        ImageView file_img;
        @BindView(R.id.file_name)
        TextView file_name;
        @BindView(R.id.list_item_container)
        LinearLayout list_item_container;
        @BindView(R.id.check_mark)
        ImageView check_mark;

        boolean check;

        public ViewHolder(View itemView, int itemLayout, final String request) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this,itemView);

            Log.d(TAG, "request: "+ request);

            /** 아이템 클릭 이벤트 설정 */
            list_item_container.setClickable(true);
            list_item_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    if(request.equals("project") && pos == 0) {
                        Intent intent = new Intent(context, Register_file_to_project_A.class);
                        ((Call_A)context).startActivityForResult(intent, Call_A.REQUEST_GET_LOCAL_FILE);
                    }

                    else if(!request.equals("project") && !check) {
                        check_mark.setVisibility(View.VISIBLE);
                        checked_files.add(files.get(pos).getFile_name_include_path());
                        check = true;
                        Log.d(TAG, "checked_files 크기: " + checked_files.size());

                        if(checked_files.size() > 0) {
                            Call_F.add_files.setImageResource(R.drawable.add_text_1);
                        }
                    }
                    else if(!request.equals("project") && check) {
                        check_mark.setVisibility(View.GONE);

                        for(int i=0; i<checked_files.size(); i++) {
                            if(checked_files.get(i).equals(files.get(pos).getFile_name_include_path())) {
                                checked_files.remove(i);
                                break;
                            }
                        }
                        check = false;
                        Log.d(TAG, "checked_files 크기: " + checked_files.size());

                        if(checked_files.size() == 0) {
                            Call_F.add_files.setImageResource(R.drawable.add_text_2);
                        }
                    }

                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "getFile_name_include_path: " + files.get(pos).getFile_name_include_path());
                    Log.d(TAG, "getFile_name: " + files.get(pos).getFile_name());
                    Log.d(TAG, "getFile_name_except_format: " + files.get(pos).getFile_name_except_format());
                    Log.d(TAG, "getFile_format: " + files.get(pos).getFile_format());

                    ask_share();
                }
            });
        }
    }



    /** onCreateViewHolder => 뷰홀더 생성 - 인플레이팅 */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, itemLayout, request);

        return viewHolder;
    }

    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        String fileFormat = null;
        String file_name_except_format = null;

        fileFormat = files.get(position).getFile_format();
        file_name_except_format = files.get(position).getFile_name_except_format();
        Log.d(TAG, "fileFormat: " + fileFormat);
        Log.d(TAG, "fileName: " + file_name_except_format);

        if(fileFormat.equals("zero")) {
            holder.file_img.setImageResource(R.drawable.add_file_2);
//            holder.file_name.setText(file_name_except_format);
        }

//        else {
            if(fileFormat.equals("pdf")) {
                holder.file_img.setImageResource(R.drawable.pdf);
//                Glide.with(context).load(R.drawable.pdf).diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .into(holder.file_img);
            }
            else if(fileFormat.equals("jpg")) {
                holder.file_img.setImageResource(R.drawable.jpg);
//                Glide.with(context).load(R.drawable.jpg).diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .into(holder.file_img);
            }
            else if(fileFormat.equals("png")) {
                holder.file_img.setImageResource(R.drawable.png);
//                Glide.with(context).load(R.drawable.png).diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .into(holder.file_img);
            }
            holder.file_name.setText(file_name_except_format);
//        }
    }

    @Override
    public int getItemCount() {
        if(itemLayout == R.layout.i_file) {
            return files.size();
        }
        else {
            return 0;
        }
    }

    // 공유 할지 물어보는 메소드
    private void ask_share() {
    }

    /**---------------------------------------------------------------------------
     메소드 ==> File_info 객체 생성하여 ary에 add하기
     ---------------------------------------------------------------------------*/
    public void File_info_create_and_add(ArrayList<String> mFileNames) {
        String content;
        for(int i=0; i<mFileNames.size(); i++) {
            content = mFileNames.get(i);
            Log.d(TAG, "content: " + content);

            String[] temp_content = content.split("[/]");
            String file_name = temp_content[temp_content.length-1];
            Log.d(TAG, "file_name: " + file_name);

            files.add(i, new File_info(content, file_name, 0));
            notifyItemInserted(i);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파일 추가 버튼 arrayList add
     ---------------------------------------------------------------------------*/
    public void add_plus_icon() {
        Log.d(TAG, "파일 추가 버튼 들어옴");
        files.add(0, new File_info("absolutePath/파일추가.zero", "파일추가.zero" , 0));
        notifyItemInserted(0);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 체크된 파일 회의 파일함 리스트에 추가하기 -- sharedPreference에 저장하기
     ---------------------------------------------------------------------------*/
    public void save_to_shared() {

        // auto_increament 수동 구동
        SharedPreferences auto_increament = context.getSharedPreferences("auto_increament", Context.MODE_PRIVATE);
        int auto_int = auto_increament.getInt("auto", 0);
        SharedPreferences.Editor edit_auto_increament = auto_increament.edit();
        Log.d(TAG, "checked_files.size(): " + checked_files.size());
        Log.d(TAG, "auto_increament: " + auto_int);

        // TODO: meeting_num ==> 서버에 DB컬럼으로 입력되는 primary key -- 지금은 방 한개라 특별히 구분 없음
        SharedPreferences meeting_num = context.getSharedPreferences("meeting_num", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit_meeting_num = meeting_num.edit();

        if(checked_files.size() > 0) {
            // 해당 파일의 CanonicalPath 넣기
            int count_checked_files = 0;
            for(int i=auto_int+1; i<=checked_files.size()+auto_int; i++) {
                edit_meeting_num.putString("CanonicalPath" + String.valueOf(i-1), checked_files.get(count_checked_files)).apply();
                count_checked_files++;
            }

            // 임시용 오토 인크리먼트 값 넣기
            int howMany = meeting_num.getAll().size();
            edit_auto_increament.putInt("auto", howMany).apply();
            Log.d(TAG, "howMany: " + howMany);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 로컬파일에서 파일을 선택한 뒤 새로 어댑터를 생성했을 때 동작하는 메소드
     ---------------------------------------------------------------------------*/
    public void add_from_shared() {
//        if(Call_F.from_files_select) {
//            Log.d(TAG, "files_from_shared(): 실행");
            files_from_shared();
//            Call_F.from_files_select = false;
//        }
    }



    /**---------------------------------------------------------------------------
     메소드 ==> sharedPreference에 저장된 회의 파일 리스트, arrayList에 add 하기
     ---------------------------------------------------------------------------*/
    public void files_from_shared() {

        files.clear();
        Log.d(TAG, "files.size(): "+files.size());

        SharedPreferences meeting_num = context.getSharedPreferences("meeting_num", Context.MODE_PRIVATE);

        int howMany = meeting_num.getAll().size();
        Log.d(TAG, "howMany: " + howMany);

        String content;

        if(howMany>0) {
            for(int i=0; i<howMany; i++) {
                content = meeting_num.getString("CanonicalPath" + String.valueOf(i), "");
                Log.d(TAG, "content: " + content);

                String[] temp_content = content.split("[/]");
                String file_name = temp_content[temp_content.length-1];
                Log.d(TAG, "file_name: " + file_name);

                files.add(i, new File_info(content, file_name, 0));
                notifyItemInserted(i);

                if(i==howMany-1) {
                    // 파일 추가 버튼, 추가
                    Log.d(TAG, "파일 추가 버튼 들어옴");
                    files.add(0, new File_info("absolutePath/파일추가.zero", "파일추가.zero" , 0));
                    notifyItemInserted(0);
                }
                Log.d(TAG, "files.size(): "+files.size());
            }
        }
        if(files.size()==0) {
            // 파일 추가 버튼, 추가
            Log.d(TAG, "파일 추가 버튼 들어옴");
            files.add(0, new File_info("absolutePath/파일추가.zero", "파일추가.zero" , 0));
            notifyItemInserted(0);
        }

        for(int j=0; j<files.size(); j++) {
            Log.d(TAG, "=====================");
            Log.d(TAG, files.get(j).getFile_name_include_path());
        }
    }












    /** 더미 파일 생성 메소드 */
    public void addItem(int position) {

        if(position == 0) {
            files.add(position, new File_info(
                    "absolutePath_" + String.valueOf(position),
                    "파일 추가" + ".zero" ,
                    new Random().nextInt(7)));
            notifyItemInserted(position);
        }

        else {
            int random = new Random().nextInt(3);
            String format;
            if(random == 0) {
                format = ".pdf";
            }
            else if(random == 1) {
                format = ".jpg";
            }
            else if(random == 2) {
                format = ".png";
            }
            else {
                format = ".jpg";
            }

            files.add(position, new File_info(
                    "absolutePath_" + String.valueOf(position),
                    "file_name_file_name_file_name_file_name"+ String.valueOf(position) + format ,
                    new Random().nextInt(7)));
            notifyItemInserted(position);
        }

    }
}
