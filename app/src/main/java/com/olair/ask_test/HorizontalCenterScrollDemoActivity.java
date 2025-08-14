package com.olair.ask_test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * HorizontalCenterScrollView 使用示例
 */
public class HorizontalCenterScrollDemoActivity extends Activity {
    
    private HorizontalCenterScrollView mScrollView;
    private TextView mStatusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建主布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        
        // 创建状态显示文本
        mStatusText = new TextView(this);
        mStatusText.setText("当前居中: 无");
        mStatusText.setTextSize(16);
        mStatusText.setPadding(0, 0, 0, 20);
        mainLayout.addView(mStatusText);
        
        // 创建水平滚动视图
        mScrollView = new HorizontalCenterScrollView(this);
        mScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                200));
        
        // 设置居中变化监听器
        mScrollView.setOnCenterChangeListener(new HorizontalCenterScrollView.OnCenterChangeListener() {
            @Override
            public void onCenterChanged(View centerView, int centerIndex) {
                // 获取居中视图的文本内容
                String text = "";
                if (centerView instanceof TextView) {
                    text = ((TextView) centerView).getText().toString();
                }
                
                // 更新状态显示
                mStatusText.setText("当前居中: " + text + " (索引: " + centerIndex + ")");
                
                // 显示Toast提示
                Toast.makeText(HorizontalCenterScrollDemoActivity.this, 
                        "居中: " + text, Toast.LENGTH_SHORT).show();
            }
        });
        
        mainLayout.addView(mScrollView);
        
        // 添加示例子视图
        addSampleItems();
        
        setContentView(mainLayout);
    }
    
    /**
     * 添加示例子视图
     */
    private void addSampleItems() {
        String[] items = {
            "项目1", "项目2", "项目3", "项目4", "项目5",
            "项目6", "项目7", "项目8", "项目9", "项目10"
        };
        
        int[] colors = {
            Color.parseColor("#FF6B6B"), // 红色
            Color.parseColor("#4ECDC4"), // 青色
            Color.parseColor("#45B7D1"), // 蓝色
            Color.parseColor("#96CEB4"), // 绿色
            Color.parseColor("#FFEAA7"), // 黄色
            Color.parseColor("#DDA0DD"), // 紫色
            Color.parseColor("#98D8C8"), // 薄荷绿
            Color.parseColor("#F7DC6F"), // 淡黄色
            Color.parseColor("#BB8FCE"), // 淡紫色
            Color.parseColor("#85C1E9")  // 淡蓝色
        };
        
        for (int i = 0; i < items.length; i++) {
            TextView itemView = createItemView(items[i], colors[i % colors.length]);
            mScrollView.addChildView(itemView);
        }
    }
    
    /**
     * 创建单个项目视图
     */
    private TextView createItemView(String text, int backgroundColor) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(backgroundColor);
        
        // 设置布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                200, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(10, 10, 10, 10);
        textView.setLayoutParams(params);
        
        return textView;
    }
}