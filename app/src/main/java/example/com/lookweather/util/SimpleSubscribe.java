package example.com.lookweather.util;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by 黑月 on 2017/3/8.
 */

public abstract class SimpleSubscribe<T> implements Observer<T> {

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onSubscribe(Disposable d) {

    }
}

