# SFTP to S3 Connector

[![Build](https://github.com/GuiaBolso/sftp-to-s3-connector/actions/workflows/build.yml/badge.svg)](https://github.com/GuiaBolso/sftp-to-s3-connector/actions/workflows/build.yml)
[![GitHub](https://img.shields.io/github/license/GuiaBolso/sftp-to-s3-connector)](https://github.com/GuiaBolso/sftp-to-s3-connector/blob/master/LICENSE)
![Maven Central](https://img.shields.io/maven-central/v/br.com.guiabolso/sftp-to-s3-connector)

## Introduction
When processing large amounts of data and integrating with external partners it's common to use files instead of APIs, and thus the Secure File Transfer Protocol (SFTP) to transfer these files. When building enterprise applications that depend upon this data, it's common to first transfer the files to your own infrastructure and then processing it.

The *SFTP to S3 Connector* library aims to ease the process of transferring files from an SFTP server to [Amazon Simple Storage Service](https://aws.amazon.com/s3/) with a small amount of code to do it.

## Using with Gradle

You can import easily it into your dependencies:
```kotlin
dependencies {
    implementation("br.com.guiabolso:sftp-to-s3-connector:{version}")
}
```

## Usage
Create an instance of `SftpToS3Connector` with the SFTP and S3 configurations:

```kotlin
val connector: SftpToS3Connector = SftpToS3Connector(
    sftpConfig = SftpConfig(host = "mysftphost.com", port = 1337, sftpUsername = "username", sftpPassword = "password"),
    s3Config = S3Config(bucket = "MyS3Bucket")
)
```

If needed, you can configure the `AmazonS3` client to be used instead of the default:
```kotlin
val s3Config = S3Config(bucket = "MyS3Bucket", amazonS3 = AmazonS3ClientBuilder.standard().configure().build())
```


Use it to transfer specific files from the SFTP server to the S3 bucket:

```kotlin
connector.transfer(sftpFilePath = "sftp/file/path", s3File = "foo/MyFile.txt")
```

You can optionally pass a KMS Key ID to request a server-side encryption with it
```kotlin
connector.transfer("sftp/file/path", "foo/MyFile.txt", kmsKeyId = "aws:kms:mykeyid")
```

You can optionally pass a Stream Transformer to process the file while it's being streamed. This can be useful for filters or event notifications of some sort.
```kotlin
connector.transfer("sftp/file/path", "foo/MyFile.txt", transformer = { inputStream, outputStream -> inputStream.copyTo(outputStream) })
```

## Features

- The file will be streamed from one point to the other, therefore there won't be any problems regarding file size in-memory. Although unmeasured, the memory footprint of this library should be small
- As per S3 specification, if there is any errors (such as an interrupted connection) during the transfer, no file chunks will be persisted
- The files can be encrypted if provided with a KMS key ID
- It's possible to process the file (as an InputStream) while it's being streamed to S3

## Limitations
- Currently this library doesn't have a way to select which files you want transferred other than by specific path and name.
- This library also doesn't support any other form of encryption/stream processing other than using the Amazon KMS service.
