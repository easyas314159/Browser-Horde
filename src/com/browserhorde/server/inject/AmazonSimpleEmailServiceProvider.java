package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSimpleEmailServiceProvider implements Provider<AmazonSimpleEmailService> {
	@Inject private ServletContext context;

	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSimpleEmailService get() {
		AmazonSimpleEmailServiceAsync ses = new AmazonSimpleEmailServiceAsyncClient(awsCredentials, awsClientConfig, executorService);

		String awsSender = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_SES_SENDER));
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
