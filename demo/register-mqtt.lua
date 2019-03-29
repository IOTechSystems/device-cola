require ("demolibs")

function register_mqtt_export (exportNameParam, filterParam, topicNameParam)
   id = { name = exportNameParam .. " Address",
	  protocol = "TCP",
	  address = "192.168.0.20",
	  port = 1883,
	  publisher = exportNameParam,
	  topic = topicNameParam }
   
   post("metadata", "addressable", id)

   post("export_client", "registration", { name = exportNameParam,
					   addressable = id,
					   format = "JSON",
					   filter = { deviceIdentifiers = { filterParam }},
					   destination = "MQTT_TOPIC",
					   enable = true })
end

register_mqtt_export("MQTT", "MQTTExportTopic", "Zone 1 Lidar")
