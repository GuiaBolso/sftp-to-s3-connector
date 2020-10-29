package br.com.guiabolso.sftptos3connector.internal.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PartETag
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams
import com.amazonaws.services.s3.model.UploadPartRequest
import java.io.ByteArrayInputStream
import java.io.OutputStream

@Suppress("MagicNumber")
/**
 * Inspired on https://gist.github.com/blagerweij/ad1dbb7ee2fff8bcffd372815ad310eb
 * by blagerweij
 */
internal class S3OutputStream(
    private val s3Client: AmazonS3,
    private val bucket: String,
    private val path: String,
    private val kmsKeyId: String? = null
) : OutputStream() {
    
    private var open = true
    private val buffer = ByteArray(10 * 1024 * 1024)
    private var position = 0
    private var uploadId: String? = null
    private val etags = mutableListOf<PartETag>()

    override fun write(b: ByteArray) = write(b, 0, b.size)

    override fun write(b: ByteArray, off: Int, len: Int) {
        require(open)
        
        var offset = off
        var length = len
        var size = buffer.size - position
        
        while(length > size) {
            System.arraycopy(b, offset, buffer, position, size)
            position += size
            flushBufferAndRewind()
            offset += size
            length -= size
            size = buffer.size - position
        }
        System.arraycopy(b, offset, buffer, position, length)
        position += length
    }
    
    private fun flushBufferAndRewind() {
        if(uploadId == null) {
            val request = InitiateMultipartUploadRequest(bucket, path)
            val response = s3Client.initiateMultipartUpload(request)
            uploadId = response.uploadId
        }
        uploadPart()
        position = 0
    }
    
    private fun uploadPart() {
        val result = s3Client.uploadPart(UploadPartRequest()
            .withBucketName(bucket)
            .withKey(path)
            .withUploadId(uploadId)
            .withInputStream(ByteArrayInputStream(buffer, 0, position))
            .withPartNumber(etags.size + 1)
            .withPartSize(position.toLong())
        )
        etags += result.partETag
    }

    override fun close() {
        if(!open) return
        open = false
        
        if(uploadId == null) {
            val request = createPutObjectRequest()
            s3Client.putObject(request)
        } else {
            if(position > 0 ) uploadPart()
            s3Client.completeMultipartUpload(CompleteMultipartUploadRequest(bucket, path, uploadId, etags))
        }
        
        if(uploadId != null) {
            if(position > 0) {
                uploadPart()
            }
            s3Client.completeMultipartUpload(CompleteMultipartUploadRequest(bucket, path, uploadId, etags))
        }
    }

    override fun flush() { require(open) }

    override fun write(b: Int) {
        require(open)
        if(position >= buffer.size) flushBufferAndRewind()
        buffer[position++] = b.toByte()
    }

    private fun createPutObjectRequest(): PutObjectRequest {
        val metadata = objectMetadata(position.toLong()).withKmsEncryption()
        val req = PutObjectRequest(bucket, path, ByteArrayInputStream(buffer, 0, position), metadata)
        if(kmsKeyId != null) {
            req.withSSEAwsKeyManagementParams(SSEAwsKeyManagementParams(kmsKeyId))
        }
        return req
    }
    
    private fun objectMetadata(contentLength: Long) = ObjectMetadata().also { it.contentLength = contentLength }

    private fun ObjectMetadata.withKmsEncryption() = apply { 
        if(kmsKeyId != null) sseAlgorithm = "aws:kms"
    }
}
