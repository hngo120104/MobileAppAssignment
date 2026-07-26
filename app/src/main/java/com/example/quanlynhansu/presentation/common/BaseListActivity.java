package com.example.quanlynhansu.presentation.common;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlynhansu.ClassroomApplication;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseListActivity<T> extends AppCompatActivity {
    protected final List<T> items = new ArrayList<>();
    protected ClassroomRepository repository;
    protected BaseAdapter adapter;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ((ClassroomApplication) getApplication()).getRepository();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(layoutResId);
        applySystemBarInsets(findViewById(android.R.id.content));
    }

    protected void setupList(ListView listView) {
        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public T getItem(int position) { return items.get(position); }
            @Override public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View recycledView, ViewGroup parent) {
                TextView textView = recycledView instanceof TextView
                        ? (TextView) recycledView
                        : new TextView(BaseListActivity.this);
                textView.setPadding(dp(24), dp(20), dp(24), dp(20));
                textView.setTextSize(16);
                textView.setTextColor(Color.DKGRAY);
                textView.setBackgroundColor(position % 2 == 0 ? 0xFFF7F9FC : Color.WHITE);
                textView.setText(render(items.get(position)));
                return textView;
            }
        };
        listView.setAdapter(adapter);
    }

    protected void replaceItems(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        adapter.notifyDataSetChanged();
    }

    protected abstract String render(T item);

    protected final int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    protected final <R> void executeDatabase(Supplier<R> operation, Consumer<R> onSuccess) {
        databaseExecutor.execute(() -> {
            try {
                R result = operation.get();
                mainHandler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        onSuccess.accept(result);
                    }
                });
            } catch (RuntimeException exception) {
                mainHandler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                this,
                                "Không thể truy cập dữ liệu. Vui lòng thử lại.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void applySystemBarInsets(View content) {
        int left = content.getPaddingLeft();
        int top = content.getPaddingTop();
        int right = content.getPaddingRight();
        int bottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            view.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(content);
    }
}
