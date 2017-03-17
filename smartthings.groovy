import groovy.json.JsonSlurper
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.150", defaultValue: "192.168.1.150" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "80", defaultValue: "80" , required: true, displayDuringSetup: true)
        input("username", "string", title:"Username", description: "pi", defaultValue: "pi" , required: true, displayDuringSetup: true)
        input("password", "password", title:"Password", description: "raspberry", defaultValue: "raspberry" , required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "Raspberry Pi", namespace: "nicholaswilde", author: "Nicholas Wilde") {
		capability "Actuator"
        capability "Illuminance Measurement"        
        capability "Light"
        capability "Motion Sensor"        
        capability "Polling"
        capability "Refresh"
        capability "Relative Humidity Measurement"        
		capability "Sensor"	
        capability "Temperature Measurement"
        
     //   attribute "cpuPercentage", "string"
      //  attribute "memory", "string"
    //    attribute "diskUsage", "string"
        
        //command "restart"
        //command "setGpioValue0"
       // command "setGpioValue1"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state "temperature", label:'${currentValue}°', unit: "C",
            backgroundColors:[
                [value: 0, color: "#153591"],
                [value: 5, color: "#1e9cbb"],
                [value: 10, color: "#90d2a7"],
                [value: 15, color: "#44b621"],
                [value: 20, color: "#f1d801"],
                [value: 25, color: "#d04e00"],
                [value: 30, color: "#bc2323"]
            ]
        }
        valueTile("humidity", "device.humidity", width: 2, height: 2) {
            state "humidity", label:'${currentValue}°', unit: "%",
            backgroundColors:[
                [value: 0, color: "#153591"],
                [value: 5, color: "#1e9cbb"],
                [value: 10, color: "#90d2a7"],
                [value: 15, color: "#44b621"],
                [value: 20, color: "#f1d801"],
                [value: 25, color: "#d04e00"],
                [value: 30, color: "#bc2323"]
            ]
        }
        standardTile("lighton", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label:'Off', icon:"st.contact.contact.closed", backgroundColor:"#79b821", action: "on"
			state "on", label:'On', icon:"st.contact.contact.open", backgroundColor:"#ffa81e", action: "off"
		}
        standardTile("motion", "device.motion", width: 2, height: 2) {
            state("inactive", label:'${name}', icon:"st.motion.motion.inactive", backgroundColor:"#79b821")
            state("active", label:'${name}', icon:"st.motion.motion.active", backgroundColor:"#ffa81e")
        }
        standardTile("islight", "device.illuminance", width: 2, height: 2) {
            state("dark", label:'${name}', icon:"st.illuminance.illuminance.dark", backgroundColor:"#79b821")
            state("bright", label:'${name}', icon:"st.illuminance.illuminance.bright", backgroundColor:"#ffa81e")
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        // multiAttributeTile(name:"contact", type: "lighting", width: 3, height: 2){
     	//    tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
        //    		attributeState "closed", label:'Off', icon:"st.contact.contact.open", backgroundColor:"#79b821", action: "setGpioValue1"
        //     	attributeState "open", label:'On', icon:"st.contact.contact.closed", backgroundColor:"#ffa81e", action: "setGpioValue0"
      	//  	}
   		// }
    
        main "lighton"
        details(["motion", "islight", "temperature", "humidity", "lighton", "refresh"])
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
    	log.debug "Computer is up"
   	//	sendEvent(name: "switch", value: "on")
    }
    
    log.debug "check temp..."
    if (result.containsKey("temp")) {
        log.debug "temp: ${result.temp.toDouble().round()} C"
    	sendEvent(name: "temperature", value: result.temp.toDouble().round())
    }

    log.debug "check humidity..."
    if (result.containsKey("humidity")) {
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
    def uri = "/api/v1/data/0"
    patchAction(uri)
}

def on(){
	log.debug "Setting light on"
    sendEvent(name: "lighton", value: "on")
    def uri = "/api/v1/data/1"
    patchAction(uri)
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

private patchAction(path){

    setDeviceNetworkId(ip,port)  

    def result = new physicalgraph.device.HubAction(
        method: "PATCH",
        path: path,
        headers: [
            HOST: getHostAddress()
        ]
    )

    result

    // def params = [
    //     uri: getHostAddress(),
    //     path: path
    // ]

 //   log.debug "params uri: ${params.uri}, path: ${params.path}"

  
//   def userpass = encodeCredentials(username, password)
//   //log.debug("userpass: " + userpass) 
  
//   def headers = getHeader(userpass)
//   //log.debug("headders: " + headers) 
  
//   def hubAction = new physicalgraph.device.HubAction(
//     method: "POST",
//     path: uri,
//     headers: headers
//   )//,delayAction(1000), refresh()]
//   log.debug("Executing hubAction on " + getHostAddress())
//   //log.debug hubAction
//   hubAction    
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
   // return "http://${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}