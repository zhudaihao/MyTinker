package cn.wqgallery.mytinker;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cn.wqgallery.mytinker.utils.FileUtils;
import cn.wqgallery.mytinker.utils.RestoreDexUtils;

public class TextActivity extends BaseActivity {
    private ImageView iv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        iv=findViewById(R.id.iv);
        iv.setImageResource(R.mipmap.t);

    }

    public void count(View view) {
        TextCount textCount = new TextCount();
        textCount.add();
        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
    }

    //修复
    public void restore(View view) {
        //1 下载修复好的dex文件到SD卡（测试就手动拷贝到sd卡里面了）

        //2 把下载好的dex文件复制到私有目录里面

        //获取sd卡里面刚下载的dex文件 (class2.dex是刚下载时保存的文件名)
        File sourcesFile = new File(Environment.getExternalStorageDirectory(), "classes2.dex");

        //在私有目录下创建个文件 getDir方法获取私有目录  参数（目录名称，目录类型） File.separator相当于反斜杠  /
        File privateFile = new File(getDir("odex", Context.MODE_PRIVATE).getAbsolutePath() + File.separator + "classes2.dex");

        //判断私有目录 是否存在 文件
        if (privateFile.exists()) {
            privateFile.delete();//存在就删除文件
        }

        try {
            //使用封装的方法 把 sd卡里面的dex文件 复制到私有目录里面
            FileUtils.copyFile(sourcesFile, privateFile);
            Toast.makeText(this, "复制私有目录成功", Toast.LENGTH_SHORT).show();
            //修复dex文件
            RestoreDexUtils.loadFixedDex(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
