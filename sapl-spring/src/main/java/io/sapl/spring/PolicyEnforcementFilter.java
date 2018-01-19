package io.sapl.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import io.sapl.spring.marshall.action.HttpAction;
import io.sapl.spring.marshall.resource.HttpResource;
import io.sapl.spring.marshall.subject.AuthenticationSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PolicyEnforcementFilter extends GenericFilterBean {

	private static final String SERVER = "localhost:8080";
	private static final String PROTOCOL = "HTTP:";

	private final StandardSAPLAuthorizator pep;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.sapl.mock.beans.SAPLGenericFilterBean#doFilter(javax.servlet.
	 * ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			LOGGER.debug("Enter the real CustomFilter:");

			LOGGER.debug("Request to Customspermissionevaluator: {} {}{} {}{}", authentication.toString(), SERVER,
					req.getRequestURI(), PROTOCOL, req.getMethod());

			boolean permission = pep.authorize(new AuthenticationSubject(authentication), new HttpAction(req),
					new HttpResource(req));
			LOGGER.debug("The response is: {}", permission);

			if (!permission) {
				LOGGER.debug("User was not authorized for this action");
				throw new AccessDeniedException("Current User may not perform this action.");
			}

		} else {
			LOGGER.debug("unauthenticated User");
			throw new AuthenticationCredentialsNotFoundException("Not authenticated");

		}
		chain.doFilter(req, response);

	}

}