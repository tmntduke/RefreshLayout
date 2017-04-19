package tmnt.example.refreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tmnt.example.refreshlayout.RefreshLayout.OnRefreshListener;
import tmnt.example.refreshlayout.RefreshLayout.RefreshLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refresh);
        refreshLayout.setCanLoad(true);
        refreshLayout.setCanRefresh(true);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                //refreshLayout.setRefreshOver(true);
            }

            @Override
            public void onLoad() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshLayout.setLoadOver(true);
            }
        });

    }
}
