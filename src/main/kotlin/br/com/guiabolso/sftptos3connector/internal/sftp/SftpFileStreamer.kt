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
import org.apache.commons.vfs2.VFS
import java.net.URI

internal class SftpFileStreamer(sftpConfig: SftpConfig) {
    private val baseConnectionURI = sftpConfig.run { URI("sftp", "$username:$password", host, port, null, null, null) }
    private val fileSystemManager = VFS.getManager()

    fun getSftpFile(filePath: String): SftpFile {
        return getInputStreamWithContentLength(filePath)
    }

    private fun getInputStreamWithContentLength(filePath: String): SftpFile {
        val remoteFile = fileSystemManager.resolveFile("$baseConnectionURI/$filePath")
        return SftpFile(remoteFile.content.inputStream, remoteFile.content.size)
    }
}
