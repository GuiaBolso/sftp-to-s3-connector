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
import br.com.guiabolso.sftptos3connector.internal.sftp.sftpFileContent
import br.com.guiabolso.sftptos3connector.internal.sftp.sftpFilePath
import br.com.guiabolso.sftptos3connector.internal.sftp.sftpPassword
import br.com.guiabolso.sftptos3connector.internal.sftp.sftpUsername
import br.com.guiabolso.sftptos3connector.internal.sftp.withConfiguredSftpServer
import com.adobe.testing.s3mock.junit5.S3MockExtension
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class SftpToS3ConnectorTest : FunSpec() {
    
    private val s3MockExtension = S3MockExtension.builder().silent().withSecureConnection(true).build()
    
    
    init {
        test("Should transfer a file from SFTP to S3") {
            withConfiguredSftpServer { server ->
                val s3Client = s3MockExtension.createS3Client()
                s3Client.createBucket("bucket")
                
                val connector = SftpToS3Connector(
                    SftpConfig("localhost", server.port, sftpUsername, sftpPassword),
                    S3Config("bucket", s3Client)
                )
                
                connector.transfer(sftpFilePath = sftpFilePath, s3File = "folder/filename.txt")
                
                val s3Object = s3Client.getObject("bucket", "folder/filename.txt")
                s3Object.objectContent.bufferedReader().readText() shouldBe sftpFileContent
            }
        }
        
    }
    
    override fun beforeSpec(spec: Spec) {
        s3MockExtension.beforeAll(null)
    }
    
    override fun afterSpec(spec: Spec) {
        s3MockExtension.afterAll(null)
    }
    
}
