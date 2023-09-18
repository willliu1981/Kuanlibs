package idv.kuan.kuanandroidlibs.components;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;

public interface InitComponentActivity {

    public void init();

    public void initComponents();

    default void completeActivity(Context context) {
        completeActivity(context, null);

    }

    default void completeActivity(Context context, Intent intent) {
        if (intent == null) {
            ((Activity) context).setResult(Activity.RESULT_OK);
        } else {
            ((Activity) context).setResult(Activity.RESULT_OK, intent);
        }

        ((Activity) context).finish();
    }

}
