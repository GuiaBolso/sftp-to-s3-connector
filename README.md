# SFTP to S3 Connector

[![Build Status](https://img.shields.io/travis/GuiaBolso/sftp-to-s3-connector)](https://travis-ci.org/GuiaBolso/sftp-to-s3-connector)
[![GitHub](https://img.shields.io/github/license/GuiaBolso/sftp-to-s3-connector)](https://github.com/GuiaBolso/sftp-to-s3-connector/blob/master/LICENSE)
[![Bintray Download](https://img.shields.io/bintray/v/gb-opensource/maven/SFTP-to-S3-Connector)](https://bintray.com/gb-opensource/maven/SFTP-to-S3-Connector)

## Introduction
When processing large amounts of data and integrating with external partners it's common to use files instead of APIs, and thus the Secure File Transfer Protocol (SFTP) to transfer these files. When building enterprise applications that depend upon this data, it's common to first transfer the files to your own infrastructure and then processing it.

The *SFTP to S3 Connector* library aims to ease the process of transferring files from an SFTP server to [Amazon Simple Storage Service](https://aws.amazon.com/s3/) with a small amount of code to do it.

## Using with Gradle

This library is published to `Bintray jcenter`, so you'll need to configure that in your repositories:
```kotlin
repositories {
    mavenCentral()
    jcenter()
}
```

And then you can import it into your dependencies:
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
connector.transfer("sft/file/path", "foo/MyFile.txt", kmsKeyId = "aws:kms:mykeyid")
```

## Features

- The file will be streamed from one point to the other, therefore there won't be any problems regarding file size in-memory. Although unmeasured, the memory footprint of this library should be small
- As per S3 specification, if there is any errors (such as an interrupted connection) during the transfer, no file chunks will be persisted
- The files can be encrypted if provided with a KMS key ID

## Limitations
- Currently this library doesn't have a way to select which files you want transferred other than by specific path and name.
- This library also doesn't support any other form of encryption/stream processing other than using the Amazon KMS service.
