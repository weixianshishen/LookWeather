package example.com.lookweather.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by 黑月 on 2017/3/7.
 */

public class TestPattern {
    public static void main(String[] args) {
        proxy();

    }

    /**
     * 代理模式
     * newProxyInstance 返回接口 调用接口实现类的任何方法都会调用代理模式的invoke
     */
    private static void proxy() {
        final ICat cat = new Cat();

        ICat proxyCat=(ICat) Proxy.newProxyInstance(TestPattern.class.getClassLoader(), cat.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                if (method.getName().equals("run")) {
                    System.out.println("猫抓老鼠");
                    return method.invoke(cat, objects);
                }
                return method.invoke(cat, objects);
            }
        });
        proxyCat.hashCode();
    }

    /**
     * 装饰者模式
     * 1.装饰者和被装饰者实现或继承相同的接口或类
     * 2.装饰者持有被装饰者的引用
     *
     */

    static class Decorate implements ICat{

        ICat mICat;

        public Decorate(ICat ICat) {
            this.mICat = ICat;
        }

        @Override
        public void run() {
            System.out.println("猫抓老鼠");
            mICat.run();
        }
    }

    static class Cat implements ICat{

        @Override
        public void run() {
            System.out.println("猫抓到了老鼠...");
        }
    }
    interface ICat{
        void run();
    }

}
