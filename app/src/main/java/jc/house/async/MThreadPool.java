package jc.house.async;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jc.house.models.BaseModel;
import jc.house.models.ServerArrayResult;
import jc.house.models.ServerObjectResult;
import jc.house.models.ServerResult;
import jc.house.utils.LogUtils;
import jc.house.utils.ParseJson;

/**
 * Created by hzj on 2015/12/11.
 */
public class MThreadPool {
    private static final int THREAD_NUM = 3;
    private static final String TAG = "MThreadPool";
    private static MThreadPool instance;
    private ExecutorService executorService;
    private Handler mHandler;

    private MThreadPool() {
        executorService = Executors.newFixedThreadPool(THREAD_NUM);
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public static MThreadPool getInstance() {
        if (null == instance) {
            synchronized (MThreadPool.class) {
                if (null == instance) {
                    instance = new MThreadPool();
                }
            }
        }
        return instance;
    }

    public void submit(Runnable task) {
        this.executorService.submit(task);
    }

    /**
     * 拒绝新的请求，继续执行并未执行完的任务
     */
    public void shutdown() {
        if (!this.executorService.isTerminated()) {
            this.executorService.shutdown();
        }
    }

    /**
     * 拒绝新的请求，清除所有未执行的任务（等待队列中的），并且在运行线程上调用interrupt()
     */
    public void shutdownNow() {
        if (!this.executorService.isTerminated()) {
            this.executorService.shutdownNow();
        }
    }

    /**
     * awaitTermination是监测当前线程池是否终止的。
     */
    public void listenShutdown() {
        if (!this.executorService.isTerminated()) {
            try {
                while (this.executorService.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                    LogUtils.debug(TAG, "线程池未关闭");
                }
                LogUtils.debug(TAG, "线程池已经关闭");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
