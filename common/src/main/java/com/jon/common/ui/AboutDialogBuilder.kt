package com.jon.common.ui

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

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

    private val callback = object : Callback<List<GithubRelease>> {
        override fun onResponse(call: Call<List<GithubRelease>>, response: Response<List<GithubRelease>>) {
            val latest = updateChecker.getLatestRelease(response.body())
            if (latest != null) {
                val suffix = getVersionSuffix(latest.name)
                rows[LATEST_INDEX].subtitle = latest.name + suffix
                adapter.notifyDataSetChanged()
            } else {
                onFailure(call, HttpException(response))
            }
        }

        override fun onFailure(call: Call<List<GithubRelease>>, t: Throwable) {
            rows[LATEST_INDEX].subtitle = "[Error: ${t.message}]"
            adapter.notifyDataSetChanged()
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
                updateChecker.check(callback)
            }
        }
        this.setTitle(R.string.menu_about)
                .setView(listView)
                .setPositiveButton(android.R.string.ok, null)
    }

    override fun show(): AlertDialog {
        updateChecker.check(callback)
        return super.show()
    }

    private fun getVersionSuffix(discoveredVersionStr: String): String {
        return try {
            /* Split into arrays of major, minor version numbers */
            val discovered = discoveredVersionStr.split(".").map { it.toInt() }.toMutableList()
            val installed = Variant.getVersionName().split(".").map { it.toInt() }.toMutableList()
            val longest = max(discovered.size, installed.size)

            /* Make them both the same length, padded with trailing zeros. Accounts for comparisons between
            * versions like "1.3.2" and "1.4"  */
            discovered += List(longest - discovered.size) { 0 }
            installed += List(longest - installed.size) { 0 }

            for (i in 0..longest) {
                if (discovered[i] > installed[i]) {
                    return " - Update available!"
                } else if (discovered[i] < installed[i]) {
                    return " - You're from the future!"
                }
            }
            return " - You're up-to-date!"
        } catch (e: Exception) {
            " - Failed parsing version numbers: ${e.message}"
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