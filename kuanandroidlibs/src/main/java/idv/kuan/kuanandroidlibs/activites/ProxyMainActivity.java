package idv.kuan.kuanandroidlibs.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import idv.kuan.kuanandroidlibs.R;

public abstract class ProxyMainActivity extends AppCompatActivity {

    Button btnEntrance;

    public abstract <A extends AppCompatActivity> Class<A> getTargetActivityClass();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proxy_activity_main);

        init();

    }

    private void init() {
        initComponents();
    }

    private void initComponents() {
        btnEntrance = findViewById(R.id.proxy_btn_entrance);
        btnEntrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getTargetActivityClass() == null) {
                    Toast.makeText(ProxyMainActivity.this, "Target Activity is null", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(ProxyMainActivity.this, getTargetActivityClass());

                    startActivity(intent);
                }


            }
        });
    }

}
