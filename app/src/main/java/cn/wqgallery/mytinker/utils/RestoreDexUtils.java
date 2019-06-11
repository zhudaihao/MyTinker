package cn.wqgallery.mytinker.utils;

import android.content.Context;

import java.io.File;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class RestoreDexUtils {
    //存放需要修复的dex集合
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        //修复前先清空
        loadedDex.clear();
    }


    //修复
    public static void loadFixedDex(Context context) {
        if (context == null) {
            return;
        }

        //获取私有目录下的dex文件
        //获取私有路径文件夹
        File odex = context.getDir("odex", Context.MODE_PRIVATE);
        //获取私有路径下的所有文件
        File[] files = odex.listFiles();

        for (File file : files) {
            if (file.getName().endsWith(".dex") && !file.getName().equals("classes.dex")) {
                //获取修复好的dex文件  装进loadedDex集合
                loadedDex.add(file);
            }
        }


        //创建类加载器
        createClassLoad(context, odex);

    }

    private static void createClassLoad(Context context, File odex) {
        //创建类加载器
        DexClassLoader dexClassLoader;

        //缓存路径
        String opt = odex.getAbsolutePath() + File.separator + "opt_dex";
        File file = new File(opt);
        //判断文件是否存在
        if (!file.exists()) {
            //不存在就创建
            file.mkdirs();
        }

        //遍历集合创建对应的类加载器 参数（dex文件的绝对路径，缓存文件，dex使用C库的路径，类加载器的父类）
        for (File dex : loadedDex) {
            dexClassLoader = new DexClassLoader(dex.getAbsolutePath(), opt, null, context.getClassLoader());
            //热修复
            hotRestore(dexClassLoader, context);
        }

    }

    /**
     * 通过修复好的类加载器 反射获取 修复好的dexElements数组
     * 通过上下文 反射获取 系统的dexElements数组
     * <p>
     * 通过合并获取一个修复好的dexElements数组在前面 系统的dexElements数组在后面的新数组
     * <p>
     * 通过上下文  反射获取 系统 dexElements变量 赋值新的数组
     * <p>
     * 原理：
     * 通过看系统源码 系统对dexElements数组的数组 如果you8重复的数据 只加载前的后面的相同的数据不会再加载
     */

    private static void hotRestore(DexClassLoader dexClassLoader, Context context) {
        try {
            //获取修复好的dex文件的dexElements数组  ;ReflectUtils.getPathList(dexClassLoader)获取pathList对象
            Object myDexElements = ReflectUtils.getDexElements(ReflectUtils.getPathList(dexClassLoader));

            //获取系统dexElements数组
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            Object systemDexElements = ReflectUtils.getDexElements(ReflectUtils.getPathList(pathClassLoader));

            //合并
            Object newDexElements = ArrayUtils.combineArray(myDexElements, systemDexElements);

            //获取系统pathList
            Object SystemPathList = ReflectUtils.getPathList(pathClassLoader);

            //重新赋值给系统的 pathList的dexElements字段
            ReflectUtils.setField(SystemPathList, SystemPathList.getClass(), newDexElements);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
