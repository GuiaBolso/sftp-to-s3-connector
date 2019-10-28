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

package br.com.guiabolso.sftptos3connector.internal.sftp

import br.com.guiabolso.sftptos3connector.config.SftpConfig
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.io.InputStream

@ExperimentalStdlibApi
class SftpFileStreamerTest : FunSpec() {
    
    init {
        test("Should stream the content from a SFTP server") {
            withConfiguredSftpServer { server ->
                val target = SftpFileStreamer( SftpConfig("localhost", server.port, sftpUsername, sftpPassword))
    
                val fileStream: InputStream = target.getSftpFile(sftpFilePath).stream
                
                fileStream.reader().readText() shouldBe sftpFileContent
            }
        }
        
        test("Should return the file and it's content length") {
            withConfiguredSftpServer { server ->
                val target = SftpFileStreamer( SftpConfig("localhost", server.port, sftpUsername, sftpPassword))
                
                val fileInfo = target.getSftpFile(sftpFilePath)
                
                fileInfo.contentLength shouldBe sftpFileContent.encodeToByteArray().size.toLong()
            }
        }
    }

}
