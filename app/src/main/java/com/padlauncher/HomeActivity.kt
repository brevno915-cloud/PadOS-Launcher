package com.padlauncher

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var clockText: TextView
    private lateinit var dateText: TextView
    private lateinit var settingsButton: ImageButton
    private lateinit var searchInput: EditText
    private lateinit var dockRecycler: RecyclerView
    private lateinit var gridRecycler: RecyclerView
    private lateinit var root: View

    private val prefs by lazy { LauncherPreferences.getSharedPreferences(this) }
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()
    private var dockItems: MutableList<AppInfo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        root = findViewById(R.id.home_root)
        clockText = findViewById(R.id.clockText)
        dateText = findViewById(R.id.dateText)
        settingsButton = findViewById(R.id.settingsButton)
        searchInput = findViewById(R.id.searchInput)
        dockRecycler = findViewById(R.id.dockRecycler)
        gridRecycler = findViewById(R.id.gridRecycler)

        loadClock()
        loadApps()
        setupDock()
        setupSearch()
        setupWallpaper()

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
        setupDock()
        setupWallpaper()
    }

    private fun loadClock() {
        val formatter = SimpleDateFormat("h:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val now = Calendar.getInstance().time

        clockText.text = formatter.format(now)
        dateText.text = dateFormat.format(now)
    }

    private fun loadApps() {
        allApps = AppLoader.loadInstalledApps(this)
        filteredApps = allApps
        updateGrid()
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                filteredApps = if (query.isEmpty()) {
                    allApps
                } else {
                    allApps.filter { it.appName.contains(query, ignoreCase = true) }
                }
                updateGrid()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    private fun setupDock() {
        val favorites = LauncherPreferences.getDockItems(prefs)
        val preferred = mutableListOf<AppInfo>()
        val appsByPackage = allApps.associateBy { it.packageName }

        for (packageName in favorites) {
            appsByPackage[packageName]?.let { preferred += it }
        }

        if (preferred.size < 4) {
            preferred += allApps.take(4 - preferred.size)
        }

        dockItems = preferred.take(4).toMutableList()
        dockRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dockRecycler.adapter = DockAdapter(dockItems) { app -> launchApp(app) }
    }

    private fun updateGrid() {
        val columns = LauncherPreferences.getGridColumns(prefs)
        gridRecycler.layoutManager = GridLayoutManager(this, columns)
        gridRecycler.adapter = AppGridAdapter(filteredApps, LauncherPreferences.getIconSize(prefs)) { app ->
            launchApp(app)
        }
    }

    private fun launchApp(app: AppInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            ?: throw IllegalStateException("No launch intent")
            startActivity(launchIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to launch ${app.appName}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupWallpaper() {
        val brightness = LauncherPreferences.getBrightness(prefs)
        val alpha = (brightness / 100f) * 0.85f + 0.15f
        root.alpha = alpha

        val uriString = LauncherPreferences.getWallpaperUri(prefs)
        if (uriString.isNullOrEmpty()) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#0B1020"))
            return
        }

        try {
            val uri = Uri.parse(uriString)
            val bitmap = android.graphics.BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
            val drawable = android.graphics.drawable.BitmapDrawable(resources, bitmap)
            root.background = drawable
        } catch (e: Exception) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#0B1020"))
        }
    }
}

class AppGridAdapter(
    private val apps: List<AppInfo>,
    private val iconSize: Int,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iconImage)
        val name: TextView = itemView.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.name.text = app.appName
        holder.icon.setImageDrawable(app.icon)
        val lp = holder.icon.layoutParams
        lp.width = iconSize
        lp.height = iconSize
        holder.icon.layoutParams = lp
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = apps.size
}

class DockAdapter(
    private val apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<DockAdapter.DockViewHolder>() {

    class DockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iconImage)
        val name: TextView = itemView.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dock_app, parent, false)
        return DockViewHolder(view)
    }

    override fun onBindViewHolder(holder: DockViewHolder, position: Int) {
        val app = apps[position]
        holder.name.text = app.appName
        holder.icon.setImageDrawable(app.icon)
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount(): Int = apps.size
}
