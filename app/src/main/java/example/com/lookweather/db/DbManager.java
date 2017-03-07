package example.com.lookweather.db;

import android.content.Context;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * Created by 黑月 on 2017/3/2.
 */

public class DbManager {
    private DaoManager manager;

    public DbManager(Context context) {
        manager = DaoManager.getInstance();
        manager.init(context);
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        manager.closeConnection();
    }

    /**
     * 完成对数据库中student 表的插入操作
     */
    public <T> boolean insert(T t) {
        boolean flag;
        flag = manager.getDaoSession().insert(t) != -1 ? true : false;
        return flag;
    }

    public <T> List<T> query(Class<T> clazz) {
        QueryBuilder<T> builder = manager.getDaoSession().queryBuilder(clazz);
        return builder.list();

    }

    public <T> List<T> query(Class<T> clazz, WhereCondition condition) {

        QueryBuilder<T> builder = manager.getDaoSession().queryBuilder(clazz);
        builder.where(condition);
        return builder.list();

    }

}
