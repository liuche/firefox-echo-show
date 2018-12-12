/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.home

import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_navigation_overlay.*
import kotlinx.android.synthetic.main.fragment_navigation_overlay.view.*
import kotlinx.coroutines.experimental.CancellationException
import org.mozilla.focus.R
import org.mozilla.focus.UrlSearcher
import org.mozilla.focus.browser.BrowserFragmentCallbacks
import org.mozilla.focus.browser.HomeTileLongClickListener
import org.mozilla.focus.telemetry.TelemetryWrapper
import org.mozilla.focus.view.DialogFragmentFullscreener

/**
 * An overlay that allows the user to navigate to new pages including "home" tiles".
 *
 * The overlay can be used within the application in two ways:
 * - Inserted into the view hierarchy (the home tiles will appear just below the app bar)
 * - Attached to a new window (i.e. as a Dialog: the home tiles will appear below the app bar
 * and a semi-opaque overlay will be displayed)
 *
 * We attach to a new window to decouple the overlay from the app (it can be overlayed on *any* view and doesn't
 * require an entry in the view hierarchy to attach - cleaner).
 *
 * TODO: insert into view hierarchy on first run. these docs. the todos in BrowserFragment. The extended commit summary
 *
 * We extend an [AppCompatDialogFragment], rather than inserting the fragment into the existing view hierarchy,
 * because
 * We use a fragment to decouple the code between things.
 *
 * TODO:
 * - a11y focus changes (fixes #5).
 * - a11y can't focus overlay to dismiss home tiles.
 * - telemetry
 * - activity/fragment restoration testing
 * - animations
 * - change to new layout.
 * - Insert into view hierarchy
 * - Pinned tile pop-in
 * - Split second delay when opening (b/c new window?)
 */
class NavigationOverlayFragment : AppCompatDialogFragment() {

    private val callbacks: BrowserFragmentCallbacks?
        get() = activity as BrowserFragmentCallbacks?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DialogFragmentFullscreener.register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val overlay = inflater.inflate(R.layout.fragment_navigation_overlay, container, false)

        overlay.navigationOverlayContainer.setOnClickListener {
            dismiss()
            TelemetryWrapper.dismissHomeOverlayClickEvent()
        }

        with(overlay.homeTiles) {
            onTileClicked = { callbacks?.onNonTextInputUrlEntered(it) }
            urlSearcher = activity as UrlSearcher

            homeTileLongClickListener = object : HomeTileLongClickListener {
                override fun onHomeTileLongClick(unpinTile: () -> Unit) {
                    callbacks?.onHomeTileLongClick(unpinTile) // todo: unpinOverlay inside callback should move into this class.
                }
            }
        }

        return overlay
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Since we start the async jobs in View.init and Android is inflating the view for us,
        // there's no good way to pass in the uiLifecycleJob. We could consider other solutions
        // but it'll add complexity that I don't think is probably worth it.
        homeTiles.uiLifecycleCancelJob.cancel(CancellationException("Parent lifecycle has ended"))
    }

    fun refreshTilesForInsertion() {
        homeTiles.refreshTilesForInsertion()
    }

    fun removePinnedSiteFromTiles(tileId: String) {
        homeTiles.removePinnedSiteFromTiles(tileId)
    }

    companion object {
        fun newInstance() = NavigationOverlayFragment()
    }
}
