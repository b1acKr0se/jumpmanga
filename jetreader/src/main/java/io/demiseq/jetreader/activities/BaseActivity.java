package io.demiseq.jetreader.activities;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, intent))
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities();
                else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    NavUtils.navigateUpTo(this, intent);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setTitle(String title) {
        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
}
