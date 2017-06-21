package com.tingtingfm.cbb.ui.activity;

/**
 * 自定义activity管理栈<br>
 * 满足dock栏activity返回定位功能<br>
 */

import android.app.Activity;

import com.tingtingfm.cbb.common.log.TTLog;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivityStack {
    private Vector<Activity> classNames;
    private Vector<Activity> activityStack;
    private static ActivityStack instance;
    private AtomicInteger mFrontPage = new AtomicInteger();

    private ActivityStack() {
        mFrontPage.set(0);
    }

    public static ActivityStack getInstance() {
        if (instance == null) {
            instance = new ActivityStack();
        }
        return instance;
    }

    /**
     * 弹出队列中的Activity
     * @param activity
     */
	public void popActivity(Activity activity) {
		if (activity != null) {
			if (!activity.isFinishing()) {
				activity.finish();
			}			
			activityStack.remove(activity);
			activity = null;
		}
		
		for (Activity a : activityStack) {
			TTLog.d("pop activity stack " +a.getClass().getName());
		}
	}

    /**
     * 弹出队列中除了给定之外的所有Activity，意思是只保留给定的Activity
     * @param classes
     */
	public void popAllActivityExcept(Class<?>... classes) {
		if (null == activityStack) {
			return;
		}		
		
		Iterator<Activity> it = activityStack.iterator();
		while (it.hasNext()) {
			Activity activity = it.next();
			if ((classes != null) && (classes.length > 0)) {
				boolean hasActiviy = false;
				for (Class<?> cls : classes) {
					if (cls.equals(activity.getClass())) {
						hasActiviy = true;
						break;
					}
				}

				if (hasActiviy) {					
					continue;
				}
			}

			if (!activity.isFinishing()) {
				activity.finish();
			}
			
			it.remove();
			activity = null;
		}
	}

    public void popSomeActivity(Class<?>... classes) {
        if (null == activityStack) {
            return;
        }

        Iterator<Activity> it = activityStack.iterator();
        while (it.hasNext()) {
            Activity activity = it.next();
            if ((classes != null) && (classes.length > 0)) {
                boolean hasActiviy = false;
                for (Class<?> cls : classes) {
                    if (cls.equals(activity.getClass())) {
                        hasActiviy = true;
                        break;
                    }
                }

                if (!hasActiviy) {
                    continue;
                }

                if (!activity.isFinishing()) {
                    activity.finish();
                }

                it.remove();
                activity = null;
            }
        }
    }

    /**
     * 放入Activity队列
     * @param activity
     */
	public void pushActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Vector<Activity>();
		}
		activityStack.add(activity);
		
		for (Activity a : activityStack) {
			TTLog.d("push activity stack " +a.getClass().getName());
		}
	}

    /**
     * 获取当前队列最上层的一个Activity
     * @return
     */
	public Activity getStackTopActivity() {
		if (activityStack != null && activityStack.size() > 0) {
			return activityStack.get(activityStack.size() - 1);
		}
		
		return null;
	}
	
	public int getActivitySize() {
		if (activityStack != null) {
			return activityStack.size();
		}
		
		return 0;
	}
	
	public void addFrontPage() {
		int value = mFrontPage.incrementAndGet();
		
		TTLog.d("app front page number : " + value);
	}
	
	public void decrementFrontPage() {
		int value = mFrontPage.decrementAndGet();
		TTLog.d("app front page number : " + value);
	}
	
	public int getFrontPage() {
		return mFrontPage.get();
	}

	public void pushToStack(Activity className) {
		if (classNames == null) {
			classNames = new Vector<Activity>();
		}
		
		classNames.add(className);
		
		for (Activity a : classNames) {
			TTLog.d("pushToStack class stack " +a.getClass().getName());
		}
	}
	
	public void popForStack(Activity className) {
		for (Activity a : classNames) {
			TTLog.d("popForStack class stack " +a.getClass().getName());
		}
		
		if (className != null) {
			classNames.remove(className);
			className = null;
		}
	}
	
	public Activity getStackTopClassName() {
		if (classNames != null && classNames.size() > 0) {
			return classNames.get(classNames.size() - 1);
		}
		
		return null;
	}

	/**
	 *查看当前Activity是否存在
	 * @return
     */
	public boolean isHaveActivity(Class classes) {
		if (null != activityStack && activityStack.size() > 0) {
			Iterator<Activity> it = activityStack.iterator();
			while (it.hasNext()) {
				Activity activity = it.next();
				if ((classes != null)) {
					if (classes.equals(activity.getClass())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
