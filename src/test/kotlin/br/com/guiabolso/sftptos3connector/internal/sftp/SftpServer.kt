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

import com.github.stefanbirkner.fakesftpserver.lambda.FakeSftpServer
import java.net.ServerSocket

val sftpUsername = "username"
val sftpPassword = "password"
val sftpFilePath = "path/to/file"
val sftpFileContent = "FileContent\nMoreContent"


fun withConfiguredSftpServer(
    password: String = sftpPassword,
    block: (FakeSftpServer) -> Unit
) = FakeSftpServer.withSftpServer { server ->
    server.port = obtainRandomAvailablePort()
    server.putFile(sftpFilePath, sftpFileContent, Charsets.UTF_8)
    server.addUser(sftpUsername, password)
    block(server)
}

private fun obtainRandomAvailablePort(): Int {
    val serverSocket = ServerSocket(0)
    val port = serverSocket.localPort
    serverSocket.close()
    return port
}
