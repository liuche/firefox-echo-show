/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.view

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatDialogFragment
import android.view.ViewGroup.LayoutParams.MATCH_PARENT

/**
 * A class that makes an [AppCompatDialogFragment] fullscreen by calling [DialogFragmentFullscreener.register].
 */
class DialogFragmentFullscreener private constructor(
    private val dialogFragment: AppCompatDialogFragment,
    @StyleRes private val theme: Int = 0
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        dialogFragment.setStyle(AppCompatDialogFragment.STYLE_NO_FRAME, theme)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        // Dialog is null if fragment is inflated inside the existing window
        // `(i.e. fragmentTransaction.add(R.layout...`.
        dialogFragment.dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    companion object {

        /**
         * Make the dialog in the given DialogFragment fullscreen: see
         * [DialogFragmentFullscreener] for more details. It is recommended to call
         * this from [AppCompatDialogFragment.onCreate].
         */
        fun register(
            dialogFragment: AppCompatDialogFragment,
            @StyleRes theme: Int = 0
        ) {
            val that = DialogFragmentFullscreener(dialogFragment, theme)
            dialogFragment.lifecycle.addObserver(that)
        }
    }
}
