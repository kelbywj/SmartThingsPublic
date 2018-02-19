/**
 *  Shop Vent Fan
 *
 *  Copyright 2018 Kelby Johnson
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
definition(
    name: "Shop Vent Fan",
    namespace: "kelbywj",
    author: "Kelby Johnson",
    description: "Turn on shop vents when cooler or warmer outside than inside based on outside temp",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Thermometers") {
		input "insideTemp", "capability.temperatureMeasurement", title: "Indside thermometer?", required: true
		input "outsideTemp", "capability.temperatureMeasurement", title: "Outside thermometer?", required: true
	}
	section("Select Vent Fan Switch") {
    		input "ventCoolSwitch", "capability.switch", title: "Which vent switch for cooling?", required: true
            input "ventHeatSwitch", "capability.switch", title: "Which vent switch for heating?", required: true
	}
	section("Set Temperatures") {
    		input "baseTemp", "decimal", title: "Temperature setpoint?", required: true, defaultValue: 70
    		input "differential", "decimal", title: "How many degrees differential?", required: true, defaultValue: 5
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(insideTemp, "temperature", tempHandler)
	subscribe(outsideTemp, "temperature", tempHandler)
	ventCoolSwitch.off()
    ventHeatSwitch.off()
	state.ventCoolOff = true
    state.ventHeatOff = true
	evalTemps()
}

def evalTemps() {
	def inTemp = insideTemp.currentTemperature
	def outTemp = outsideTemp.currentTemperature
	log.debug "evalTemps in: $inTemp out: $outTemp"
//Runs vent fan that is mounted high if temperature outside is less than temperature inside.
	if((inTemp > outTemp + differential) && (inTemp > baseTemp) && state.ventCoolOff) {
		ventCoolSwitch.on()
		state.ventCoolOff = false
        log.debug "Cooling fan on"
	}
	if((inTemp <= outTemp || inTemp < baseTemp) && !state.ventCoolOff) {
		ventCoolSwitch.off()
		state.ventCoolOff = true
        log.debug "Cooling fan off"
     }   
//Runs vent fan that is mounted low if temperature outside is greater than temperature inside.
	if((inTemp < outTemp - differential) && (inTemp < baseTemp) && state.ventHeatOff) {
		ventHeatSwitch.on()
		state.ventHeatOff = false
        log.debug "Heating fan on"
	}
	
    if((inTemp >= outTemp || inTemp > baseTemp) && !state.ventHeatOff) {
		ventHeatSwitch.off()
		state.ventHeatOff = true
        log.debug "Heating fan off"
	}
}

def tempHandler(evt) {
	log.debug "tempHandler: $evt.displayName $evt.value"
	evalTemps()
}