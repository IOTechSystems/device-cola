require ("demolibs")

post("metadata", "addressable",
        { name = "Zone 1 Lidar DistanceAhead",
          protocol = "HTTP",
          address = "device-cola2",
          port = 49978,
          path = "/api/v1/device/ " deviceID  " /DistanceAhead",
          method = "GET" })

post("metadata", "schedule", { name = "interval-1s", frequency = "PT1S" })

post ("metadata", "scheduleevent",
      { name = "Retrieve DistanceAhead",
        addressable = { name= "Zone 1 Lidar DistanceAhead" },
        schedule = "interval-1s",
        service = "device-cola2"})
