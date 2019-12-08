package template;

import android.os.SystemClock;
import android.view.View;

/**
 * 谷歌工程师的多击代码
 * 和SingleClickController一样的形式包装
 * view.setOnClickListener(new MultipleClicksController(3, new View.OnClickListener(){...}))
 */
public class MultipleClicksController implements View.OnClickListener {
    public long intervals = 500;//间隔时间ms
    private final long[] hits;

    public View.OnClickListener vocListener;
    public MultipleClicksController(int clicks, View.OnClickListener vocListener) {
        this.vocListener = vocListener;
        hits = new long[clicks];
    }

    @Override
    public void onClick(View v) {
        System.arraycopy(hits, 1, hits, 0, hits.length - 1);
        hits[hits.length - 1] = SystemClock.uptimeMillis();
        if (hits[0] >= (SystemClock.uptimeMillis() - intervals)) {//这上面三行是关键
            vocListener.onClick(v);//这里是多击完成的逻辑
        }
    }
}