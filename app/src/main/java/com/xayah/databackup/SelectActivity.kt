package com.xayah.databackup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.xayah.databackup.adapter.AppListAdapter
import com.xayah.databackup.databinding.ActivitySelectBinding
import com.xayah.databackup.model.AppInfo
import com.xayah.databackup.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SelectActivity : AppCompatActivity() {
    lateinit var mContext: Context
    lateinit var binding: ActivitySelectBinding
    lateinit var adapter: AppListAdapter
    lateinit var mShell: Shell
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        WindowUtil.setWindowMode(!resolveThemedBoolean(android.R.attr.windowLightStatusBar), window)
        mContext = this
        binding()
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 1) {
            adapter.notifyDataSetChanged()
            init()
        }
    }

    private fun binding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select)
        binding.chipOnlyApp.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                adapter.selectAll(0, 1)
            else
                adapter.selectAll(0, 0)
        }
        binding.chipBackup.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                adapter.selectAll(1, 1)
            else
                adapter.selectAll(1, 0)
        }
        binding.topAppBar.setNavigationOnClickListener { finish() }
        binding.topAppBar.title = getString(R.string.title_select_apps)
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                    R.id.menu_save -> {
                        mShell.saveAppList(adapter.appList)
                        true
                    }
                    R.id.menu_refresh -> {
                        adapter.appList = mutableListOf()
                        mShell.onGenerateAppList()
                        true
                    }
                else -> false
            }
        }
    }

    private fun init() {
        setResult(2, intent)
        adapter = AppListAdapter(this)
        mShell = Shell(this)
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAppList.layoutManager = layoutManager
        GlobalScope.launch {
            val appPackages = ShellUtil.getAppPackages(mShell.APP_LIST_FILE_PATH)
            for (i in appPackages) {
                val app = i.substring(0, i.lastIndexOf(" "))
                val packageName = i.substring(i.lastIndexOf(" ") + 1)
                try {
                    val (appIcon, appName, appPackage) = DataUtil.getAppInfo(mContext, packageName)
                    val appInfo = AppInfo(
                        appIcon,
                        appName,
                        appPackage,
                        app.contains("#"),
                        app.contains("!")
                    )
                    adapter.addApp(appInfo)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            runOnUiThread {
                binding.recyclerViewAppList.adapter = adapter
                var onlyAppAll = true
                var backupAll = true
                for (i in adapter.appList) {
                    if (!i.onlyApp)
                        onlyAppAll = false
                    if (i.ban)
                        backupAll = false
                }
                binding.chipOnlyApp.isChecked = onlyAppAll
                binding.chipBackup.isChecked = backupAll
            }
        }
    }
}