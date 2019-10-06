package template;

import android.os.SystemClock;
import android.view.View;

/**
 * 单击时间间隔控制，使用的是聚合模式，避免了单继承的限制；此类可以添加多个继承接口，按照下面模板的方式控制
 * view.setOnClickListener(new SingleClickController(new View.OnClickListener(){...}))
 */
public class SingleClickController implements View.OnClickListener,
        YourClickListener, YourClickListener_Next {
    //-------全局一个-----------
    public long intervals = 800;//间隔时间ms
    private long lastTime = 0;
    //------------------------

    //------------View.OnClickListener模板------------
    public View.OnClickListener vocListener;
    public SingleClickController(View.OnClickListener vocListener) {
        this.vocListener = vocListener;
    }
    @Override
    public final void onClick(View v) {
        if (SystemClock.elapsedRealtime() - lastTime < intervals) return;
        if (vocListener == null) throw new NullPointerException("vocListener == null");
        vocListener.onClick(v);
        lastTime = SystemClock.elapsedRealtime();
    }
    //------------View.OnClickListener模板end---------


    public YourClickListener yourListener;
    public SingleClickController(YourClickListener yourListener) {
        this.yourListener = yourListener;
    }
    @Override
    public final void yourClick() {
        if (SystemClock.elapsedRealtime() - lastTime < intervals) return;
        if (yourListener == null) throw new NullPointerException("yourListener == null");
        yourListener.yourClick();
        lastTime = SystemClock.elapsedRealtime();
    }

    public YourClickListener_Next yourListener_next;
    public SingleClickController(YourClickListener_Next yourListener_next) {
        this.yourListener_next = yourListener_next;
    }
    @Override
    public final void yourClick_next() {
        if (SystemClock.elapsedRealtime() - lastTime < intervals) return;
        if (yourListener_next == null) throw new NullPointerException("yourListener_next == null");
        yourListener_next.yourClick_next();
        lastTime = SystemClock.elapsedRealtime();
    }
}

interface YourClickListener {
    void yourClick();
}

interface YourClickListener_Next {
    void yourClick_next();
}
