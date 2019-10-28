/*
 * Copyright 2019 Guiabolso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.guiabolso.sftptos3connector

import br.com.guiabolso.sftptos3connector.config.S3Config
import br.com.guiabolso.sftptos3connector.config.SftpConfig
import br.com.guiabolso.sftptos3connector.internal.s3.S3FileSender
import br.com.guiabolso.sftptos3connector.internal.sftp.SftpFileStreamer

/**
 * Simple connector from a SFTP server to a S3 bucket
 *
 * This class provides a simple way to transfer files from a SFTP server to a S3 file.
 *
 * It uses the SFTP protocol to obtain a file stream and pipes that stream directly to S3. This way no file is entirely
 * buffered to the application memory. The streamed file can optionally be encrypted if a KMS Key ID is provided.
 *
 * According to S3 specification, if there is a failure during the file transfer, no file chunks are stored at S3,
 * so if no exception is thrown during the execution, it's assumed that the file was transferred correctly.
 */
public class SftpToS3Connector(
    sftpConfig: SftpConfig,
    private val s3Config: S3Config
) {
    private val sftpFileStreamer = SftpFileStreamer(sftpConfig)
    private val s3FileSender = S3FileSender(s3Config.amazonS3)
    
    /**
     * Transfers the file in [sftpFilePath] to [s3File] in S3, optionally encrypting it with [kmsKeyId]
     *
     * This method will stream a file from the SFTP server configured in the constructor to a specific S3 bucket (also
     * configured in the constructor) with [s3File] as its key.
     *
     * It's possible to encrypt the file using a [kmsKeyId], but this is optional and a non-encrypted transfer is also
     * possible (although not recommended).
     *
     * As per S3 specification, if there's a failure during the file transfer, no file chunks are stored at S3, so if
     * no exception is thrown during this execution, it's assumed that the file was transferred correctly.
     */
    public fun transfer(sftpFilePath: String, s3File: String, kmsKeyId: String? = null): Unit {
        val sftpFile = sftpFileStreamer.getSftpFile(sftpFilePath)
        s3FileSender.send(s3Config.bucket, sftpFile, s3File, kmsKeyId)
    }
    
    
}
