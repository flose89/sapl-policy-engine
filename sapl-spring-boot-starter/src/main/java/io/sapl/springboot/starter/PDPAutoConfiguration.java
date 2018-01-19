package io.sapl.springboot.starter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.sapl.api.functions.FunctionException;
import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.api.pdp.PolicyDecisionPoint;
import io.sapl.api.pip.AttributeException;
import io.sapl.pdp.embedded.EmbeddedPolicyDecisionPoint;
import io.sapl.pdp.remote.RemotePolicyDecisionPoint;
import io.sapl.spring.PIPProvider;
import io.sapl.spring.PolicyEnforcementFilter;
import io.sapl.spring.StandardSAPLAuthorizator;
import io.sapl.spring.marshall.obligation.Obligation;
import io.sapl.spring.marshall.obligation.ObligationHandler;
import io.sapl.spring.marshall.obligation.ObligationsHandlerService;
import io.sapl.spring.marshall.obligation.SimpleObligationHandlerService;
import io.sapl.springboot.starter.PDPProperties.Remote;
import lombok.extern.slf4j.Slf4j;

/**
 * This automatic configuration will provide you several beans to deal with SAPL
 * by default. <br/>
 * <b>PRESUMPTION:</b>The only presumption you have to fullfill to work with the
 * <i>sapl-spring-boot-starter</i> is that you will configure at leat one
 * {@link PolicyDecisionPoint}. <br/>
 * If you do not change it, the default configuration (see
 * {@link PDPProperties}) will configure an {@link EmbeddedPolicyDecisionPoint}
 * for you. <br/>
 * <br/>
 * <h2>Configure an EmbeddedPolicyDecisionPoint</h2> To have a bean instance of
 * an {@link EmbeddedPolicyDecisionPoint} just activate it in your
 * <i>application.properties</i>-file (or whatever spring supported way to
 * provide properties you wish to use. c. f. <a href=
 * "https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html">Spring
 * Boot Documentation on config parameters</a>) <br/>
 * Do not forget to provide the minimal required files in your policy path! (at
 * leat you need a <i>pdp.json</i> file) <br/>
 * Example Snippet from .properties:<br/>
 * <code>
 * pdp.embedded.active=true
 * <br/>
 * pdp.embedded.policyPath=classpath:path/to/policies
 * </code> <br/>
 * <b>Hint:</b>The Bean is provided with the predefined qualifier name:
 * {@value #BEAN_NAME_PDP_EMBEDDED}
 *
 * <br/>
 * <br/>
 * <br/>
 *
 * @author danschmi
 * @see PDPProperties
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(PDPProperties.class)
public class PDPAutoConfiguration {

	public static final String BEAN_NAME_PDP_REMOTE = "pdpRemote";

	public static final String BEAN_NAME_PDP_EMBEDDED = "pdpEmbedded";

	private static final String BEAN_NAME_OBLIGATION_HANDLER_DENY_ALL = "denyAllObligationHandler";

	@Autowired
	private PDPProperties pdpProperties;

	@Bean(BEAN_NAME_PDP_EMBEDDED)
	@ConditionalOnProperty("pdp.embedded.active")
	public PolicyDecisionPoint pdpEmbedded(PIPProvider pipProvider)
			throws PolicyEvaluationException, AttributeException, FunctionException, IOException {
		log.debug("creating embedded PDP with Bean name {} and policy path {}", BEAN_NAME_PDP_EMBEDDED,
				pdpProperties.getEmbedded().getPolicyPath());

		EmbeddedPolicyDecisionPoint pdp = new EmbeddedPolicyDecisionPoint(pdpProperties.getEmbedded().getPolicyPath());
		log.debug("PIP-Provider has {} entries.", pipProvider.getPIPClasses().size());
		for (Class<?> clazz : pipProvider.getPIPClasses()) {
			log.debug("importAttributeFindersFromPackage: {}", clazz.getPackage().getName());
			pdp.importAttributeFindersFromPackage(clazz.getPackage().getName());
		}
		return pdp;
	}

	@Bean(BEAN_NAME_PDP_REMOTE)
	@ConditionalOnProperty("pdp.remote.active")
	public PolicyDecisionPoint pdpRemote() {
		Remote remoteProps = pdpProperties.getRemote();
		String host = remoteProps.getHost();
		int port = remoteProps.getPort();
		String key = remoteProps.getKey();
		String secret = remoteProps.getSecret();
		log.debug("creating remote PDP with Bean name {} and properties: \nhost {} \nport {} \nkey {} \nsecret {}",
				BEAN_NAME_PDP_REMOTE, host, port, key, "*******");
		return new RemotePolicyDecisionPoint(host, port, key, secret);
	}

	@Bean
	@ConditionalOnMissingBean
	public PIPProvider processInformationPoints() {
		return Collections::emptyList;
	}

	@Bean
	@ConditionalOnProperty("policyEnforcementFilter")
	public PolicyEnforcementFilter policyEnforcementFilter(StandardSAPLAuthorizator saplAuthorizer) {
		log.debug("no Bean of type PolicyEnforcementFilter defined. Will create default Beanof class {}",
				PolicyEnforcementFilter.class);
		return new PolicyEnforcementFilter(saplAuthorizer);
	}

	@Bean
	@ConditionalOnMissingBean
	public StandardSAPLAuthorizator createStandardSAPLAuthorizator(PolicyDecisionPoint pdp,
			ObligationsHandlerService ohs) {
		log.debug("no Bean of type StandardSAPLAuthorizator defined. Will create default Bean of class {}",
				StandardSAPLAuthorizator.class);
		return new StandardSAPLAuthorizator(pdp, ohs);
	}

	@Bean
	@ConditionalOnMissingBean
	public ObligationsHandlerService createDefaultObligationsHandlerService() {
		log.debug("no Bean of type ObligationsHandlerService defined. Will create default Bean of class {}",
				SimpleObligationHandlerService.class);
		return new SimpleObligationHandlerService();
	}

	@Bean
	public CommandLineRunner registerObligationHandlers(List<ObligationHandler> obligationHandlers,
			ObligationsHandlerService ohs) {
		if (!pdpProperties.getObligationsHandler().isAutoregister()) {
			log.debug("Automatic registration of obligation hanlders is deactivated.");
			return args -> {
			};
		}
		log.debug(
				"Automatic registration of obligation hanlders is activated. {} beans of type ObligationHandler found, they will be reigistered at the ObligationsHandlerServive-bean",
				obligationHandlers.size());
		return args -> obligationHandlers.stream().forEach(ohs::register);

	}

	@Bean(BEAN_NAME_OBLIGATION_HANDLER_DENY_ALL)
	@ConditionalOnMissingBean
	public ObligationHandler denyAllObligationHandler() {
		return new ObligationHandler() {

			@Override
			public void handleObligation(Obligation obligation) {
				log.warn(
						"using denyAllObligationHandler. If you want to handle Obligations register your own und probably unregister this one (Bean name: {})");
			}

			@Override
			public boolean canHandle(Obligation obligation) {
				return false;
			}
		};
	}

}