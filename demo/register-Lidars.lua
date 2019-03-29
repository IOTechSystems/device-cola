require("demolibs")

upload_profile("SICK.TiM100.Lidar.profile.yaml")

function register_sensor(deviceNameParam)

    post("metadata", "addressable", {
        name = deviceNameParam .. " Address",
        protocol = "TCP",
        address = "10.211.55.6",
        port = 2112
    })

    code, id = post("metadata", "device",
        {
            name = deviceNameParam,
            description = "Sensor",
            adminState = "UNLOCKED",
            operatingState = "ENABLED",
            addressable = { name = deviceNameParam .. " Address" },
            labels = { "sensor" },
            service = { name = "device-cola" },
            profile = { name = "TiM100-2d-Lidar" }
        })
end

register_sensor("Zone 1 Lidar")
