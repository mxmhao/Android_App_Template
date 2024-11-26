package template;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

// 详细教程：https://blog.csdn.net/ezconn/article/details/133844948
public class RxJava3Templates {

    private static final String TAG = "RxJava3Templates";

    // 线程切换示例
    @SuppressLint("CheckResult")
    public void test1() {
        // 详细请看： https://zhuanlan.zhihu.com/p/346694377
        Observable.create(new ObservableOnSubscribe<String>() {
                    // .subscribeOn() 改变发射源头的执行线程，也就是这里的执行线程
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                        Log.e(TAG, "apply0: " + Thread.currentThread().getName());
                        emitter.onNext("");
                        emitter.onComplete();
                    }
                })
                // 切换下面map的执行线程
                .observeOn(Schedulers.io())
                .map(new Function<String, Integer>() {
                    @Override
                    public Integer apply(String s) throws Throwable {
                        Log.e(TAG, "apply1: " + Thread.currentThread().getName());
                        Thread.sleep(1000);
                        return 1;
                    }
                })
                // 切换下面map的执行线程
                .observeOn(Schedulers.computation())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Throwable {
                        Log.e(TAG, "apply2: " + Thread.currentThread().getName());
                        return "";
                    }
                })
                // 切换下面map的执行线程
                .observeOn(Schedulers.single())
                .map(new Function<String, Float>() {
                    @Override
                    public Float apply(String s) throws Throwable {
                        Log.e(TAG, "apply3: " + Thread.currentThread().getName());
                        return 0f;
                    }
                })
                // 切换下面subscribe的执行线程
                .subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<Float>() {
                    @Override
                    public void accept(Float aFloat) throws Throwable {
                        Log.e(TAG, "apply4: " + Thread.currentThread().getName());
                    }
                });
    }
}
