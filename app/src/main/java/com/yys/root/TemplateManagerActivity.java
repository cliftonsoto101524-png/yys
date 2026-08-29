package com.yys.root;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Template manager activity for viewing and managing screenshot templates.
 */
public class TemplateManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerModules;
    private TextView tvEmpty;
    private TemplateManager mTemplateManager;
    private ModuleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_manager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTemplateManager = TemplateManager.getInstance();

        recyclerModules = findViewById(R.id.recycler_modules);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerModules.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ModuleAdapter();
        recyclerModules.setAdapter(mAdapter);

        FloatingActionButton fab = findViewById(R.id.fab_scan);
        fab.setOnClickListener(v -> {
            mTemplateManager.scanTemplates();
            refreshData();
            Toast.makeText(this, "扫描完成", Toast.LENGTH_SHORT).show();
        });

        refreshData();
    }

    private void refreshData() {
        List<ModuleItem> items = new ArrayList<>();
        for (String module : mTemplateManager.getModules()) {
            int count = mTemplateManager.getTemplateCount(module);
            items.add(new ModuleItem(module, count));
        }
        mAdapter.setItems(items);

        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerModules.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerModules.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_template_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_scan) {
            mTemplateManager.scanTemplates();
            refreshData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Module item data class.
     */
    static class ModuleItem {
        final String name;
        final int templateCount;

        ModuleItem(String name, int count) {
            this.name = name;
            this.templateCount = count;
        }
    }

    /**
     * RecyclerView adapter for modules.
     */
    class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {
        private List<ModuleItem> mItems = new ArrayList<>();

        void setItems(List<ModuleItem> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_module, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModuleItem item = mItems.get(position);
            holder.tvName.setText(item.name);
            holder.tvCount.setText(item.templateCount + " 模板");
            holder.itemView.setOnClickListener(v -> {
                showModuleTemplates(item.name);
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvCount;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_module_name);
                tvCount = itemView.findViewById(R.id.tv_template_count);
            }
        }
    }

    private void showModuleTemplates(String moduleName) {
        List<String> templates = mTemplateManager.getModuleTemplates(moduleName);
        String[] names = new String[templates.size()];
        for (int i = 0; i < templates.size(); i++) {
            names[i] = new File(templates.get(i)).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle(moduleName + " (" + templates.size() + ")")
                .setItems(names, null)
                .setPositiveButton("确定", null)
                .show();
    }
}
