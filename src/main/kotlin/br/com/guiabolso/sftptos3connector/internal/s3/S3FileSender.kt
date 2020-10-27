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
import java.io.InputStream
import java.io.OutputStream

internal class S3FileSender(
    private val s3Client: AmazonS3
) {

    fun send(
        bucket: String,
        sftpFile: SftpFile,
        filename: String,
        transformer: (InputStream, OutputStream) -> Unit,
        kmsKeyId: String? = null
    ) {
        if(kmsKeyId == null) {
            streamObjectUnencrypted(bucket, sftpFile, filename, transformer)
        } else {
            streamObjectEncrypted(bucket, sftpFile, filename, transformer, kmsKeyId)
        }
        
    }
    
    private fun streamObjectUnencrypted(
        bucket: String,
        sftpFile: SftpFile,
        filename: String,
        transformer: (InputStream, OutputStream) -> Unit
    ) {
        sftpFile.stream.use { sftpStream ->
            S3OutputStream(s3Client, bucket, filename).use { s3Stream ->
                transformer(sftpStream, s3Stream)
            }   
        }
    }
    
    private fun streamObjectEncrypted(
        bucket: String,
        sftpFile: SftpFile,
        filename: String,
        transformer: (InputStream, OutputStream) -> Unit,
        kmsKey: String
    ) {
        sftpFile.stream.use { sftpStream ->
            S3OutputStream(s3Client, bucket, filename, kmsKey).use { s3Stream ->
                transformer(sftpStream, s3Stream)
            }
        }
    }
}
