class GzipAction extends DeferredAction {
	private final AmazonS3 awsS3;

	private final String bucket, srcKey, dstKey;

	public GzipAction(AmazonS3 awsS3, String bucket, String srcKey, String dstKey) {
		this.awsS3 = awsS3;
		this.bucket = bucket;

		this.srcKey = srcKey;
		this.dstKey = dstKey;
	}

	public void execute() throws Exception {
		S3Object object = awsS3.getObject(bucket, srcKey);
		ObjectMetadata metadata = object.getObjectMetadata();
		AccessControlList acl = awsS3.getObjectAcl(bucket, srcKey);

		metadata.setContentEncoding("gzip");

		ConcurrentPipeStream pipe = new ConcurrentPipeStream();
		CountingOutputStream outCount = new CountingOutputStream(pipe.getOutputStream());
		GZIPOutputStream outGzip = new GZIPOutputStream(outCount);

		IOUtils.copy(object.getObjectContent(), outGzip);

		IOUtils.closeQuietly(object.getObjectContent());
		IOUtils.closeQuietly(outGzip);

		metadata.setContentLength(outCount.getByteCount());
		metadata.setHeader("Vary", "Accept-Encoding");

		awsS3.putObject(bucket, dstKey, pipe.getInputStream(), metadata);
		awsS3.setObjectAcl(bucket, dstKey, acl);
	}
}