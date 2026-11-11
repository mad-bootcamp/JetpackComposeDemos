package com.pnc.jetpackcomposedemos.features.artists.domain

import javax.inject.Inject

class RefreshArtistsUseCase @Inject constructor(
    private val repository: ArtistRepository,
    private val something: Int = 0
) {

    suspend operator fun invoke() {
        repository.refreshArtists()
    }

}

