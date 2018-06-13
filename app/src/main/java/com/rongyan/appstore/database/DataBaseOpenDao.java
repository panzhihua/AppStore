package com.rongyan.appstore.database;

import com.rongyan.appstore.item.AppInfo;

import java.util.List;

/**
 * 数据库操作方法接口
 */

public interface DataBaseOpenDao {

    boolean QueryBeingClass(int id,int page);//根据id查询类别是否存在

    boolean QueryOverdueClass(int id ,int page);//根据id查询类别是否已过期

    void AddClass(int id,String value,int page);//插入数据库

    void UpdateClass(int id,String value,int page);//修改数据库

    List<String> QueryClass(int id);//根据id查询内容

    void DeleteClass(int id);//删除数据库

    boolean QueryBeingApp(String app_no);//根据id查询app是否存在

    void AddApp(AppInfo appinfo);//插入数据库

    void UpdateApp(AppInfo appinfo);//修改数据库

    void UpdateAppRatings(AppInfo appinfo);//修改评分

    void UpdateAppTnstalledTimes(AppInfo appinfo);//修改下载次数

    void UpdateAppInstalling(AppInfo appinfo);//修改是否正在安装状态

    AppInfo QueryApp(String app_no);//根据id查询内容

    List<AppInfo> GetAllApp();//查询所有内容
}
