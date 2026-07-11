package dev.ridill.oar.settings.data

import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.settings.data.remote.GDriveApi
import dev.ridill.oar.settings.data.remote.dto.GDriveFileDto
import dev.ridill.oar.settings.domain.modal.BackupDetails

fun GDriveFileDto.toBackupDetails(): BackupDetails = BackupDetails(
    name = name,
    id = id,
    timestamp = appProperties[GDriveApi.APP_PROPERTIES_KEY_BACKUP_TIMESTAMP].orEmpty(),
    hashSalt = appProperties[GDriveApi.APP_PROPERTIES_KEY_HASH_SALT],
    scheme = EncryptionScheme.fromTag(appProperties[GDriveApi.APP_PROPERTIES_KEY_ENCRYPTION_SCHEME])
)