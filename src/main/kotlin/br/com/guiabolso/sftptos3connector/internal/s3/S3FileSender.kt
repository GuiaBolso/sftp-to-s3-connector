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

package br.com.guiabolso.sftptos3connector.internal.s3

import br.com.guiabolso.sftptos3connector.internal.sftp.SftpFile
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams

internal class S3FileSender(
    private val s3Client: AmazonS3
) {

    fun send(
        bucket: String,
        sftpFile: SftpFile,
        filename: String,
        kmsKeyId: String? = null
    ) {
        if(kmsKeyId == null) {
            putObjectUnencrypted(bucket, sftpFile, filename)
        } else {
            putObjectEncrypted(bucket, sftpFile, filename, kmsKeyId)
        }
        
    }
    
    private fun putObjectUnencrypted(bucket: String, sftpFile: SftpFile, filename: String) {
        s3Client.putObject(bucket, filename, sftpFile.stream, objectMetadata(sftpFile.contentLength))
    }
    
    private fun putObjectEncrypted(bucket: String, sftpFile: SftpFile, filename: String, kmsKey: String) {
        s3Client.putObject(
            PutObjectRequest(bucket,
                             filename,
                             sftpFile.stream,
                             objectMetadata(sftpFile.contentLength).withKmsEncryption()
            ).withSSEAwsKeyManagementParams(SSEAwsKeyManagementParams(kmsKey))
        )
    }
    
    private fun objectMetadata(contentLength: Long) = ObjectMetadata().also { it.contentLength = contentLength }
    
    private fun ObjectMetadata.withKmsEncryption() = apply { sseAlgorithm = "aws:kms" }
}
