package com.jon.common.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.jon.common.R
import com.jon.common.databinding.FragmentAboutBinding
import com.jon.common.di.IBuildResources
import com.jon.common.ui.viewBinding
import com.jon.common.utils.MinimumVersions
import com.jon.common.utils.VersionUtils
import com.jon.common.versioncheck.GithubRelease
import com.jon.common.versioncheck.UpdateChecker
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : Fragment(R.layout.fragment_about) {
    @Inject
    lateinit var buildResources: IBuildResources

    @Inject
    lateinit var updateChecker: UpdateChecker

    private val rows by lazy {
        listOf(
                AboutRow("Version", buildResources.versionName),
                AboutRow("Build Type", if (buildResources.isDebug) "Debug" else "Release"),
                AboutRow("Build Time", buildResources.buildTimestamp),
                AboutRow("Latest Github Release", LOADING, R.drawable.refresh),
                AboutRow("Github Repository", "https://github.com/jonapoul/cotgenerator", R.drawable.go_to)
        )
    }

    private val binding by viewBinding(FragmentAboutBinding::bind)

    private val adapter by lazy { AboutArrayAdapter(requireContext(), rows) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listView.adapter = adapter
        binding.listView.setOnItemClickListener { _, _, position: Int, _ ->
            if (position == GITHUB_INDEX) {
                /* Open a web browser to the Github page */
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(rows[position].subtitle)
                requireContext().startActivity(intent)
            } else if (position == LATEST_INDEX) {
                /* Check for any new versions */
                rows[LATEST_INDEX].subtitle = LOADING
                adapter.notifyDataSetChanged()
                fetchLatestVersion()
            }
        }
        fetchLatestVersion()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    @SuppressLint("CheckResult")
    private fun fetchLatestVersion() {
        /* Minimum SDK required by OkHttp, a dependency of Retrofit */
        if (VersionUtils.isAtLeast(MinimumVersions.OKHTTP_SSL)) {
            updateChecker.fetchReleases()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { updateChecker.getLatestRelease(it) }
                    .subscribe(::onSuccess, ::onFailure)
        } else {
            rows[LATEST_INDEX].subtitle = "Update checking requires Android 5.0 or later"
            adapter.notifyDataSetChanged()
        }
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
    }
}