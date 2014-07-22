/**
 *  HTTP Ping
 *
 *  Copyright 2014 Kristopher Kubicki
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

preferences {
	input("dest_ip", "text", title: "IP", description: "The device IP you wish to ping")
    input("dest_port", "number", title: "Port", description: "The port you wish to connect to to emulate a ping")
}
 

metadata {
	definition (name: "Ping", namespace: "Ping", author: "Kristopher Kubicki") {
		capability "Polling"
		attribute "status", "string"
        attribute "ttl", "string"
        attribute "last_request", "number"
        attribute "last_live", "number"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("status", "device.status", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
            state "Down", label: '${device.name} Down', backgroundColor: "#FF0000", icon:"st.secondary.off"
            state "Up", label: '${device.name} Up', backgroundColor: "#32CD32", icon:"st.switches.light.on"
        }
        standardTile("refresh", "device.ttl", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        standardTile("ttl", "device.ttl", inactiveLabel: false, decoration: "flat") {
            state "ttl", label:'${ttl}'
        }
		main "status"
        	details(["status", "refresh", "ttl"])
		}
		// TODO: define your main and details tiles here
	}




def parse(String description) {
//  log.debug "Parsing '${description}'"
    def map = stringToMap(description)

//  def headerString = new String(map.headers.decodeBase64())
//	def bodyString = new String(map.body.decodeBase64())
//  log.debug "Parsing '${headerString}'"
//	log.debug "Parsing '${bodyString}'"
    
    def c = new GregorianCalendar()
    sendEvent(name: 'status', value: "${device.name} Up")
    sendEvent(name: 'last_live', value: c.time.time)
    def ping = ttl()
    sendEvent(name: 'ttl', value: ping)
    
    log.debug "Pinging ${device.deviceNetworkId}: ${ping}"

}

private ttl() { 
    def last_request = device.latestValue("last_request")
    if(!last_request) {
    	last_request = 0
    }
    def last_alive = device.latestValue("last_live")
    if(!last_alive) { 
    	last_alive = 0
    }
    def last_status = device.latestValue("status")
    
    def c = new GregorianCalendar()
    def ttl = c.time.time - last_request
    if(ttl > 10000 || last_status == "Down") { 
    	ttl = c.time.time - last_alive
    }
    
    def units = "ms"
    if(ttl > 10*52*7*24*60*60*1000) { 
    	return "Never"
    }
    else if(ttl > 52*7*24*60*60*1000) { 
        ttl = ttl / (52*7*24*60*60*1000)
        units = "y"
    }
    else if(ttl > 7*24*60*60*1000) { 
        ttl = ttl / (7*24*60*60*1000)
        units = "w"
    }
    else if(ttl > 24*60*60*1000) { 
        ttl = ttl / (24*60*60*1000)
        units = "d"
    }
    else if(ttl > 60*60*1000) { 
        ttl = ttl / (60*60*1000)
        units = "h"
    }
    else if(ttl > 60*1000) { 
        ttl = ttl / (60*1000)
        units = "m"
    }
    else if(ttl > 1000) { 
        ttl = ttl / 1000
        units = "s"
    }  
    def ttl_int = ttl.intValue()
    
    "${ttl_int} ${units}"
}


// handle commands
def poll() {

    def hosthex = convertIPToHex(dest_ip)
    def porthex = Long.toHexString(Long.parseLong(dest_port))
    if (porthex.length() < 4) { porthex = "00" + porthex }
    device.deviceNetworkId = "$hosthex:$porthex" 
    
//   log.debug "The DNI configured is $device.deviceNetworkId"
   
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: "/"
    )        
  
    def last_request = device.latestValue("last_request")
    def last_live = device.latestValue("last_live")
    if(!last_request) {
    	last_request = 0
    }
    if(!last_live) {
    	last_live = 0
    }

    def c = new GregorianCalendar()
    if(last_live < last_request) { 
    	sendEvent(name: 'status', value: "${device.name} Down")  
        sendEvent(name: 'ttl', value: ttl())
    }
    sendEvent(name: 'last_request', value: c.time.time)
       
//  log.debug hubAction
    
	hubAction
}


private Long convertIntToLong(ipAddress) {
	long result = 0
	def parts = ipAddress.split("\\.")
    for (int i = 3; i >= 0; i--) {
        result |= (Long.parseLong(parts[3 - i]) << (i * 8));
    }

    return result & 0xFFFFFFFF;
}

private String convertIPToHex(ipAddress) {
	return Long.toHexString(convertIntToLong(ipAddress));
}
