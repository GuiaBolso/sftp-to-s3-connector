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
import com.adobe.testing.s3mock.junit5.S3MockExtension
import com.amazonaws.services.s3.AmazonS3
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import java.io.ByteArrayInputStream
import java.io.InputStream

class S3FileSenderTest : BehaviorSpec() {
    
    private val kmsKey = "AWS::KMS::Key"
    
    private val s3MockExtension = S3MockExtension.builder().silent().withSecureConnection(true).build()
    
    init {
        Given("A file stream with content") {
            val content = "abc".toByteArray(Charsets.UTF_8)
            val stream: InputStream = ByteArrayInputStream(content)
            val sftpFile = SftpFile(stream, content.size.toLong())
            val filename = "abc.file"
        
            When("The S3FileSender is called to send it without a KMS key") {
                val client = s3MockExtension.createS3Client().withBucket("bucket")
                val target = S3FileSender(client)
                target.send("bucket", sftpFile, filename)

                Then("The file should be sent") {
                    val sentFile = client.getObject("bucket", filename)
                    sentFile.objectContent.readBytes() shouldBe content
                }
            }
            
            When("The S3FileSender is called to send it with a KMS key") {
                s3MockExtension.registerKMSKeyRef(kmsKey)
                val client = s3MockExtension.createS3Client().withBucket("bucket")
                val target = S3FileSender(client)
                target.send("bucket", sftpFile, filename, kmsKey)
                
                Then("The file should be sent with the kms key") {
                    val sentFileMetadata = client.getObjectMetadata("bucket", filename)
                    sentFileMetadata.sseAwsKmsKeyId shouldBe kmsKey
                }
            }
        }
    }
        
        private fun AmazonS3.withBucket(bucket: String) = apply {
            createBucket(bucket)
        }
        
    
    override fun beforeSpec(spec: Spec) {
        s3MockExtension.beforeAll(null)
    }
    
    override fun afterSpec(spec: Spec) {
        s3MockExtension.afterAll(null)
    }
    
}
