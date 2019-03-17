local http = require("socket.http")
local ltn12 = require("ltn12")
local json = require("dkjson")
local multipart = (require "multipart-post").gen_request

-- ip="172.16.52.131"
ip = "127.0.0.1"
endpoints = {
    metadata = ip .. ":48081",
    export_client = ip .. ":48071",
    data = ip .. ":48080",
    command = ip .. ":48082",
    logging = ip .. ":48061",
    notifications = ip .. ":48060",
}

local function _formencodepart(s)
    return s and (s:gsub("%W", function(c)
        if c ~= " " then
            return string.format("%%%02x", c:byte());
        else
            return "+";
        end
    end));
end

function formencode(form)
    local result = {};
    if form[1] then -- Array of ordered { name, value }
        for _, field in ipairs(form) do
            table.insert(result, _formencodepart(field.name) .. "=" .. _formencodepart(field.value));
        end
    else -- Unordered map of name -> value
        for name, value in pairs(form) do
            table.insert(result, _formencodepart(name) .. "=" .. _formencodepart(value));
        end
    end
    return table.concat(result, "&");
end

function get(target, path)
    local response_body = {}
    local url = "http://" .. endpoints[target] .. "/api/v1/" .. path
    local res, code, response_headers, status = http.request
        {
            url = url,
            method = "GET",
            sink = ltn12.sink.table(response_body),
        }
    print(url)
    print("res = " .. res .. " code = " .. code .. " response_headers = " ..
            table.concat(response_headers, ',') .. "status = " .. status ..
            " response_body = " .. table.concat(response_body))
    for k, v in pairs(response_body) do
        print(k, v)
    end
    return code, table.concat(response_body)
end

function post(target, path, data)
    local payload = json.encode(data)
    local response_body = {}
    local url = "http://" .. endpoints[target] .. "/api/v1/" .. path
    local res, code, response_headers, status = http.request
        {
            url = url,
            method = "POST",
            headers = {
                ["Content-Type"] = "application/json",
                ["Content-Length"] = payload:len()
            },
            source = ltn12.source.string(payload),
            sink = ltn12.sink.table(response_body),
        }
    print(url)
    print(payload)
    print(code)
    print("res = " .. res .. " code = " .. code .. " response_headers = " ..
            table.concat(response_headers, ',') .. "status = " .. status ..
            " response_body = " .. table.concat(response_body))
    for k, v in pairs(response_body) do
        print(k, v)
    end
    return code, table.concat(response_body)
end

function upload_profile(profile)
    local file = io.open(profile, "rb")
    local contents = file:read("*a")
    local rq = multipart {
        file = {
            content_type = "application/x-yaml",
            name = "file",
            data = contents
        }
    }
    rq.url = "http://" .. endpoints.metadata ..
            "/api/v1/deviceprofile/uploadfile"
    local res, code, response_headers, status = http.request(rq)
    print("res = " .. res .. " code = " .. code .. " response_headers = " ..
            table.concat(response_headers, ',') .. "status = " .. status)
end

--function sleep(sec)
--   socket.select(nil, nil, sec)
--end

local clock = os.clock

function sleep(n)
    local t0 = clock()
    while clock() - t0 <= n do end
end
