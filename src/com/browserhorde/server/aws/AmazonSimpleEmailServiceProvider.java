package com.browserhorde.server.aws;

import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;
import com.browserhorde.server.ServletInitOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class AmazonSimpleEmailServiceProvider implements Provider<AmazonSimpleEmailService> {
	@Inject @Named(ServletInitOptions.AWS_SES_SENDER) private String awsSender;

	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSimpleEmailService get() {
		AmazonSimpleEmailServiceAsync ses = new AmazonSimpleEmailServiceAsyncClient(awsCredentials, awsClientConfig, executorService);

		if(awsSender != null) {
			ListVerifiedEmailAddressesResult verifiedResults = ses.listVerifiedEmailAddresses();
			if(!verifiedResults.getVerifiedEmailAddresses().contains(awsSender)) {
				VerifyEmailAddressRequest verifyRequest = new VerifyEmailAddressRequest();
				verifyRequest.setEmailAddress(awsSender);
				ses.verifyEmailAddress(verifyRequest);
			}
		}

		return ses;
	}
}
