/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice: device-cola
 * @author: Tyler Cox, Dell
 * @version: 1.0.0
 *******************************************************************************/

package org.edgexfoundry.pkg.cola;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.ResourceOperation;
import org.edgexfoundry.exception.controller.ServiceException;
import org.edgexfoundry.pkg.data.DeviceStore;
import org.edgexfoundry.pkg.data.ObjectStore;
import org.edgexfoundry.pkg.data.ProfileStore;
import org.edgexfoundry.pkg.domain.COLAAttribute;
import org.edgexfoundry.pkg.domain.COLAObject;
import org.edgexfoundry.pkg.domain.ScanList;
import org.edgexfoundry.pkg.handler.COLAHandler;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.sick.sopas.scl.ColaDialect;

@Service
public class COLADriver {

	private static final EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(COLADriver.class);

	@Autowired
	ProfileStore profiles;

	@Autowired
	DeviceStore devices;

	@Autowired
	ObjectStore objectCache;

	@Autowired
	COLAHandler handler;

	private Map<String, TiM5Driver> driverMap = Collections.synchronizedMap(new HashMap<>());

	public ScanList discover() {
		ScanList scan = new ScanList();

		// TODO 4: [Optional] For discovery enabled device services:
		// Replace with COLA specific discovery mechanism
		// TODO 5: [Required] Remove next code block if discovery is not used
		for (int i = 0; i < 10; i++) {
			Map<String, String> identifiers = new HashMap<>();
			identifiers.put("name", String.valueOf(i));
			identifiers.put("address", "02:01:00:11:12:1" + String.valueOf(i));
			identifiers.put("interface", "default");
			scan.add(identifiers);
		}

		return scan;
	}

	// operation is get or set
	// Device to be written to
	// COLA Object to be written to
	// value is string to be written or null
	public void process(ResourceOperation operation, Device device, COLAObject object, String value,
			String transactionId, String opId) {
		String result = "";

		// TODO 2: [Optional] Modify this processCommand call to pass any additional
		// required metadata
		// from the profile to the driver stack
		result = processCommand(operation.getOperation(), device.getAddressable(), object.getAttributes(), value,
				transactionId, object);

		objectCache.put(device, operation, result);
		handler.completeTransaction(transactionId, opId, objectCache.getResponses(device, operation));
	}

	// Modify this function as needed to pass necessary metadata from the device and
	// its profile to
	// the driver interface
	public String processCommand(String operation, Addressable addressable, COLAAttribute attributes, String value,
			String transactionId, COLAObject object) {
		String address = addressable.getPath();
		String intface = addressable.getAddress();
		logger.debug("ProcessCommand: " + operation + ", interface: " + intface + ", address: " + address
				+ ", attributes: " + attributes + ", value: " + value);
		String result = "";

		// TODO 1: [Required] COLA stack goes here, return the raw value from the
		// device
		// as a string for processing
		TiM5Driver l_driver = null;
		try {
			if (driverMap.containsKey(addressable.getAddress())) {
				l_driver = driverMap.get(addressable.getAddress());
			} else {
				l_driver = new TiM5Driver(addressable.getAddress(), addressable.getPort(), ColaDialect.COLA_A);
				l_driver.connect();
				driverMap.put(addressable.getAddress(), l_driver);
			}
			if (operation.toLowerCase().equals("get")) {
				if ("SerialNumber".equals(object.getName())) {
					result = l_driver.readSerialnumber();
				} else if ("DistanceAhead".equals(object.getName())) {
					result = Integer.toString(l_driver.readDistanceAhead());
				} else if ("Scandata".equals(object.getName())) {
					int[] l_scandata = l_driver.readScandata();

					StringBuilder l_sb = new StringBuilder();
					l_sb.append("[");
					for (int i = 0; i < l_scandata.length; i++) {
						if (0 != i) {
							l_sb.append(", ");
						}
						l_sb.append(Integer.toString(l_scandata[i]));
					}
					l_sb.append("]");

					result = l_sb.toString();
				}
			} else {
				result = value;
			}
		} catch (IOException e) {
			logger.error("Driver process Exception e:" + e.getMessage());
			logger.debug(e.getMessage(), e);
			handler.failTransaction(transactionId, new ServiceException(e));
			Thread.currentThread().interrupt();
		}

		return result;
	}

	public void initialize() {
		// TODO 3: [Optional] Initialize the interface(s) here if necessary, runs once
		// on
		// service startup
	}

	public void disconnectDevice(Addressable address) {
		// TODO 6: [Optional] Disconnect devices here using driver level operations
	}

	@PreDestroy
	public void disconnectDevices() {
		for (TiM5Driver driver : driverMap.values()) {
			try {
				driver.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private void receive() {
		// TODO 7: [Optional] Fill with your own implementation for handling
		// asynchronous
		// data from the driver layer to the device service
		Device device = null;
		String result = "";
		ResourceOperation operation = null;

		objectCache.put(device, operation, result);
	}
}
