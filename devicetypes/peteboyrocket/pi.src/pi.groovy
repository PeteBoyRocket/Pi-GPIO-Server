import groovy.json.JsonSlurper
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.150", defaultValue: "192.168.1.150" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "80", defaultValue: "80" , required: true, displayDuringSetup: true)
     //   input("username", "string", title:"Username", description: "pi", defaultValue: "pi" , required: true, displayDuringSetup: true)
    //    input("password", "password", title:"Password", description: "raspberry", defaultValue: "raspberry" , required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "Raspberry Pi", namespace: "peteboyrocket", author: "Pete Whitehead") {
		capability "Actuator"
        capability "Illuminance Measurement"        
        capability "Light"
        capability "Motion Sensor"        
        capability "Polling"
        capability "Refresh"
        capability "Relative Humidity Measurement"        
		capability "Sensor"	
        capability "Temperature Measurement"
        capability "Switch"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state "temperature", label:'Temp ${currentValue}°', unit: "C",
            backgroundColors:[
                [value: 40, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 92, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("humidity", "device.humidity", width: 2, height: 2) {
            state "humidity", label:'Humidity ${currentValue}%', unit: "%",
            backgroundColors:[
                [value: 35, color: "#153591"],
                [value: 40, color: "#1e9cbb"],
                [value: 45, color: "#90d2a7"],
                [value: 50, color: "#44b621"],
                [value: 55, color: "#f1d801"],
                [value: 60, color: "#d04e00"],
                [value: 65, color: "#bc2323"]
            ]
        }
        standardTile("motion", "device.motion", width: 3, height: 2) {
            state("inactive", label:'${name}', icon:"st.motion.motion.inactive", backgroundColor:"#79b821")
            state("active", label:'${name}', icon:"st.motion.motion.active", backgroundColor:"#ffa81e")
        }
        standardTile("islight", "device.illuminance", width: 3, height: 2) {
            state("dark", label:'${name}', icon:"st.illuminance.illuminance.dark", backgroundColor:"#79b821")
            state("bright", label:'${name}', icon:"st.illuminance.illuminance.bright", backgroundColor:"#ffa81e")
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        multiAttributeTile(name:"lighton", type: "lighting", canChangeIcon: true){
     	    tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
           		attributeState "off", label:'Off', icon:"st.contact.contact.open", backgroundColor:"#ffffff", action: "on"
             	attributeState "on", label:'On', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc", action: "off"
      	  	}
   		}
    
        main "lighton"
        details(["lighton", "motion", "islight", "temperature", "humidity", "refresh"])
    }
}

// ------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
    def map = [:]
    def descMap = parseDescriptionAsMap(description)
    log.debug "descMap: ${descMap}"
    
    def body = new String(descMap["body"].decodeBase64())
    log.debug "body: ${body}"
    
    if (body.contains("OK")){
    	log.debug "body was 'OK'. Returning"
        return
    }
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    log.debug "result: ${result}"

	if (result){
    	log.debug "Computer is up "
    }
    
    log.debug "check temp..."
    if (result.containsKey("temp") && result.temp != null) {
        log.debug "temp: ${result.temp.toDouble().round()} C"
    	sendEvent(name: "temperature", value: result.temp.toDouble().round())
    }

    log.debug "check humidity..."
    if (result.containsKey("humidity") && result.humidity != null) {
        log.debug "humidity: ${result.humidity.toDouble().round()}"
    	sendEvent(name: "humidity", value: result.humidity.toDouble().round())
    }
 
  	if (result.containsKey("lighton")) {
     	log.debug "lighton: ${result.lighton.toDouble().round()}"
        if (result.lighton.contains("1")){
           	log.debug "lighton: on"
            sendEvent(name: "switch", value: "on")
        } else {
           	log.debug "lighton: off"
            sendEvent(name: "switch", value: "off")
        }
    }
  	
    if (result.containsKey("motion")) {
        log.debug "motion: ${result.motion.toDouble().round()}"
        if (result.motion.contains("1")){
            log.debug "motion: active"
            sendEvent(name: "motion", value: "active")
        } else {
           	log.debug "motion: inactive"
            sendEvent(name: "motion", value: "inactive")
        }
    }

    if (result.containsKey("islight")) {
        log.debug "islight: ${result.islight.toDouble().round()}"
        if (result.islight.contains("1")){
            log.debug "islight: bright"
            sendEvent(name: "illuminance", value: "bright")
        } else {
         	log.debug "islight: dark"
            sendEvent(name: "illuminance", value: "dark")
        }
    }
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getRPiData()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getRPiData()
}

def off(){
	log.debug "Setting light off"
    sendEvent(name: "lighton", value: "off")
    def uri = "/api/v1/pin/11"
    patchAction(uri, 0)
}

def on(){
	log.debug "Setting light on"
    sendEvent(name: "lighton", value: "on")
    def uri = "/api/v1/pin/11"
    patchAction(uri, 1)
}

private getRPiData() {
	def uri = "/api/v1/data"
    getAction(uri)
}

// ------------------------------------------------------------------

private getAction(path){

    setDeviceNetworkId(ip,port)  

    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: path,
        headers: [
            HOST: getHostAddress()
        ]
    )

    result   
}

private patchAction(path, value){

    setDeviceNetworkId(ip,port)  

    def result = new physicalgraph.device.HubAction(
        method: "PATCH",
        path: path,
        headers: [
            HOST: getHostAddress(),
            "Content-Type":"application/json"
        ],
        body: "{\"value\":${value}}"
    )

    result  
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}


def toAscii(s){
        StringBuilder sb = new StringBuilder();
        String ascString = null;
        long asciiInt;
                for (int i = 0; i < s.length(); i++){
                    sb.append((int)s.charAt(i));
                    sb.append("|");
                    char c = s.charAt(i);
                }
                ascString = sb.toString();
                asciiInt = Long.parseLong(ascString);
                return asciiInt;
    }

private encodeCredentials(username, password){
	log.debug "Encoding credentials"
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    //log.debug "ASCII credentials are ${userpassascii}"
    //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass){
	log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    headers.put("Authorization", userpass)
    //log.debug "Headers are ${headers}"
    return headers
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "$iphex:$porthex"
  	log.debug "Device Network Id set to ${iphex}:${porthex}"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}
