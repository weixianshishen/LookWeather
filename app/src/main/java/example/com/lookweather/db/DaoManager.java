package example.com.lookweather.db;

import android.content.Context;

import org.greenrobot.greendao.query.QueryBuilder;

import example.com.lookweather.db.dao.DaoMaster;
import example.com.lookweather.db.dao.DaoSession;

/**
 * Created by 黑月 on 2017/3/2.
 */

public class DaoManager {
    private static final String DB_NAME = "city.db";//数据库名称
    private static DaoMaster.DevOpenHelper helper;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
    private Context context;

    private DaoManager() {
    }

    /**
     * 使用单例模式获得操作数据库的对象
     */
    public static DaoManager getInstance() {
        return DaoManagerHolder.instance;
    }

    private static class DaoManagerHolder {
        private static final DaoManager instance = new DaoManager();
    }

    public void init(Context context) {
        this.context = context;
    }

    /**
     * 判断是否存在数据库，如果没有则创建数据库
     *
     * @return
     */
    public DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**
     * 完成对数据库的添加、删除、修改、查询的操作，仅仅是一个接口
     *
     * @return
     */
    public DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    /**
     * 打开输出日志的操作,默认是关闭的
     */
    public void setDebug() {
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    /**
     * 关闭所有的操作,数据库开启的时候，使用完毕了必须要关闭
     */
    public void closeConnection() {
        closeHelper();
        closeDaoSession();
    }

    public void closeHelper() {
        if (helper != null) {
            helper.close();
            helper = null;
        }
    }

    public void closeDaoSession() {
        if (daoSession != null) {
            daoSession.clear();
            daoSession = null;
        }
    }
}
