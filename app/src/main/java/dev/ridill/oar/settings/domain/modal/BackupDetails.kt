package dev.ridill.oar.settings.domain.modal

import android.os.Parcelable
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import kotlinx.parcelize.Parcelize

@Parcelize
data class BackupDetails(
    val name: String,
    val id: String,
    val timestamp: String,
    val hashSalt: String?,
    val scheme: EncryptionScheme
) : Parcelable