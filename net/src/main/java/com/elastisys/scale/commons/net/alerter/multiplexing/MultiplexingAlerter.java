package com.elastisys.scale.commons.net.alerter.multiplexing;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elastisys.scale.commons.json.types.TimeInterval;
import com.elastisys.scale.commons.net.alerter.Alert;
import com.elastisys.scale.commons.net.alerter.Alerter;
import com.elastisys.scale.commons.net.alerter.filtering.FilteringAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerter;
import com.elastisys.scale.commons.net.alerter.http.HttpAlerterConfig;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerter;
import com.elastisys.scale.commons.net.alerter.smtp.SmtpAlerterConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

/**
 * An {@link Alerter} that listens for incoming {@link Alert}s on an
 * {@link EventBus} and forwards them to a collection of registered SMTP and
 * HTTP {@link Alerter}s.
 * <p/>
 * New {@link Alerter}s are registered with a {@link AlertersConfig}, which
 * describes a number of SMTP and HTTP {@link Alerter}s as well as for how long
 * to suppress duplicate {@link Alert} messages. Two {@link Alert}s are
 * considered equal if they share topic, message and metadata tags (see
 * {@link FilteringAlerter#DEFAULT_IDENTITY_FUNCTION}).
 */
public class MultiplexingAlerter implements Alerter {
	private final static Logger LOG = LoggerFactory
			.getLogger(MultiplexingAlerter.class);

	/**
	 * Holds the list of configured {@link Alerter}s (if any) to which incoming
	 * {@link Alert}s will be dispatched (unless the duplicate filter is
	 * triggered for the {@link Alerter}).
	 */
	private List<Alerter> alerters;

	/**
	 * Creates an {@link MultiplexingAlerter} that listens to {@link Alert}s on
	 * the given {@link EventBus} and dispatches them to any registered
	 * {@link Alerter}s.
	 *
	 * @param eventBus
	 *            The {@link EventBus} on which to listen for {@link Alert}s.
	 */
	public MultiplexingAlerter(EventBus eventBus) {
		checkArgument(eventBus != null, "no eventBus given");
		eventBus.register(this);
		this.alerters = new CopyOnWriteArrayList<>();
	}

	@Subscribe
	@Override
	public void handleAlert(Alert alert) throws RuntimeException {
		for (Alerter alerter : alerters()) {
			try {
				alerter.handleAlert(alert);
			} catch (Exception e) {
				LOG.warn("failed to dispatch alert to alerter: {}",
						e.getMessage());
			}
		}
	}

	/**
	 * Registers a number of {@link Alerter}s to which incoming {@link Alert}s
	 * are to be forwarded. The {@link Alerter}s, specified in an
	 * {@link AlertersConfig}, are added to the {@link Alerter}s already
	 * registered with this {@link MultiplexingAlerter}.
	 *
	 * @param alertersConfig
	 *            Describes the {@link Alerter}s to register and the duration
	 *            for which to suppress duplicate {@link Alert}s from being
	 *            re-sent. May be <code>null</code>, meaning no {@link Alerter}s
	 *            will be registered.
	 * @param standardAlertMetadataTags
	 *            Tags that are to be included in all sent out {@link Alert}s
	 *            (in addition to those already set on the {@link Alert}
	 *            itself). May be <code>null</code>, which means no standard
	 *            tags are to be used.
	 */
	public void registerAlerters(AlertersConfig alertersConfig,
			Map<String, JsonElement> standardAlertMetadataTags) {
		if (alertersConfig == null) {
			LOG.debug("no alert handlers registered.");
			return;
		}
		Map<String, JsonElement> standardTags = ImmutableMap.of();
		if (standardAlertMetadataTags != null) {
			standardTags = standardAlertMetadataTags;
		}

		LOG.debug("alerters set up with duplicate suppression: {}",
				alertersConfig.getDuplicateSuppression());

		List<Alerter> newAlerters = Lists.newArrayList();
		// add SMTP alerters
		List<SmtpAlerterConfig> smtpAlerters = alertersConfig.getSmtpAlerters();
		LOG.debug("adding {} SMTP alerter(s)", smtpAlerters.size());
		for (SmtpAlerterConfig smtpAlerterConfig : smtpAlerters) {
			newAlerters.add(filteredAlerter(
					new SmtpAlerter(smtpAlerterConfig, standardTags),
					alertersConfig.getDuplicateSuppression()));
		}
		// add HTTP alerters
		List<HttpAlerterConfig> httpAlerters = alertersConfig.getHttpAlerters();
		LOG.debug("adding {} HTTP alerter(s)", httpAlerters.size());
		for (HttpAlerterConfig httpAlerterConfig : httpAlerters) {
			newAlerters.add(filteredAlerter(
					new HttpAlerter(httpAlerterConfig, standardTags),
					alertersConfig.getDuplicateSuppression()));
		}
		this.alerters.addAll(newAlerters);
	}

	private Alerter filteredAlerter(Alerter alerter,
			TimeInterval duplicateSuppression) {
		long suppressionTime = duplicateSuppression.getTime();
		TimeUnit timeUnit = duplicateSuppression.getUnit();
		return new FilteringAlerter(alerter, suppressionTime, timeUnit);
	}

	/**
	 * Clears all registered {@link Alerter}s.
	 */
	public void unregisterAlerters() {
		this.alerters.clear();
	}

	/**
	 * Returns <code>true</code> if this {@link MultiplexingAlerter} has no
	 * registered {@link Alerter}s, <code>false</code> otherwise.
	 *
	 * @return
	 */
	public synchronized boolean isEmpty() {
		return this.alerters.isEmpty();
	}

	/**
	 * Return a copy of the currently configured {@link Alerter}s.
	 *
	 * @return
	 */
	List<Alerter> alerters() {
		return ImmutableList.copyOf(this.alerters);
	}

}
