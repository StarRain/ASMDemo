package com.rainstar.asm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class TestASMActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test_asm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAsmAddToast();
            }
        });
    }

    /**
     * 此方法为调试目标方法
     */
    private void testAsmAddToast() {
    }
}