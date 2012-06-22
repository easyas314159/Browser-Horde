public class MinifyAction extends DeferredAction {
	private final AmazonS3 awsS3;

	private final String bucket, srcKey, dstKey;

	public MinifyAction(AmazonS3 awsS3, String bucket, String srcKey, String dstKey) {
		this.awsS3 = awsS3;
		this.bucket = bucket;

		this.srcKey = srcKey;
		this.dstKey = dstKey;
	}

	public void execute() throws Exception {
		S3Object object = awsS3.getObject(bucket, srcKey);
		ObjectMetadata metadata = object.getObjectMetadata();
		AccessControlList acl = awsS3.getObjectAcl(bucket, srcKey);

		InputStreamReader reader = new InputStreamReader(object.getObjectContent());

		ConcurrentPipeStream pipe = new ConcurrentPipeStream();
		CountingOutputStream outCount = new CountingOutputStream(pipe.getOutputStream());
		OutputStreamWriter writer = new OutputStreamWriter(outCount);

		JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new Feedback());
		compressor.compress(writer, 16384, true, true, false, false);

		IOUtils.closeQuietly(object.getObjectContent());
		IOUtils.closeQuietly(outGzip);

		metadata.setContentLength(outCount.getByteCount());

		awsS3.putObject(bucket, key, pipe.getInputStream(), metadata);
		awsS3.setObjectAcl(bucket, key, acl);
	}
}
