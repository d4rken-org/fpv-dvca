package eu.darken.fpv.dvca.videofeed.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.fpv.dvca.common.viewmodel.SmartVM
import eu.darken.fpv.dvca.main.core.SomeRepo
import javax.inject.Inject

@HiltViewModel
class VideoFeedVM @Inject constructor(
    private val handle: SavedStateHandle,
) : SmartVM() {



}