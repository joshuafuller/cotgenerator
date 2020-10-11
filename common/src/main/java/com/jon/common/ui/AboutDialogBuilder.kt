package com.jon.common.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jon.common.R
import com.jon.common.variants.Variant
import com.jon.common.versioncheck.GithubRelease
import com.jon.common.versioncheck.UpdateChecker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

internal class AboutDialogBuilder(context: Context) : MaterialAlertDialogBuilder(context) {
    private data class Row(val title: String, var subtitle: String, @DrawableRes var iconId: Int? = null)

    private val updateChecker = UpdateChecker()

    private val rows: List<Row> = listOf(
            Row("Version", Variant.getVersionName()),
            Row("Build Type", BUILD_TYPE),
            Row("Build Time", BUILD_DATE),
            Row("Latest Github Release", LOADING, R.drawable.refresh),
            Row("Github Repository", "https://github.com/jonapoul/cotgenerator", R.drawable.go_to)
    )

    private val adapter = object : ArrayAdapter<Row>(context, R.layout.about_listview_item, R.id.aboutItemTextTitle, rows) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, null, parent)
            val row = rows[position]
            view.findViewById<TextView>(R.id.aboutItemTextTitle).text = row.title
            view.findViewById<TextView>(R.id.aboutItemTextSubtitle).text = row.subtitle
            row.iconId?.let {
                val drawable = ContextCompat.getDrawable(context, it)
                view.findViewById<ImageView>(R.id.aboutItemIcon)?.setImageDrawable(drawable)
            }
            return view
        }
    }

    init {
        val listView = View.inflate(context, R.layout.about_listview, null) as ListView
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position: Int, _ ->
            if (position == GITHUB_INDEX) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(rows[position].subtitle)
                context.startActivity(intent)
            } else if (position == LATEST_INDEX) {
                rows[LATEST_INDEX].subtitle = LOADING
                adapter.notifyDataSetChanged()
                fetchLatestVersion()
            }
        }
        this.setTitle(R.string.menu_about)
                .setView(listView)
                .setPositiveButton(android.R.string.ok, null)
    }

    override fun show(): AlertDialog {
        fetchLatestVersion()
        return super.show()
    }

    @SuppressLint("CheckResult")
    private fun fetchLatestVersion() {
        updateChecker.fetchReleases()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { updateChecker.getLatestRelease(it) }
                .subscribe(::onSuccess, ::onFailure)
    }

    private fun onSuccess(release: GithubRelease?) {
        if (release != null) {
            val suffix = getVersionSuffix(release)
            rows[LATEST_INDEX].subtitle = release.name + suffix
            adapter.notifyDataSetChanged()
        } else {
            onFailure(Exception("Null response"))
        }
    }

    private fun onFailure(throwable: Throwable) {
        rows[LATEST_INDEX].subtitle = "[Error: ${throwable.message}]"
        adapter.notifyDataSetChanged()
    }

    private fun getVersionSuffix(latest: GithubRelease): String {
        return if (updateChecker.isNewerVersion(latest)) {
            " - Update available!"
        } else {
            " - You're up-to-date!"
        }
    }

    private companion object {
        const val LOADING = "Loading..."
        const val LATEST_INDEX = 3
        const val GITHUB_INDEX = 4

        private val BUILD_TYPE = if (Variant.isDebug()) "Debug" else "Release"
        private val BUILD_DATE = SimpleDateFormat("HH:mm:ss dd MMM yyyy z", Locale.ENGLISH).format(Variant.getBuildDate())
    }
}