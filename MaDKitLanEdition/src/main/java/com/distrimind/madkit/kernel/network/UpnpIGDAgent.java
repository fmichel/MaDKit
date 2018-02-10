/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.igd.callback.GetExternalIP;
import org.fourthline.cling.support.igd.callback.GetStatusInfo;
import org.fourthline.cling.support.igd.callback.PortMappingAdd;
import org.fourthline.cling.support.igd.callback.PortMappingDelete;
import org.fourthline.cling.support.model.Connection.Status;
import org.fourthline.cling.support.model.Connection.StatusInfo;
import org.fourthline.cling.support.model.PortMapping;
import org.fourthline.cling.support.model.PortMapping.Protocol;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.AgentLogger;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.NetworkAgent;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.TaskID;
import com.distrimind.madkit.message.KernelMessage;
import com.distrimind.util.OSValidator;

/**
 * This class agent aims to analyze network interfaces, local networks, and
 * local routers. Than to the UPNP IGD protocol, it is able to give external ip
 * address behind a router, open ports, and get connection status.
 * 
 * When a router is detected, a message {@link NewDeviceReceived} is sent to the
 * agents taking the role
 * {@link LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION#ROLE} and the group
 * {@link LocalCommunity.Groups#NETWORK}. When a router is removed, a message
 * {@link NewDeviceRemoved} is sent to the same agents.
 * 
 * To ask for a connection status message, you need to send a
 * {@link AskForConnectionStatusMessage} message to the group
 * {@link LocalCommunity.Groups#NETWORK} and to the role
 * {@link LocalCommunity.Roles#LOCAL_NETWORK_EXPLORER_ROLE}. Then a
 * {@link ConnexionStatusMessage} will be returned. Changes over time can be
 * notified.
 * 
 * To ask for the external IP message, you need to send a
 * {@link AskForExternalIPMessage} to the same agent. Then a
 * {@link ExternalIPMessage} will be returned. Changes over time can be
 * notified.
 * 
 * To add a port mapping into a specific router, you need to send a
 * {@link AskForPortMappingAddMessage} to the same agent. Then a
 * {@link PortMappingAnswerMessage} will be returned.
 * 
 * To remove a port mapping into a specific router, you need to send a
 * {@link AskForPortMappingRemoveMessage} to the same agent.
 * 
 * @author Jason Mahdjoub
 * @since MadKitLanEdition 1.0
 * @version 1.0
 *
 */
class UpnpIGDAgent extends AgentFakeThread {

	private static final String[] sub_loggers_names = { Registry.class.getName(), UpnpServiceImpl.class.getName(),
			org.fourthline.cling.DefaultUpnpServiceConfiguration.class.getName(), DatagramProcessor.class.getName(),
			SOAPActionProcessor.class.getName(), GENAEventProcessor.class.getName(),
			DeviceDescriptorBinder.class.getName(), ServiceDescriptorBinder.class.getName(), Namespace.class.getName(),
			Registry.class.getName(), org.fourthline.cling.transport.Router.class.getName(),
			ControlPointImpl.class.getName(), ProtocolFactory.class.getName(), MulticastReceiver.class.getName(),
			StreamServer.class.getName(), DatagramIO.class.getName() };

	protected static volatile UpnpService upnpService = null;
	protected static int pointedUpnpServiceNumber = 0;

	final static ThreadPoolExecutor serviceExecutor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 1, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());

	private final RegistryListener registeryListener = new RegistryListener();
	private final HashMap<InetAddress, Router> upnp_igd_routers = new HashMap<>();
	private final ArrayList<AskForRouterDetectionInformation> askers_for_router_detection = new ArrayList<>();

	protected void addRouter(InetAddress ia, Router router) {
		if (ia == null)
			throw new NullPointerException("ia");
		if (router == null)
			throw new NullPointerException("router");

		removeRouter(ia, false);
		synchronized (upnp_igd_routers) {
			upnp_igd_routers.put(ia, router);
			for (AskForRouterDetectionInformation m : askers_for_router_detection)
				sendReply(m, new IGDRouterFoundMessage(router.internal_address));
		}
	}

	protected void updateRouter(RemoteDevice router) {
		if (router == null)
			throw new NullPointerException("router");

		boolean unkowDeviceDetected = false;
		synchronized (upnp_igd_routers) {
			try {
				InetAddress ia = InetAddress.getByName(router.getIdentity().getDescriptorURL().getHost());
				Router r = upnp_igd_routers.remove(ia);
				if (r == null) {
					for (Iterator<Router> it = upnp_igd_routers.values().iterator(); it.hasNext();) {
						Router ro = it.next();
						if (ro.device.getIdentity().equals(router.getIdentity())) {
							r = ro;
							it.remove();
							break;
						}
					}
				}
				if (r == null)
					unkowDeviceDetected = true;
				else {

					RemoteService connectionService;
					if ((connectionService = discoverConnectionService(router)) == null) {
						for (AskForRouterDetectionInformation m : askers_for_router_detection) {
							sendReply(m, new IGDRouterLostMessage(r.internal_address));
						}
					} else {

						if (!r.internal_address.equals(ia)) {
							for (AskForRouterDetectionInformation m : askers_for_router_detection) {
								sendReply(m, new IGDRouterLostMessage(r.internal_address));
								sendReply(m, new IGDRouterFoundMessage(ia));
							}
							r.internal_address = ia;
						}
						r.service = connectionService;
						upnp_igd_routers.put(ia, r);
					}
				}
			} catch (UnknownHostException e) {
				if (getLogger1() != null)
					getLogger1().severeLog("Device updated but impossible to access to its ip address : ", e);
				else
					e.printStackTrace();
			}
		}
		if (unkowDeviceDetected)
			remoteDeviceDetected(router);
	}

	protected void remoteDeviceDetected(RemoteDevice device) {
		try {
			RemoteService connectionService;
			if ((connectionService = discoverConnectionService(device)) == null) {
				return;
			}

			if (getLogger1() != null && getLogger1().isLoggable(Level.FINE))
				getLogger1().fine("Device added : " + device);

			InetAddress ip = InetAddress.getByName(device.getIdentity().getDescriptorURL().getHost());
			addRouter(ip, new Router(ip, device, connectionService));
		} catch (UnknownHostException e) {
			if (getLogger1() != null)
				getLogger1().severeLog("Device added but impossible to access to its ip address : ", e);
			else
				e.printStackTrace();
		}

	}

	protected RemoteService discoverConnectionService(RemoteDevice device) {

		if (!device.getType().getType().equals(IGD)) {
			return null;
		}

		RemoteDevice[] connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE);
		if (connectionDevices.length == 0) {
			if (getLogger1() != null && getLogger1().isLoggable(Level.FINE))
				getLogger1().fine("IGD doesn't support '" + CONNECTION_DEVICE_TYPE + "': " + device);

			return null;
		}

		RemoteDevice connectionDevice = connectionDevices[0];
		if (getLogger1() != null && getLogger1().isLoggable(Level.FINE))
			getLogger1().fine("Using first discovered WAN connection device: " + connectionDevice);

		RemoteService ipConnectionService = (RemoteService) connectionDevice.findService(IP_SERVICE_TYPE);
		if (ipConnectionService == null)
			ipConnectionService = (RemoteService) connectionDevice.findService(IP_SERVICE_TYPE_BIS);
		RemoteService pppConnectionService = (RemoteService) connectionDevice.findService(PPP_SERVICE_TYPE);

		if (ipConnectionService == null && pppConnectionService == null && getLogger1() != null
				&& getLogger1().isLoggable(Level.FINE)) {
			getLogger1().fine("IGD doesn't support IP or PPP WAN connection service: " + device);
		}

		return ipConnectionService != null ? ipConnectionService : pppConnectionService;
	}

	protected Router getRouter(RemoteDevice d) {
		synchronized (upnp_igd_routers) {
			for (Router r : upnp_igd_routers.values())
				if (r.concerns(d))
					return r;
		}
		return null;
	}

	protected Router removeRouter(RemoteDevice d, boolean manual) {
		Router res = null;
		synchronized (upnp_igd_routers) {
			for (Router r : upnp_igd_routers.values()) {
				if (r.concerns(d)) {
					res = r;
					break;
				}
			}
		}
		if (res != null)
			res.setRemoved(manual);
		return res;
	}

	protected Router removeRouter(InetAddress ia, boolean manual) {
		Router res = null;
		synchronized (upnp_igd_routers) {
			res = upnp_igd_routers.remove(ia);
		}
		if (res != null)
			res.setRemoved(manual);
		return res;
	}

	protected void removeAllRouters(boolean manual) {
		synchronized (upnp_igd_routers) {
			for (Router r : upnp_igd_routers.values()) {
				r.setRemoved(manual);
			}
			upnp_igd_routers.clear();
		}
	}

	protected class Router {
		protected volatile InetAddress internal_address;
		protected final AtomicReference<InetAddress> external_address = new AtomicReference<>(null);
		protected final RemoteDevice device;
		protected volatile RemoteService service;
		protected final AtomicReference<StatusInfo> status = new AtomicReference<>(null);
		private final AtomicBoolean removed = new AtomicBoolean(false);

		private Task<Object> status_task_updater = null;
		private TaskID status_task_id = null;
		private Task<Object> external_address_task_updater = null;
		private TaskID external_address_task_id = null;

		protected final ArrayList<AskForConnectionStatusMessage> asks_for_status = new ArrayList<>();
		protected final ArrayList<AskForExternalIPMessage> asks_for_external_ip = new ArrayList<>();
		protected final ArrayList<PortMapping> desired_mappings = new ArrayList<>();

		protected Router(InetAddress _internal_address, RemoteDevice _remote_device, RemoteService _service) {
			if (_internal_address == null)
				throw new NullPointerException("_internal_address");
			if (_remote_device == null)
				throw new NullPointerException("_remote_device");
			if (_service == null)
				throw new NullPointerException("_service");

			internal_address = _internal_address;
			device = _remote_device;
			service = _service;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o instanceof Router)
				return device.equals(((Router) o).device);
			else
				return false;
		}

		public boolean concerns(RemoteDevice device) {
			if (device == null)
				return false;
			return this.device.equals(device);
		}

		@Override
		public int hashCode() {
			return device.hashCode();
		}

		void setRemoved(boolean manual) {
			if (status_task_id != null) {
				cancelTask(status_task_id, false);
				status_task_id = null;
			}
			if (external_address_task_id != null) {
				cancelTask(external_address_task_id, false);
				external_address_task_id = null;
			}
			if (!removed.get()) {
				removed.set(true);

				ConnexionStatusMessage answer = new ConnexionStatusMessage(internal_address,
						new StatusInfo(Status.Disconnected, 0, null), status.get());
				synchronized (asks_for_status) {
					for (AskForConnectionStatusMessage m : asks_for_status) {
						UpnpIGDAgent.this.sendReply(m, answer.clone());
					}
				}

				UpnpIGDAgent.this.broadcastMessage(LocalCommunity.Groups.NETWORK,
						LocalCommunity.Roles.LOCAL_NETWORK_AFFECTATION_ROLE, new IGDRouterLostMessage(internal_address),
						false);

				if (manual) {
					removeAllPointMapping();
				} else {
					synchronized (desired_mappings) {
						if (desired_mappings.size() > 0)
							handleFailureMessage(
									"Device disappeared, couldn't delete port mappings: " + desired_mappings.size());
					}

				}
			}
		}

		public void newMessage(final AskForConnectionStatusMessage m) {
			if (UpnpIGDAgent.upnpService != null && !removed.get()) {
				boolean cancel = false;
				boolean update_repetitive_task = false;
				long referenced_delay = -1;
				if (m.isRepetitive()) {
					synchronized (asks_for_status) {
						asks_for_status.add(m);
						if (status_task_id != null && (status_task_updater == null
								|| status_task_updater.getDurationBetweenEachRepetition() > m.getDelay())) {
							UpnpIGDAgent.this.cancelTask(status_task_id, false);
							status_task_id = null;
							status_task_updater = null;
						}
						if (status_task_updater == null) {
							update_repetitive_task = true;
							referenced_delay = m.getDelay();
						}
					}
				} else {
					synchronized (asks_for_status) {
						long delay = -1;
						Iterator<AskForConnectionStatusMessage> it = asks_for_status.iterator();
						while (it.hasNext()) {
							AskForConnectionStatusMessage tmpm = it.next();
							if (tmpm.getSender().equals(m.getSender())) {
								delay = tmpm.getDelay();
								it.remove();
								cancel = true;
								break;
							}
						}

						if (cancel) {
							if (delay == status_task_updater.getDurationBetweenEachRepetition()
									|| asks_for_status.size() == 0) {
								UpnpIGDAgent.this.cancelTask(status_task_id, false);
								status_task_id = null;
								status_task_updater = null;
							}
							if (asks_for_status.size() > 0) {
								referenced_delay = -1;
								for (AskForConnectionStatusMessage tmpm : asks_for_status) {
									if (tmpm.getDelay() > referenced_delay)
										referenced_delay = tmpm.getDelay();
								}
								update_repetitive_task = true;
							}
						}
					}
				}
				if (update_repetitive_task) {
					status_task_updater = new Task<>(new Callable<Object>() {

						@Override
						public Object call() throws Exception {
							upnpService.getControlPoint().execute(new GetStatusInfo(service) {

								@Override
								public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
										UpnpResponse _operation, String _defaultMsg) {
									if (getLogger1() != null)
										getLogger1().warning(_defaultMsg);
								}

								@Override
								protected void success(StatusInfo _statusInfo) {
									StatusInfo old = status.get();
									if (old == null || !old.equals(_statusInfo)) {
										ConnexionStatusMessage answer = new ConnexionStatusMessage(internal_address,
												_statusInfo, old);
										status.set(_statusInfo);
										synchronized (asks_for_status) {
											for (Iterator<AskForConnectionStatusMessage> it = asks_for_status
													.iterator(); it.hasNext();) {
												AskForConnectionStatusMessage m = it.next();
												if (!UpnpIGDAgent.this.sendReply(m, answer.clone())
														.equals(ReturnCode.SUCCESS)) {
													it.remove();
												}
											}
										}
									}

								}
							});
							return null;
						}
					}, System.currentTimeMillis() + referenced_delay, referenced_delay);
					status_task_id = UpnpIGDAgent.this.scheduleTask(status_task_updater);
				}

				upnpService.getControlPoint().execute(new GetStatusInfo(service) {

					@Override
					public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
							UpnpResponse _operation, String _defaultMsg) {

						if (getLogger1() != null)
							getLogger1().warning(_defaultMsg);
						ConnexionStatusMessage answer = new ConnexionStatusMessage(internal_address, null,
								status.get());
						answer.setMessage(_defaultMsg);
						UpnpIGDAgent.this.sendReply(m, answer);
					}

					@Override
					protected void success(StatusInfo _statusInfo) {
						UpnpIGDAgent.this.sendReply(m, new ConnexionStatusMessage(internal_address, _statusInfo, null));
					}
				});
			}
		}

		public void newMessage(final AskForExternalIPMessage m) {
			if (UpnpIGDAgent.upnpService != null && !removed.get()) {
				boolean cancel = false;
				boolean update_repetitive_task = false;
				long referenced_delay = -1;
				if (m.isRepetitive()) {
					synchronized (asks_for_external_ip) {
						asks_for_external_ip.add(m);
						if (external_address_task_updater != null
								&& external_address_task_updater.getDurationBetweenEachRepetition() > m.getDelay()) {
							UpnpIGDAgent.this.cancelTask(external_address_task_id, false);
							external_address_task_id = null;
							external_address_task_updater = null;
						}
						if (external_address_task_updater == null) {
							update_repetitive_task = true;
							referenced_delay = m.getDelay();
						}
					}
				} else {
					synchronized (asks_for_external_ip) {
						long delay = -1;
						Iterator<AskForExternalIPMessage> it = asks_for_external_ip.iterator();
						while (it.hasNext()) {
							AskForExternalIPMessage tmpm = it.next();
							if (tmpm.getSender().equals(m.getSender())) {
								delay = tmpm.getDelay();
								it.remove();
								cancel = true;
								break;
							}
						}

						if (cancel) {
							if (delay == external_address_task_updater.getDurationBetweenEachRepetition()
									|| asks_for_external_ip.size() == 0) {
								UpnpIGDAgent.this.cancelTask(external_address_task_id, false);
								external_address_task_id = null;
								external_address_task_updater = null;
							}
							if (asks_for_external_ip.size() > 0) {
								referenced_delay = -1;
								for (AskForExternalIPMessage tmpm : asks_for_external_ip) {
									if (tmpm.getDelay() > referenced_delay)
										referenced_delay = tmpm.getDelay();
								}
								update_repetitive_task = true;
							}
						}
					}
				}
				if (update_repetitive_task) {
					external_address_task_updater = new Task<>(new Callable<Object>() {

						@Override
						public Object call() throws Exception {
							upnpService.getControlPoint().execute(new GetExternalIP(service) {

								@Override
								public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
										UpnpResponse _operation, String _defaultMsg) {
									if (getLogger1() != null)
										getLogger1().warning(_defaultMsg);
								}

								@Override
								protected void success(String _externalIPAddress) {
									try {
										InetAddress old = external_address.get();
										InetAddress newaddress = InetAddress.getByName(_externalIPAddress);
										if (old == null || !old.equals(newaddress)) {
											ExternalIPMessage answer = new ExternalIPMessage(internal_address,
													newaddress, old);
											external_address.set(newaddress);
											synchronized (asks_for_external_ip) {
												for (Iterator<AskForExternalIPMessage> it = asks_for_external_ip
														.iterator(); it.hasNext();) {
													AskForExternalIPMessage m = it.next();
													if (!UpnpIGDAgent.this.sendReply(m, answer.clone())
															.equals(ReturnCode.SUCCESS))
														it.remove();
												}
											}
										}
									} catch (Exception e) {
										if (getLogger1() != null) {
											getLogger1().severeLog("Unexpected exception :", e);
										}
									}
								}

							});
							return null;
						}
					}, System.currentTimeMillis() + referenced_delay, referenced_delay);
					external_address_task_id = UpnpIGDAgent.this.scheduleTask(external_address_task_updater);
				}

				upnpService.getControlPoint().execute(new GetExternalIP(service) {

					@Override
					public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
							UpnpResponse _operation, String _defaultMsg) {

						if (getLogger1() != null)
							getLogger1().warning(_defaultMsg);
						ExternalIPMessage answer = new ExternalIPMessage(internal_address, null,
								external_address.get());
						answer.setMessage(_defaultMsg);
						UpnpIGDAgent.this.sendReply(m, answer);
					}

					@Override
					protected void success(String _externalIPAddress) {
						try {
							UpnpIGDAgent.this.sendReply(m, new ExternalIPMessage(internal_address,
									InetAddress.getByName(_externalIPAddress), null));
						} catch (Exception e) {
							if (getLogger1() != null) {
								getLogger1().severeLog("Unexpected exception :", e);
							}
						}
					}
				});
			}
		}

		public void newMessage(final AskForPortMappingAddMessage m) {
			if (UpnpIGDAgent.upnpService != null && !removed.get()) {
				synchronized (desired_mappings) {
					for (PortMapping pm : desired_mappings) {
						if (pm.getInternalPort().getValue().longValue() == m.getInternalPort()) {
							for (int i : m.getExternalPortsRange()) {
								if (i == pm.getExternalPort().getValue().longValue()) {
									UpnpIGDAgent.this.sendReply(m,
											new PortMappingAnswerMessage(m.getConcernedRouter(),
													m.getConcernedLocalAddress(), i, m.getInternalPort(),
													m.getDescription(), m.getProtocol(), MappingReturnCode.SUCESS));
									return;
								}
							}
						}
					}
				}
				final AtomicInteger index = new AtomicInteger(0);
				final PortMapping pm = new PortMapping();
				pm.setDescription(m.getDescription());
				pm.setEnabled(true);
				pm.setExternalPort(new UnsignedIntegerTwoBytes(m.getExternalPortsRange()[index.getAndIncrement()]));
				pm.setInternalPort(new UnsignedIntegerTwoBytes(m.getInternalPort()));
				pm.setProtocol(m.getProtocol());
				pm.setInternalClient(m.getConcernedLocalAddress().getHostAddress());
				upnpService.getControlPoint().execute(new PortMappingAdd(service, pm) {

					@Override
					public void success(@SuppressWarnings("rawtypes") ActionInvocation _invocation) {
						synchronized (desired_mappings) {
							desired_mappings.add(pm);
						}
						UpnpIGDAgent.this.sendReply(m,
								new PortMappingAnswerMessage(m.getConcernedRouter(), m.getConcernedLocalAddress(),
										m.getExternalPortsRange()[index.get() - 1], m.getInternalPort(),
										m.getDescription(), m.getProtocol(), MappingReturnCode.SUCESS));
					}

					@Override
					public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
							UpnpResponse _operation, String _defaultMsg) {
						if (index.get() < m.getExternalPortsRange().length) {
							pm.setExternalPort(
									new UnsignedIntegerTwoBytes(m.getExternalPortsRange()[index.getAndIncrement()]));
							upnpService.getControlPoint().execute(this);
						} else {
							if (_defaultMsg.toLowerCase().contains("authorized"))
								UpnpIGDAgent.this.sendReply(m,
										new PortMappingAnswerMessage(m.getConcernedRouter(),
												m.getConcernedLocalAddress(), -1, m.getInternalPort(),
												m.getDescription(), m.getProtocol(), MappingReturnCode.ACCESS_DENIED));
							else
								UpnpIGDAgent.this.sendReply(m, new PortMappingAnswerMessage(m.getConcernedRouter(),
										m.getConcernedLocalAddress(), -1, m.getInternalPort(), m.getDescription(),
										m.getProtocol(), MappingReturnCode.CONFLICTUAL_PORT_AND_IP));
						}

					}
				});
			}
		}

		private void removeAllPointMapping() {
			if (UpnpIGDAgent.upnpService != null) {
				synchronized (desired_mappings) {

					for (final PortMapping pm : desired_mappings) {
						upnpService.getControlPoint().execute(new PortMappingDelete(service, pm) {

							@Override
							public void success(@SuppressWarnings("rawtypes") ActionInvocation _invocation) {
								synchronized (desired_mappings) {
									desired_mappings.remove(pm);
								}
							}

							@Override
							public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
									UpnpResponse _operation, String _defaultMsg) {
								handleFailureMessage("Impossible to remove port mapping : " + _defaultMsg);
							}
						});
					}
					desired_mappings.clear();
				}
			}

		}

		public void newMessage(AskForPortMappingDeleteMessage m) {
			if (UpnpIGDAgent.upnpService != null && !removed.get()) {
				PortMapping pmfound = null;
				synchronized (desired_mappings) {

					for (PortMapping pm : desired_mappings) {
						if (pm.getExternalPort().getValue().longValue() == m.getExternalPort()
								&& pm.getProtocol().equals(m.getProtocol())) {
							pmfound = pm;
							break;
						}
					}
				}
				if (pmfound != null) {
					final PortMapping pm = pmfound;
					upnpService.getControlPoint().execute(new PortMappingDelete(service, pm) {

						@Override
						public void success(@SuppressWarnings("rawtypes") ActionInvocation _invocation) {
							synchronized (desired_mappings) {
								desired_mappings.remove(pm);
							}
						}

						@Override
						public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
								UpnpResponse _operation, String _defaultMsg) {
							handleFailureMessage("Impossible to remove port mapping : " + _defaultMsg);
						}
					});
				}
			}
		}

		public void removeAllPortMappings() {
			if (UpnpIGDAgent.upnpService != null && !removed.get()) {
				synchronized (desired_mappings) {
					for (final PortMapping pm : desired_mappings) {
						upnpService.getControlPoint().execute(new PortMappingDelete(service, pm) {

							@Override
							public void success(@SuppressWarnings("rawtypes") ActionInvocation _invocation) {
								synchronized (desired_mappings) {
									desired_mappings.remove(pm);
								}
							}

							@Override
							public void failure(@SuppressWarnings("rawtypes") ActionInvocation _invocation,
									UpnpResponse _operation, String _defaultMsg) {
								handleFailureMessage("Impossible to remove port mapping : " + _defaultMsg);
							}
						});
					}
				}
			}
		}

	}

	private UpnpIGDAgent() {
		super();
		// setName("UpnpIGDAgent");
	}

	/**
	 * Returns an instance of UpnpIGDAgent.
	 * 
	 * @return an instance of UpnpIGDAgent.
	 */
	public static UpnpIGDAgent getInstance() {
		return new UpnpIGDAgent();
	}

	protected void activate() {
		setLogLevel(getMadkitConfig().networkProperties.UpnpIGDLogLevel);
		// loggerModified(logger);
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("Launching UPNPIGDAgent ...");
		requestRole(LocalCommunity.Groups.NETWORK, LocalCommunity.Roles.LOCAL_NETWORK_EXPLORER_ROLE);
		if (getMadkitConfig().networkProperties.upnpIGDEnabled) {
			synchronized (UpnpIGDAgent.class) {
				if (upnpService == null) {
					upnpService = new UpnpServiceImpl(new DefaultUpnpServiceConfiguration(getMadkitConfig().networkProperties.upnpIDGPort));
				}

				pointedUpnpServiceNumber++;
				upnpService.getRegistry().addListener(registeryListener);
				for (RemoteDevice d : upnpService.getRegistry().getRemoteDevices()) {
					remoteDeviceDetected(d);
				}
				if (pointedUpnpServiceNumber == 1) {
					upnpService.getControlPoint().search();
				}

			}
		}
		if (logger != null && logger.isLoggable(Level.FINE))
			logger.fine("UPNPIGDAgent launched !");
	}

	@Override
	protected void loggerModified(AgentLogger logger) {

		if (logger == null) {
			for (String ls : sub_loggers_names) {
				Logger log = Logger.getLogger(ls);
				log.setParent(Logger.getGlobal());
				log.setUseParentHandlers(false);
				log.setLevel(Level.OFF);
			}
		} else {
			for (String ls : sub_loggers_names) {
				Logger log = Logger.getLogger(ls);
				log.setParent(logger);
				log.setUseParentHandlers(true);
				log.setLevel(logger.getLevel());
			}
		}

	}

	@Override
	protected void end() {
		removeAllRouters(true);
		network_interface_info.shutdown();
		if (getMadkitConfig().networkProperties.upnpIGDEnabled && upnpService != null) {
			synchronized (UpnpIGDAgent.class) {
				upnpService.getRegistry().removeListener(registeryListener);
				if (--pointedUpnpServiceNumber == 0) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						if (logger != null)
							logger.severeLog("", e);
						else
							e.printStackTrace();
					}
					upnpService.shutdown();
					upnpService = null;
				}
			}
		}
		if (logger != null)
			logger.fine("UPNPIGDAgent killed !");

	}

	public static long getHardwareAddress(byte hardwareAddress[]) {
		long result = 0;
		if (hardwareAddress != null) {
			for (final byte value : hardwareAddress) {
				result <<= 8;
				result |= value & 255;
			}
		}
		return result;
	}

	protected class NetworkInterfaceInfo {

		ArrayList<NetworkInterface> network_interfaces = new ArrayList<>();
		long min_delay = -1;
		HashMap<AgentAddress, AskForNetworkInterfacesMessage> askers = new HashMap<>();
		TaskID task_id = null;
		Task<Object> task = null;
		private boolean shutdown = false;

		ArrayList<NetworkInterface> init() {

			try {
				Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
				ArrayList<NetworkInterface> network_interfaces = new ArrayList<>();
				while (e.hasMoreElements()) {
					NetworkInterface ni = e.nextElement();
					if (isValid(ni)) {
						network_interfaces.add(ni);
					}
				}
				return network_interfaces;
			} catch (SocketException e1) {
			}
			return new ArrayList<>();
		}

		boolean isValid(NetworkInterface ni) throws SocketException {
			long addr = getHardwareAddress(ni.getHardwareAddress());
			return ni.isUp() && (addr != 0 || ni.isLoopback()) && addr != 224;
		}

		void addAsker(AskForNetworkInterfacesMessage _message) {
			if (!getMadkitConfig().networkProperties.networkInterfaceScan)
				return;
			synchronized (this) {
				if (shutdown)
					return;
				boolean removed = false;
				long old_delay = min_delay;
				if (askers.remove(_message.getSender()) != null) {
					min_delay = -1;
					removed = true;
				}
				if (_message.isRepetitive()) {
					askers.put(_message.getSender(), _message);
					min_delay = -1;
				}
				if (min_delay == -1 && askers.size() > 0) {
					min_delay = Long.MAX_VALUE;

					for (AskForNetworkInterfacesMessage afni : askers.values()) {
						if (min_delay > afni.getDelay())
							min_delay = afni.getDelay();
					}
					if (min_delay < 0)
						min_delay = -1;
				}
				if (old_delay == -1 || old_delay > min_delay) {
					if (old_delay != -1) {
						cancelTask(task_id, false);
						task_id = null;
					}

					if (min_delay != -1) {
						task = new Task<Object>(new Callable<Object>() {

							@Override
							public Object call() throws Exception {
								synchronized (UpnpIGDAgent.NetworkInterfaceInfo.this) {
									ArrayList<NetworkInterface> cur_nis = init();
									ArrayList<NetworkInterface> new_nis = new ArrayList<>();
									ArrayList<NetworkInterface> del_nis = new ArrayList<>();

									for (NetworkInterface ni : network_interfaces) {
										boolean found = false;
										for (NetworkInterface ni2 : cur_nis) {
											if (ni.equals(ni2)) {
												found = true;
												break;
											}
										}
										if (!found)
											del_nis.add(ni);
									}

									for (NetworkInterface ni : cur_nis) {
										boolean found = false;
										for (NetworkInterface ni2 : network_interfaces) {
											if (ni.equals(ni2)) {
												found = true;
												break;
											}
										}
										if (!found)
											new_nis.add(ni);
									}

									network_interfaces = cur_nis;
									if (new_nis.size() != 0 || del_nis.size() != 0) {
										for (Iterator<AskForNetworkInterfacesMessage> it = askers.values()
												.iterator(); it.hasNext();) {
											AskForNetworkInterfacesMessage m = it.next();
											if (!sendReply(m, new NetworkInterfaceInformationMessage(network_interfaces,
													new_nis, del_nis)).equals(ReturnCode.SUCCESS))
												it.remove();

										}
									}
									return null;
								}
							}
						}, old_delay != -1 ? old_delay : min_delay, min_delay);
						task_id = scheduleTask(task);
					}
				}

				if (_message.isRepetitive()) {
					UpnpIGDAgent.this.sendReply(_message, new NetworkInterfaceInformationMessage(network_interfaces,
							network_interfaces, new ArrayList<NetworkInterface>()));
				} else if (!removed) {
					Collection<NetworkInterface> c = init();
					UpnpIGDAgent.this.sendReply(_message,
							new NetworkInterfaceInformationMessage(c, c, new ArrayList<NetworkInterface>()));
				}
			}
		}

		void shutdown() {
			synchronized (this) {
				if (task_id != null)
					cancelTask(this.task_id, false);
				this.askers.clear();
				this.min_delay = -1;
				this.task = null;
				this.task_id = null;
				shutdown = true;
			}
		}
	}

	private final NetworkInterfaceInfo network_interface_info = new NetworkInterfaceInfo();

	AgentLogger getLogger1() {
		return logger;
	}

	void stopNetwork() {
		if (this.getState().compareTo(State.ENDING) < 0)
			this.killAgent(this);
	}

	@Override
	protected void liveByStep(Message _message) {
		if (_message == null)
			return;
		if (_message.getClass() == KernelMessage.class) {
			proceedEnumMessage((KernelMessage) _message);
		} else if (_message instanceof UpnpIGDAgent.AskForConnectionStatusMessage) {
			AskForConnectionStatusMessage m = (AskForConnectionStatusMessage) _message;

			Router r = null;
			synchronized (upnp_igd_routers) {
				r = upnp_igd_routers.get(m.getConcernedRouter());
			}
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Managing message : " + m);
				r.newMessage(m);
			} else {
				handleFailureMessage("Trying to ask connection status for a router which does not exists : " + m);
				sendReply(m, new ConnexionStatusMessage(m.getConcernedRouter(),
						new StatusInfo(Status.Unconfigured, 0, null), new StatusInfo(Status.Unconfigured, 0, null)));
			}
		} else if (_message instanceof UpnpIGDAgent.AskForExternalIPMessage) {
			AskForExternalIPMessage m = (AskForExternalIPMessage) _message;
			Router r = null;
			synchronized (upnp_igd_routers) {
				r = upnp_igd_routers.get(m.getConcernedRouter());
			}
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Managing message : " + m);
				r.newMessage(m);
			} else {
				handleFailureMessage(
						"Trying to ask external ip message considering a router which does not exists : " + m);
			}
		} else if (_message instanceof UpnpIGDAgent.AskForNetworkInterfacesMessage) {
			if (logger != null && logger.isLoggable(Level.FINER))
				logger.finer("Managing message : " + _message);

			network_interface_info.addAsker((UpnpIGDAgent.AskForNetworkInterfacesMessage) _message);
		} else if (_message instanceof UpnpIGDAgent.AskForPortMappingAddMessage) {
			AskForPortMappingAddMessage m = (AskForPortMappingAddMessage) _message;
			Router r = null;
			synchronized (upnp_igd_routers) {
				r = upnp_igd_routers.get(m.getConcernedRouter());
			}
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Managing message : " + m);

				r.newMessage(m);
			} else {
				handleFailureMessage("Trying to add a port mapping considering a router which does not exists : " + m);
				sendReply(m, new PortMappingAnswerMessage(m.getConcernedRouter(), m.getConcernedLocalAddress(), -1,
						m.getInternalPort(), m.getDescription(), m.getProtocol(), MappingReturnCode.UNKNOWN));
			}

		} else if (_message instanceof UpnpIGDAgent.AskForPortMappingDeleteMessage) {
			AskForPortMappingDeleteMessage m = (AskForPortMappingDeleteMessage) _message;
			Router r = null;
			synchronized (upnp_igd_routers) {
				r = upnp_igd_routers.get(m.getConcernedRouter());
			}
			if (r != null) {
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Managing message : " + m);

				r.newMessage(m);
			} else {
				handleFailureMessage(
						"Trying to remove a port mapping considering a router which does not exists : " + m);
			}
		} else if (_message instanceof AskForRouterDetectionInformation) {
			synchronized (upnp_igd_routers) {
				AskForRouterDetectionInformation m = (AskForRouterDetectionInformation) _message;
				if (logger != null && logger.isLoggable(Level.FINER))
					logger.finer("Managing message : " + m);

				for (Router r : upnp_igd_routers.values())
					sendReply(m, new IGDRouterFoundMessage(r.internal_address));

				if (m.permanent_request) {
					boolean found = false;
					for (AskForRouterDetectionInformation m2 : askers_for_router_detection) {
						if (m2.getSender().equals(m.getSender())) {
							found = true;
						}
					}
					if (!found)
						askers_for_router_detection.add(m);
				} else {
					for (Iterator<AskForRouterDetectionInformation> it = askers_for_router_detection.iterator(); it
							.hasNext();) {
						if (it.next().getSender().equals(m.getSender()))
							it.remove();
					}
				}
			}
		} else if (_message instanceof NetworkAgent.StopNetworkMessage) {
			this.killAgent(this);
		}
	}

	protected void handleFailureMessage(String s) {
		if (logger != null)
			logger.warning(s);
	}

	protected static final String IGD = "InternetGatewayDevice";
	protected static final DeviceType IGD_DEVICE_TYPE_1 = new UDADeviceType(IGD, 1);
	protected static final DeviceType IGD_DEVICE_TYPE_2 = new UDADeviceType("InternetGatewayDevice", 2);
	protected static final DeviceType CONNECTION_DEVICE_TYPE = new UDADeviceType("WANConnectionDevice", 1);

	protected static final ServiceType IP_SERVICE_TYPE = new UDAServiceType("WANIPConnection", 1);
	protected static final ServiceType IP_SERVICE_TYPE_BIS = new UDAServiceType("WANIPConn", 1);
	protected static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);

	protected class RegistryListener extends DefaultRegistryListener {

		public RegistryListener() {
			super();
		}

		@Override
		public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {

			updateRouter(device);
		}

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			remoteDeviceDetected(device);
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			if (device instanceof RemoteDevice) {
				RemoteDevice rd = (RemoteDevice) device;

				try {
					InetAddress ia = InetAddress.getByName(rd.getIdentity().getDescriptorURL().getHost());
					removeRouter(ia, false);
				} catch (UnknownHostException e) {
					if (getLogger1() != null)
						getLogger1().severeLog("Device removed but impossible to access to its ip address : ", e);
					else
						e.printStackTrace();
				}
			}
		}

		@Override
		synchronized public void beforeShutdown(Registry registry) {
			removeAllRouters(false);
		}

	}

	enum MappingReturnCode {
		SUCESS, CONFLICTUAL_PORT_AND_IP, ACCESS_DENIED, UNKNOWN
	}

	public static abstract class AbstractRouterMessage extends Message {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6352496613545768242L;

		private final InetAddress concerned_router;
		private String message;

		protected AbstractRouterMessage(InetAddress _concerned_router) {
			if (_concerned_router == null)
				throw new NullPointerException("_concerned_router");
			concerned_router = _concerned_router;
		}

		public InetAddress getConcernedRouter() {
			return concerned_router;
		}

		protected void setMessage(String _message) {
			message = _message;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[concernedRouter=" + concerned_router + ", message=" + message + "]";
		}

	}

	public static class AskForPortMappingAddMessage extends AbstractRouterMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = 178159584387889945L;

		private final InetAddress concerned_local_ip;
		private final int external_ports_range[];
		private final int internal_port;
		private final String description;
		private final Protocol protocol;

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[concernedRouter=" + getConcernedRouter() + ", message=" + getMessage()
					+ ", concerned_local_ip" + concerned_local_ip + ", internalPort=" + internal_port + ", protocol="
					+ protocol + ", externalPortRange=" + external_ports_range + "]";
		}

		public AskForPortMappingAddMessage(InetAddress _concerned_router, InetAddress _concerned_local_ip,
				List<Integer> external_ports_range, int _internal_port, String _description, Protocol _protocol) {
			super(_concerned_router);
			if (_concerned_local_ip == null)
				throw new NullPointerException("_concerned_local_ip");

			concerned_local_ip = _concerned_local_ip;
			if (external_ports_range == null)
				throw new NullPointerException("external_ports_range");
			if (external_ports_range.isEmpty())
				throw new IllegalArgumentException("external_ports_range is empty");
			int nb = 0;
			for (Integer i : external_ports_range) {
				if (i != null)
					nb++;
			}
			if (nb == 0)
				throw new IllegalArgumentException("external_ports_range is empty or has no valid port");
			this.external_ports_range = new int[nb];
			int index = 0;
			for (Integer i : external_ports_range) {
				if (i != null)
					this.external_ports_range[index++] = i.intValue();
			}

			internal_port = _internal_port;
			description = _description;
			protocol = _protocol;
		}

		public int[] getExternalPortsRange() {
			return external_ports_range;
		}

		public InetAddress getConcernedLocalAddress() {
			return concerned_local_ip;
		}

		public int getInternalPort() {
			return internal_port;
		}

		public String getDescription() {
			return description;
		}

		public Protocol getProtocol() {
			return protocol;
		}

	}

	public static class AskForPortMappingDeleteMessage extends AbstractRouterMessage {
		/**
		* 
		*/
		private static final long serialVersionUID = -1710573952608534629L;

		private final int external_port;
		private final Protocol protocol;

		public AskForPortMappingDeleteMessage(InetAddress _concerned_router, int _external_port, Protocol _protocol) {
			super(_concerned_router);
			external_port = _external_port;
			protocol = _protocol;
		}

		public int getExternalPort() {
			return external_port;
		}

		public Protocol getProtocol() {
			return protocol;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[concernedRouter=" + getConcernedRouter() + ", message=" + getMessage()
					+ ", protocol=" + protocol + ", externalPort=" + external_port + "]";
		}

	}

	public static class PortMappingAnswerMessage extends AbstractRouterMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8432675044477908723L;

		private final InetAddress concerned_local_ip;
		private final int external_port;
		private final int internal_port;
		private final String description;
		private final MappingReturnCode return_code;
		private final Protocol protocol;

		public PortMappingAnswerMessage(InetAddress _concerned_router, InetAddress _concerned_local_ip,
				int _external_port, int _internal_port, String _description, Protocol _protocol,
				MappingReturnCode _return_code) {
			super(_concerned_router);
			concerned_local_ip = _concerned_local_ip;
			external_port = _external_port;
			internal_port = _internal_port;
			description = _description;
			return_code = _return_code;
			protocol = _protocol;
		}

		public InetAddress getConcernedLocalAddress() {
			return concerned_local_ip;
		}

		public int getInternalPort() {
			return internal_port;
		}

		public int getExternalPort() {
			return external_port;
		}

		public String getDescription() {
			return description;
		}

		public MappingReturnCode getReturnCode() {
			return return_code;
		}

		public Protocol getProtocol() {
			return protocol;
		}
	}

	public static abstract class RepetitiveRouterRequest extends AbstractRouterMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8856616652000136067L;

		private long delay;

		protected RepetitiveRouterRequest(InetAddress _concerned_router) {
			this(_concerned_router, -1);
		}

		protected RepetitiveRouterRequest(InetAddress _concerned_router, long _delay_between_each_check) {
			super(_concerned_router);
			delay = _delay_between_each_check;
		}

		public long getDelay() {
			return delay;
		}

		public boolean isRepetitive() {
			return delay > 0;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[concernedRouter=" + getConcernedRouter() + ", delay=" + delay + "]";
		}
	}

	public static class AskForConnectionStatusMessage extends RepetitiveRouterRequest {
		/**
		 * 
		 */
		private static final long serialVersionUID = 703616198132746998L;

		public AskForConnectionStatusMessage(InetAddress _concerned_router) {
			super(_concerned_router);
		}

		public AskForConnectionStatusMessage(InetAddress _concerned_router, long _delay_between_each_check) {
			super(_concerned_router, _delay_between_each_check);
		}

	}

	public static class ConnexionStatusMessage extends AbstractRouterMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8819060658814969370L;

		private final StatusInfo status;
		private final StatusInfo old_status;

		public ConnexionStatusMessage(InetAddress _concerned_router, StatusInfo _status, StatusInfo _old_status) {
			super(_concerned_router);
			status = _status;
			old_status = _old_status;
		}

		public StatusInfo getStatus() {
			return status;
		}

		public StatusInfo getOldStatus() {
			return old_status;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[concernedRouter=" + getConcernedRouter() + ", message=" + getMessage()
					+ ", status=" + status + ", oldStatus=" + old_status + "]";
		}

	}

	public static class AskForExternalIPMessage extends RepetitiveRouterRequest {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3525681704187524583L;

		public AskForExternalIPMessage(InetAddress _concerned_router) {
			super(_concerned_router);
		}

		public AskForExternalIPMessage(InetAddress _concerned_router, long _delay_between_each_check) {
			super(_concerned_router, _delay_between_each_check);
		}
	}

	public static class ExternalIPMessage extends AbstractRouterMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3902169716732967854L;

		private final InetAddress external_ip, old_ip;

		public ExternalIPMessage(InetAddress _concerned_router, InetAddress _external_ip, InetAddress _old_ip) {
			super(_concerned_router);
			external_ip = _external_ip;
			old_ip = _old_ip;
		}

		public InetAddress getExternalIP() {
			return external_ip;
		}

		public InetAddress getOldIP() {
			return old_ip;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[concernedRouter=" + getConcernedRouter() + ", message=" + getMessage()
					+ "externalIP=" + external_ip + ", oldIP=" + old_ip + "]";
		}

	}

	public static class AskForNetworkInterfacesMessage extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6619851730772542543L;

		private final long delay;

		public AskForNetworkInterfacesMessage(long _delay_between_each_check) {
			delay = _delay_between_each_check;
		}

		public long getDelay() {
			return delay;
		}

		public boolean isRepetitive() {
			return delay > 0;
		}

		@Override
		public String toString() {
			return "AskForNetworkInterfacesMessage[delay=" + delay + "]";
		}

	}

	public static class NetworkInterfaceInformationMessage extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1978259818362981706L;

		private final Collection<NetworkInterface> connected_interfaces, new_connected_interfaces,
				new_disconnected_interfaces;

		public NetworkInterfaceInformationMessage(Collection<NetworkInterface> _connected_interfaces,
				Collection<NetworkInterface> _new_connected_interfaces,
				Collection<NetworkInterface> _new_disconnected_interfaces) {
			connected_interfaces = _connected_interfaces;
			new_connected_interfaces = _new_connected_interfaces;
			new_disconnected_interfaces = _new_disconnected_interfaces;
		}

		public Collection<NetworkInterface> getConnectedInterfaces() {
			return connected_interfaces;
		}

		public Collection<NetworkInterface> getNewConnectedInterfaces() {
			return new_connected_interfaces;
		}

		public Collection<NetworkInterface> getNewDisconnectedInterfaces() {
			return new_disconnected_interfaces;
		}

		@Override
		public String toString() {
			return "NetworkInterfaceInformationMessage[new_connected_interfaces=" + new_connected_interfaces
					+ ", new_disconnected_interfaces=" + new_disconnected_interfaces + "]";
		}
	}

	/*
	 * protected class NewDeviceReceived extends Message { private static final long
	 * serialVersionUID = 1L;
	 * 
	 * final RemoteDevice device; final RemoteService service; final InetAddress
	 * local_ip;
	 * 
	 * NewDeviceReceived(RemoteDevice _device, RemoteService _service, InetAddress
	 * _local_ip) { device=_device; service=_service; local_ip=_local_ip; } }
	 * 
	 * protected class NewDeviceRemoved extends Message { private static final long
	 * serialVersionUID = 1L;
	 * 
	 * final RemoteDevice device;
	 * 
	 * NewDeviceRemoved(RemoteDevice _device) { device=_device; } }
	 */

	public static class AskForRouterDetectionInformation extends Message {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1692219215996867194L;

		final boolean permanent_request;

		public AskForRouterDetectionInformation(boolean permanent_request) {
			this.permanent_request = permanent_request;
		}

		@Override
		public String toString() {
			return "AskForRouterDetectionInformation[permanentRequest=" + permanent_request + "]";
		}
	}

	public static class IGDRouterFoundMessage extends AbstractRouterMessage {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4787334473964343304L;

		public IGDRouterFoundMessage(InetAddress _concerned_router) {
			super(_concerned_router);
		}
	}

	public static class IGDRouterLostMessage extends AbstractRouterMessage {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5839981501136915354L;

		protected IGDRouterLostMessage(InetAddress _concerned_router) {
			super(_concerned_router);
		}

	}

}

class NONAndroidUpnpServiceConfiguration extends org.fourthline.cling.DefaultUpnpServiceConfiguration {
	/**
	 * Defaults to port '0', ephemeral.
	 */
	public NONAndroidUpnpServiceConfiguration() {
		super();
	}

	public NONAndroidUpnpServiceConfiguration(int streamListenPort) {
		super(streamListenPort);
	}

	protected ExecutorService createDefaultExecutorService() {
		return UpnpIGDAgent.serviceExecutor;
	}
}

class AndroidUpnpServiceConfiguration extends org.fourthline.cling.android.AndroidUpnpServiceConfiguration {
	/**
	 * Defaults to port '0', ephemeral.
	 */
	public AndroidUpnpServiceConfiguration() {
		super();
	}

	public AndroidUpnpServiceConfiguration(int streamListenPort) {
		super(streamListenPort);
	}

	protected ExecutorService createDefaultExecutorService() {
		return UpnpIGDAgent.serviceExecutor;
	}
}

class DefaultUpnpServiceConfiguration implements org.fourthline.cling.UpnpServiceConfiguration {
	private final org.fourthline.cling.UpnpServiceConfiguration usc;

	/**
	 * Defaults to port '0', ephemeral.
	 */
	public DefaultUpnpServiceConfiguration() {
		this(0);
	}

	public DefaultUpnpServiceConfiguration(int streamListenPort) {
		if (OSValidator.getCurrentOS()==OSValidator.ANDROID) {
			usc = new AndroidUpnpServiceConfiguration(streamListenPort);
		} else
			usc = new NONAndroidUpnpServiceConfiguration(streamListenPort);
	}

	@Override
	public NetworkAddressFactory createNetworkAddressFactory() {
		return usc.createNetworkAddressFactory();
	}

	@Override
	public DatagramProcessor getDatagramProcessor() {
		return usc.getDatagramProcessor();
	}

	@Override
	public SOAPActionProcessor getSoapActionProcessor() {
		return usc.getSoapActionProcessor();
	}

	@Override
	public GENAEventProcessor getGenaEventProcessor() {
		return usc.getGenaEventProcessor();
	}

	@Override
	public StreamClient<?> createStreamClient() {
		return usc.createStreamClient();
	}

	@Override
	public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory _networkAddressFactory) {
		return usc.createMulticastReceiver(_networkAddressFactory);
	}

	@Override
	public DatagramIO<?> createDatagramIO(NetworkAddressFactory _networkAddressFactory) {
		return usc.createDatagramIO(_networkAddressFactory);
	}

	@Override
	public StreamServer<?> createStreamServer(NetworkAddressFactory _networkAddressFactory) {
		return usc.createStreamServer(_networkAddressFactory);
	}

	@Override
	public Executor getMulticastReceiverExecutor() {
		return usc.getMulticastReceiverExecutor();
	}

	@Override
	public Executor getDatagramIOExecutor() {
		return usc.getDatagramIOExecutor();
	}

	@Override
	public ExecutorService getStreamServerExecutorService() {
		return usc.getStreamServerExecutorService();
	}

	@Override
	public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
		return usc.getDeviceDescriptorBinderUDA10();
	}

	@Override
	public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
		return usc.getServiceDescriptorBinderUDA10();
	}

	@Override
	public ServiceType[] getExclusiveServiceTypes() {
		return usc.getExclusiveServiceTypes();
	}

	@Override
	public int getRegistryMaintenanceIntervalMillis() {
		return usc.getRegistryMaintenanceIntervalMillis();
	}

	@Override
	public int getAliveIntervalMillis() {
		return usc.getAliveIntervalMillis();
	}

	@Override
	public boolean isReceivedSubscriptionTimeoutIgnored() {
		return usc.isReceivedSubscriptionTimeoutIgnored();
	}

	@Override
	public Integer getRemoteDeviceMaxAgeSeconds() {
		return usc.getRemoteDeviceMaxAgeSeconds();
	}

	@Override
	public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity _identity) {
		return usc.getDescriptorRetrievalHeaders(_identity);
	}

	@Override
	public UpnpHeaders getEventSubscriptionHeaders(RemoteService _service) {
		return usc.getEventSubscriptionHeaders(_service);
	}

	@Override
	public Executor getAsyncProtocolExecutor() {
		return usc.getAsyncProtocolExecutor();
	}

	@Override
	public ExecutorService getSyncProtocolExecutorService() {
		return usc.getSyncProtocolExecutorService();
	}

	@Override
	public Namespace getNamespace() {
		return usc.getNamespace();
	}

	@Override
	public Executor getRegistryMaintainerExecutor() {
		return usc.getRegistryMaintainerExecutor();
	}

	@Override
	public Executor getRegistryListenerExecutor() {
		return usc.getRegistryListenerExecutor();
	}

	@Override
	public void shutdown() {
		usc.shutdown();
	}

}
