package com.example.reelshuffle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(247, 247, 249));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(42), dp(24), dp(24));
        scroll.addView(root);

        TextView title = text("Reel Shuffle", 30, Color.rgb(25, 25, 30));
        title.setTypeface(null, 1);
        root.addView(title);
        addText(root, "릴스마다 다음 영상으로 가는 손가락 방향을 무작위로 바꿉니다.", 17, 14);
        addText(root, "↑ 또는 ↓ 중 하나가 매 영상에서 50% 확률로 선택됩니다. 잘못된 방향은 이전 영상으로 전달됩니다.", 15, 22);
        addText(root, "사용 방법", 20, 24);
        addText(root, "① 접근성 서비스를 켭니다.\n② 인스타그램에서 릴스 화면을 엽니다.\n③ 우측에 나타난 ↕ 버튼을 눌러 시작합니다.\n④ 끝낼 때는 버튼을 한 번 더 누릅니다.", 16, 12);

        addButton(root, "1. 접근성 서비스 설정 열기", v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addButton(root, "2. 인스타그램 열기", v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            if (launch == null) {
                addText(root, "인스타그램 앱이 설치되어 있지 않거나 실행할 수 없습니다.", 14, 8);
            } else {
                startActivity(launch);
            }
        });

        Switch hint = new Switch(this);
        hint.setText("활성화 버튼에 정답 방향 표시 (기본: 숨김)");
        hint.setTextSize(15f);
        hint.setPadding(0, dp(24), 0, dp(18));
        hint.setChecked(prefs.getBoolean("show_direction", false));
        hint.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean("show_direction", checked).apply());
        root.addView(hint);

        addText(root, "주의", 20, 16);
        addText(root, "이 앱은 릴스 여부를 자동 판별하지 않습니다. 댓글창·프로필 등에서는 버튼을 꺼 주세요. 화면 위의 투명한 레이어가 터치를 받고 다시 전달하기 때문에 멀티터치, 길게 누르기, 일부 UI가 매끄럽지 않을 수 있습니다. 서비스는 인스타그램을 벗어나면 입력 레이어를 제거합니다.", 14, 10);
        addText(root, "인터넷 권한 없음 · 화면 내용 열람 없음 · 동작 검증용 프로토타입", 13, 24);
        setContentView(scroll);
    }

    private void addButton(LinearLayout root, String title, View.OnClickListener onClick) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(title);
        button.setTextSize(15f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.topMargin = dp(12);
        root.addView(button, lp);
        button.setOnClickListener(onClick);
    }

    private void addText(LinearLayout root, String value, int size, int topMarginDp) {
        TextView t = text(value, size, Color.rgb(65, 66, 76));
        t.setLineSpacing(dp(4), 1.0f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(topMarginDp);
        root.addView(t, lp);
    }

    private TextView text(String value, int size, int color) {
        TextView result = new TextView(this);
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(color);
        result.setGravity(Gravity.START);
        return result;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
