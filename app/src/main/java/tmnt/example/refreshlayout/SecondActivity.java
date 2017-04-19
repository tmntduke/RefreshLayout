package tmnt.example.refreshlayout;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tmnt.example.refreshlayout.RefreshLayout.OnRefreshListener;
import tmnt.example.refreshlayout.RefreshLayout.RefreshLayout;

public class SecondActivity extends AppCompatActivity {

    private RefreshLayout mRefreshLayout;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mRefreshLayout = (RefreshLayout) findViewById(R.id.second_refresh);
        mListView = (ListView) findViewById(R.id.list);

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            list.add("data" + i);
        }

        ListViewAdapter adapter = new ListViewAdapter(this, list);
        mListView.setAdapter(adapter);

        mRefreshLayout.setCanLoad(true);
        mRefreshLayout.setCanRefresh(true);

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mRefreshLayout.setRefreshOver(true);
                    }
                }).start();
            }

            @Override
            public void onLoad() {

            }
        });

    }

}
